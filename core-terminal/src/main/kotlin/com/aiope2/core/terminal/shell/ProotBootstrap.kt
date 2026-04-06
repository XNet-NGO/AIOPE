package com.aiope2.core.terminal.shell

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Downloads and configures the proot Ubuntu rootfs.
 * Aligned with AIOPE's aiope_cli.py env_setup for reliability.
 */
object ProotBootstrap {

    private const val TAG = "ProotBootstrap"
    private const val BOX_ROOTFS_VERSION = "rootfs_box_v2"

    fun envDir(ctx: Context) = File(ctx.filesDir, "env")
    fun rootfsDir(ctx: Context) = File(envDir(ctx), "ubuntu")
    private fun marker(ctx: Context, name: String) = File(envDir(ctx), ".$name")

    fun isInstalled(ctx: Context): Boolean {
        val rootfs = rootfsDir(ctx)
        return marker(ctx, BOX_ROOTFS_VERSION).exists()
            && rootfs.isDirectory
            && File(rootfs, "bin/sh").isFile
    }

    /**
     * Find proot-xed binary, handling Android renaming libproot-xed.so to libroot-xed.so.
     */
    fun findProotXed(ctx: Context): File? {
        val nativeDir = File(ctx.applicationInfo.nativeLibraryDir)
        for (name in arrayOf("libproot-xed.so", "libroot-xed.so")) {
            val f = File(nativeDir, name)
            if (f.isFile) return f
        }
        return null
    }

    /**
     * Re-apply patches to an existing rootfs (e.g. after app update).
     */
    fun ensurePatched(ctx: Context, log: (String) -> Unit) {
        val rootfs = rootfsDir(ctx)
        if (rootfs.isDirectory) {
            fixProotStubs(rootfs)
            patchRootfs(rootfs, log)
        }
    }

    /**
     * Full bootstrap: create dirs, copy talloc, download rootfs, patch.
     * Call on a background thread.
     */
    fun setup(ctx: Context, log: (String) -> Unit): Boolean {
        try {
            val filesDir = ctx.filesDir
            val nativeDir = ctx.applicationInfo.nativeLibraryDir
            val envDir = envDir(ctx)
            val rootfs = rootfsDir(ctx)

            // 1. Create dirs
            log("Creating directories...")
            listOf(envDir, rootfs, File(filesDir, "tmp"), File(filesDir, "home")).forEach { it.mkdirs() }

            // 2. Copy libtalloc.so → libtalloc.so.2 (proot needs this)
            val tallocSrc = File(nativeDir, "libtalloc.so")
            val tallocDst = File(filesDir, "libtalloc.so.2")
            if (tallocSrc.exists()) {
                tallocSrc.inputStream().use { i -> tallocDst.outputStream().use { o -> i.copyTo(o) } }
                log("libtalloc.so.2 ready (${tallocDst.length()} bytes)")
            } else {
                log("ERROR: libtalloc.so not found in $nativeDir")
                return false
            }

            // 2b. Prepare bsdtar binary
            val bsdtar = prepareBsdtar(ctx)
            if (bsdtar == null) {
                log("ERROR: libbsdtar.so not found in $nativeDir")
                return false
            }

            // 3. Download rootfs if needed — use box version marker to detect upgrades
            val boxMarker = marker(ctx, BOX_ROOTFS_VERSION)
            val rootfsHasSh = File(rootfs, "bin/sh").isFile
            if (!boxMarker.exists() || !rootfsHasSh) {
                // Remove old rootfs if present (upgrade path)
                if (rootfs.isDirectory) {
                    log("Removing old rootfs for Box upgrade...")
                    rootfs.deleteRecursively()
                    rootfs.mkdirs()
                }

                val arch = System.getProperty("os.arch")?.lowercase() ?: "aarch64"
                val pdArch = when {
                    "aarch64" in arch || "arm64" in arch -> "aarch64"
                    "armv7" in arch || "arm" in arch -> "arm"
                    "x86_64" in arch -> "x86_64"
                    else -> "aarch64"
                }
                val url = "https://github.com/xnet-admin-1/box/releases/download/rootfs-ubuntu-24.04.4/box-ubuntu-24.04-$pdArch.tar.xz"
                val tarball = File(envDir, "rootfs.tar.xz")

                log("Downloading Ubuntu 24.04 rootfs ($pdArch)...")
                download(url, tarball, log)

                log("Extracting rootfs...")
                val ok = runBsdtar(bsdtar, tarball, rootfs, stripComponents = 1, log = log)
                tarball.delete()
                if (!ok || !File(rootfs, "bin/sh").isFile) {
                    log("ERROR: Extraction failed - bin/sh not found in rootfs")
                    return false
                }
                boxMarker.writeText("box-ubuntu-24.04")
                log("Rootfs extracted (Box release)")
            } else {
                log("Rootfs already installed")
            }

            // 4. Patch rootfs
            patchRootfs(rootfs, log)

            // 5. Distro setup — run commands inside proot (locale, etc.)
            distroSetup(ctx, log)

            log("Ubuntu environment ready!")
            return true
        } catch (e: Exception) {
            log("ERROR: ${e.message}")
            Log.e(TAG, "setup failed", e)
            return false
        }
    }

    private fun prepareBsdtar(ctx: Context): File? {
        val f = File(ctx.applicationInfo.nativeLibraryDir, "libbsdtar.so")
        return if (f.exists() && f.canExecute()) f else null
    }

    private fun runBsdtar(bsdtar: File, tarball: File, destDir: File, stripComponents: Int, log: (String) -> Unit): Boolean {
        destDir.mkdirs()
        val cmd = mutableListOf(
            bsdtar.absolutePath, "-xf", tarball.absolutePath,
            "-C", destDir.absolutePath, "--no-same-owner"
        )
        if (stripComponents > 0) {
            cmd.addAll(listOf("--strip-components", stripComponents.toString()))
        }
        Log.d(TAG, "bsdtar: ${cmd.joinToString(" ")}")
        return try {
            val pb = ProcessBuilder(cmd)
            pb.redirectErrorStream(true)
            pb.environment()["LD_LIBRARY_PATH"] = bsdtar.parentFile?.absolutePath ?: ""
            val proc = pb.start()
            val output = proc.inputStream.bufferedReader().readText()
            val ok = proc.waitFor(300, TimeUnit.SECONDS)
            val exit = if (ok) proc.exitValue() else -1
            if (exit != 0) {
                if (!ok) { proc.destroyForcibly(); log("bsdtar timed out"); return false }
                val lines = output.lines().filter { it.isNotBlank() }
                val hardlinkErrors = lines.filter { "Can't create" in it || "Permission denied" in it }
                val otherErrors = lines.filter {
                    it.isNotBlank() && "Can't create" !in it && "Permission denied" !in it
                    && "Error exit delayed" !in it
                }
                if (otherErrors.isEmpty() && hardlinkErrors.isNotEmpty()) {
                    log("bsdtar: ${hardlinkErrors.size} hardlinks failed (Android limitation), fixing...")
                    fixFailedHardlinks(destDir, output, log)
                    true
                } else {
                    log("bsdtar error (exit $exit): ${output.take(500)}")
                    false
                }
            } else {
                if (output.isNotBlank()) Log.d(TAG, "bsdtar output: ${output.take(200)}")
                true
            }
        } catch (e: Exception) {
            log("bsdtar failed: ${e.message}")
            Log.e(TAG, "runBsdtar failed", e)
            false
        }
    }

    private fun fixFailedHardlinks(destDir: File, bsdtarOutput: String, log: (String) -> Unit) {
        val knownLinks = mapOf(
            "usr/bin/perl5.38.2" to "usr/bin/perl",
            "usr/bin/uncompress" to "usr/bin/gunzip",
            "usr/bin/pager" to "usr/bin/less",
            "usr/bin/zcmp" to "usr/bin/zdiff",
            "usr/bin/zegrep" to "usr/bin/zgrep",
            "usr/bin/zfgrep" to "usr/bin/zgrep",
            "usr/bin/zless" to "usr/bin/zmore",
            "usr/bin/bzcat" to "usr/bin/bzip2",
            "usr/bin/bunzip2" to "usr/bin/bzip2",
            "usr/bin/bzegrep" to "usr/bin/bzgrep",
            "usr/bin/bzfgrep" to "usr/bin/bzgrep",
            "usr/bin/bzless" to "usr/bin/bzmore",
            "usr/bin/lzcat" to "usr/bin/xz",
            "usr/bin/lzma" to "usr/bin/xz",
            "usr/bin/unlzma" to "usr/bin/xz",
            "usr/bin/unxz" to "usr/bin/xz",
            "usr/bin/xzcat" to "usr/bin/xz",
            "usr/bin/lzcmp" to "usr/bin/lzdiff",
            "usr/bin/lzegrep" to "usr/bin/lzgrep",
            "usr/bin/lzfgrep" to "usr/bin/lzgrep",
            "usr/bin/lzless" to "usr/bin/lzmore",
            "usr/sbin/vigr" to "usr/sbin/vipw",
            "usr/bin/sg" to "usr/bin/newgrp",
            "usr/bin/open" to "usr/bin/openvt",
            "usr/bin/i386" to "usr/bin/setarch",
            "usr/bin/x86_64" to "usr/bin/setarch",
            "usr/bin/linux32" to "usr/bin/setarch",
            "usr/bin/linux64" to "usr/bin/setarch",
            "usr/bin/uname26" to "usr/bin/setarch",
            "usr/bin/aarch64" to "usr/bin/setarch",
            "usr/bin/sha224sum" to "usr/bin/sha256sum",
            "usr/bin/sha384sum" to "usr/bin/sha512sum",
            "usr/bin/w.procps" to "usr/bin/w",
            "usr/bin/awk" to "usr/bin/mawk",
            "usr/bin/nawk" to "usr/bin/mawk",
            "usr/bin/rb" to "usr/bin/red",
            "usr/bin/pgrep" to "usr/bin/pidof",
            "usr/bin/slogin" to "usr/bin/ssh"
        )

        val regex = Regex("""^(.+?): Can't create '(.+?)': Permission denied""")
        val failedPaths = bsdtarOutput.lines().mapNotNull { regex.find(it)?.groupValues?.get(1) }

        var fixed = 0
        for (path in failedPaths) {
            val target = knownLinks[path] ?: continue
            val linkFile = File(destDir, path)
            val targetFile = File(destDir, target)
            if (targetFile.exists() && !linkFile.exists()) {
                try {
                    targetFile.inputStream().use { i -> linkFile.outputStream().use { o -> i.copyTo(o) } }
                    linkFile.setExecutable(targetFile.canExecute())
                    fixed++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fix hardlink $path → $target: ${e.message}")
                }
            }
        }
        log("  Fixed $fixed/${failedPaths.size} hardlinks (copied as regular files)")
    }

    private fun distroSetup(ctx: Context, log: (String) -> Unit) {
        val setupMarker = File(envDir(ctx), ".distro_setup_done")
        if (setupMarker.exists()) return

        log("Running distro setup...")
        try {
            // Fix any interrupted dpkg state first
            ProotExecutor.exec(ctx, "dpkg --configure -a --force-unsafe-io 2>/dev/null || true", timeoutMs = 60_000)
            // Then locale
            ProotExecutor.exec(ctx,
                "DEBIAN_FRONTEND=noninteractive dpkg-reconfigure locales 2>/dev/null || true",
                timeoutMs = 60_000)
            setupMarker.writeText("done")
            log("Distro setup complete")
        } catch (e: Exception) {
            Log.w(TAG, "distro_setup failed (non-fatal): ${e.message}")
            log("Distro setup skipped (${e.message})")
        }
    }

    private fun download(urlStr: String, dest: File, log: (String) -> Unit) {
        var url = URL(urlStr)
        var redirects = 0
        var conn: HttpURLConnection
        while (true) {
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 30_000; conn.readTimeout = 60_000
            conn.instanceFollowRedirects = false
            val code = conn.responseCode
            if (code in 301..308) {
                val loc = conn.getHeaderField("Location") ?: break
                url = URL(url, loc)
                conn.disconnect()
                if (++redirects > 5) { log("ERROR: too many redirects"); return }
                continue
            }
            if (code != 200) {
                log("ERROR: HTTP $code from $url")
                conn.disconnect()
                return
            }
            break
        }
        val total = conn.contentLength.toLong()
        var downloaded = 0L
        var lastReportedMB = -1L
        BufferedInputStream(conn.inputStream).use { input ->
            FileOutputStream(dest).use { output ->
                val buf = ByteArray(65536)
                var n: Int
                while (input.read(buf).also { n = it } != -1) {
                    output.write(buf, 0, n)
                    downloaded += n
                    val mb = downloaded / (1024 * 1024)
                    if (total > 0 && mb > lastReportedMB) {
                        lastReportedMB = mb
                        log("  ${mb}MB / ${total / (1024 * 1024)}MB")
                    }
                }
            }
        }
        conn.disconnect()
        log("  Download complete (${dest.length() / 1024}KB)")
    }

    /**
     * Fix proot stubs — ensure they're simple exit-0 scripts.
     * Called every time shells are discovered, not just at setup.
     * Matches AIOPE's _fix_proot_stubs().
     */
    fun fixProotStubs(rootfs: File) {
        val stubScript = "#!/bin/sh\nexit 0\n"
        for (rel in listOf("sbin/ldconfig", "usr/sbin/ldconfig", "usr/bin/ldconfig",
                           "usr/sbin/invoke-rc.d", "sbin/start-stop-daemon", "usr/sbin/start-stop-daemon")) {
            val f = File(rootfs, rel)
            if (f.isFile) {
                try {
                    val content = f.readText()
                    if ("exit 0" in content && content.length < 30) continue
                    f.writeText(stubScript)
                    f.setExecutable(true)
                } catch (_: Exception) {}
            }
        }
    }

    private fun patchRootfs(rootfs: File, log: (String) -> Unit) {
        // DNS
        File(rootfs, "etc").mkdirs()
        File(rootfs, "etc/resolv.conf").writeText("nameserver 8.8.8.8\nnameserver 8.8.4.4\nnameserver 1.1.1.1\n")

        // Locale
        val localeGen = File(rootfs, "etc/locale.gen")
        if (localeGen.exists()) {
            localeGen.writeText(localeGen.readText().replace(Regex("""#\s?(en_US\.UTF-8\s+UTF-8)"""), "$1"))
        }
        File(rootfs, "etc/default").mkdirs()
        File(rootfs, "etc/default/locale").writeText("LANG=en_US.UTF-8\nLANGUAGE=en_US:en\nLC_ALL=en_US.UTF-8\n")

        // Fix permissions on bin dirs
        listOf("bin", "sbin", "usr/bin", "usr/sbin", "usr/local/bin", "usr/local/sbin",
               "usr/lib/apt/methods", "usr/lib/dpkg").forEach { dir ->
            File(rootfs, dir).walkTopDown().filter { it.isFile }.forEach { it.setExecutable(true) }
        }
        // dpkg info scripts need +x
        File(rootfs, "var/lib/dpkg/info").let { dir ->
            if (dir.isDirectory) dir.walkTopDown()
                .filter { it.isFile && (it.name.endsWith(".postinst") || it.name.endsWith(".preinst")
                    || it.name.endsWith(".postrm") || it.name.endsWith(".prerm")) }
                .forEach { it.setExecutable(true) }
        }
        // ELF interpreters
        listOf("lib/ld-linux-aarch64.so.1", "lib/aarch64-linux-gnu/ld-linux-aarch64.so.1",
               "lib/aarch64-linux-gnu/ld-2.39.so", "lib/ld-linux-armhf.so.3",
               "usr/bin/env", "bin/sh", "bin/bash", "bin/dash").forEach { rel ->
            val f = File(rootfs, rel)
            if (f.exists() && !f.isDirectory) f.setExecutable(true)
        }
        // .so files
        listOf("lib", "usr/lib").forEach { dir ->
            File(rootfs, dir).walkTopDown()
                .filter { it.isFile && (it.name.endsWith(".so") || it.name.contains(".so.")) }
                .forEach { it.setExecutable(true) }
        }

        // Standard dirs
        for (d in listOf("tmp", "var/tmp", "var/cache/apt", "var/lib/apt", "home", "root",
                         "root/.config/pip", "root/workspace")) {
            File(rootfs, d).mkdirs()
        }
        File(rootfs, "tmp").setWritable(true, false)

        // apt config
        File(rootfs, "etc/apt/apt.conf.d").mkdirs()
        File(rootfs, "etc/apt/apt.conf.d/99-agent-optimizations").writeText("""
APT::Get::Assume-Yes "true";
APT::Get::Show-Upgraded "false";
APT::Install-Recommends "false";
APT::Install-Suggests "false";
APT::Quiet "2";
APT::Periodic::Update-Package-Lists "0";
APT::Periodic::Unattended-Upgrade "0";
Acquire::Retries "3";
Acquire::https::Timeout "30";
Acquire::http::Timeout "30";
APT::Sandbox::User "root";
DPkg::Options {"--force-unsafe-io";};
""".trimIndent() + "\n")

        // dpkg config
        File(rootfs, "etc/dpkg/dpkg.cfg.d").mkdirs()
        File(rootfs, "etc/dpkg/dpkg.cfg.d/PaxHeaders").let { if (it.exists()) it.deleteRecursively() }
        File(rootfs, "etc/dpkg/dpkg.cfg.d/99-agent-optimizations").writeText(
            "force-unsafe-io\nforce-confdef\nforce-confold\nno-debsig\n")

        // bashrc
        File(rootfs, "root/.bashrc").writeText("""
export TERM=xterm-256color
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
export COLORTERM=truecolor
export DEBIAN_FRONTEND=noninteractive
export PS1='\u@mcpshell:\w$ '
export HISTSIZE=0
export HISTFILESIZE=0
alias ls='ls --color=auto'
alias ll='ls -lah'
alias apt='apt -qq'
""".trimIndent() + "\n")

        // profile
        File(rootfs, "root/.profile").writeText("""
export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
export DEBIAN_FRONTEND=noninteractive
""".trimIndent() + "\n")

        // profile.d
        File(rootfs, "etc/profile.d").mkdirs()
        File(rootfs, "etc/profile.d/mcpshell.sh").writeText("""
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
export HOME=/root
export TERM=xterm-256color
export TMPDIR=/tmp
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
""".trimIndent() + "\n")

        // Tool configs
        File(rootfs, "root/.inputrc").writeText(
            "set editing-mode emacs\nset bell-style none\nset completion-ignore-case on\nTAB: complete\n")
        File(rootfs, "root/.wgetrc").writeText("quiet=on\ntimeout=30\ntries=3\n")
        File(rootfs, "root/.curlrc").writeText("--silent\n--fail\n--connect-timeout 30\n--max-time 120\n--retry 3\n")
        File(rootfs, "root/.gitconfig").writeText(
            "[core]\n\tpager = cat\n[http]\n\tsslVerify = false\n[advice]\n\tdetachedHead = false\n")
        File(rootfs, "root/.config/pip").mkdirs()
        File(rootfs, "root/.config/pip/pip.conf").writeText(
            "[global]\nno-cache-dir = true\nquiet = 1\nprogress-bar = off\n")
        File(rootfs, "root/.npmrc").writeText("progress=false\nloglevel=error\n")
        File(rootfs, "root/.nanorc").writeText("set autoindent\nset linenumbers\nset nobackup\n")

        // Proot stubs
        val stubScript = "#!/bin/sh\nexit 0\n"
        val stubTargets = listOf(
            "sbin/ldconfig", "usr/sbin/ldconfig", "usr/bin/ldconfig",
            "usr/sbin/invoke-rc.d", "sbin/start-stop-daemon", "usr/sbin/start-stop-daemon"
        )
        for (rel in stubTargets) {
            val f = File(rootfs, rel)
            f.parentFile?.mkdirs()
            if (f.exists() || rel.startsWith("sbin/") || rel.startsWith("usr/sbin/")) {
                val bak = File(f.parent, f.name + ".real")
                if (f.exists() && !bak.exists()) {
                    val content = try { f.readText() } catch (_: Exception) { "" }
                    if (content != stubScript) f.copyTo(bak)
                }
                f.writeText(stubScript)
                f.setExecutable(true)
            }
        }

        // Android GIDs
        val groupFile = File(rootfs, "etc/group")
        if (groupFile.exists()) {
            val existing = groupFile.readText()
            val knownGids = mapOf(
                1000 to "system", 1001 to "radio", 1002 to "bluetooth", 1003 to "graphics",
                1004 to "input", 1005 to "audio", 1006 to "camera", 1007 to "log",
                1008 to "compass", 1009 to "mount", 1010 to "wifi", 1011 to "adb",
                1012 to "install", 1013 to "media", 1014 to "dhcp", 1015 to "sdcard_rw",
                1016 to "vpn", 1017 to "keystore", 1018 to "usb", 1019 to "drm",
                1020 to "mdnsr", 1021 to "gps", 1023 to "media_rw", 1024 to "mtp",
                1026 to "drmrpc", 1027 to "nfc", 1028 to "sdcard_r", 1029 to "clat",
                2000 to "shell", 2001 to "cache", 2002 to "diag",
                3001 to "aid_net_bt_admin", 3002 to "aid_net_bt",
                3003 to "aid_inet", 3004 to "aid_net_raw", 3005 to "aid_net_admin",
                3006 to "aid_net_bw_stats", 3007 to "aid_net_bw_acct",
                3008 to "aid_readproc", 3009 to "aid_wakelock",
                9997 to "aid_everybody", 9998 to "aid_misc", 9999 to "aid_nobody"
            )
            val toAdd = knownGids.filter { (gid, _) -> ":$gid:" !in existing }
                .map { (gid, name) -> "$name:x:$gid:" }
            try {
                val uid = android.os.Process.myUid()
                val dynamicGids = listOf(
                    "u0_a${uid - 10000}:x:$uid:",
                    "all_a${uid - 10000}:x:${uid + 10000}:",
                    "cache_$uid:x:${uid + 40000}:"
                ).filter { entry -> ":${entry.split(":")[2]}:" !in existing }
                val allNew = toAdd + dynamicGids
                if (allNew.isNotEmpty()) groupFile.appendText(allNew.joinToString("\n", postfix = "\n"))
            } catch (e: Exception) {
                if (toAdd.isNotEmpty()) groupFile.appendText(toAdd.joinToString("\n", postfix = "\n"))
            }
        }

        log("Rootfs patched (DNS, stubs, agent defaults, GIDs)")
    }
}
