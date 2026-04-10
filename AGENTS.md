# AIOPE Development Loop

## Environment

### Local workstation (serv-2)
- Linux x86_64, Liquorix kernel
- Working directory: `/home/xnet-admin/projects/`
- Two repos:
  - `aiope2/` — Android app (Kotlin, Jetpack Compose, Gradle 9.1)
  - `aiope-gateway/` — Backend gateway (Kotlin, Jetty, Gradle 8.3)
- Docker container `xnet-dev` — Android SDK build environment
  - Mounts `/home/xnet-admin/dev-container/workspace` → `/workspace`
  - Has Android SDK, Gradle, JDK 17
  - Used for `./gradlew :app:assembleDebug`
- ADB connects to phone over WiFi (IP changes, check `adb devices`)

### Production server (inf.xnet.ngo)
- AWS Graviton ARM64 instance
- SSH: `ssh ubuntu@inf.xnet.ngo`
- Working directory: `/workspace/aiope-gateway/`
- Docker containers:
  - `aiope-gateway-blue` — gateway on port 8082
  - `aiope-gateway-green` — gateway on port 8083
  - `caddy-l4` — TLS termination, reverse proxy
  - `xnet-dev` — build container (JDK 17, Gradle), mounts `/workspace`
  - `llama` — local LLM inference
  - `xnet-web` — website on port 3000

---

## Android App Build & Deploy

```bash
# 1. Edit code locally in /home/xnet-admin/projects/aiope2/

# 2. Copy to build container and build
cd /home/xnet-admin/projects
docker exec xnet-dev rm -rf /workspace/aiope2
docker cp aiope2 xnet-dev:/workspace/aiope2
docker exec xnet-dev bash -c "cd /workspace/aiope2 && chmod +x gradlew && ./gradlew :app:assembleDebug"

# 3. Pull APK and install on phone
docker cp xnet-dev:/workspace/aiope2/app/build/outputs/apk/debug/app-debug.apk /home/xnet-admin/projects/aiope2/aiope2-debug.apk
adb -s <DEVICE_IP:PORT> install -r /home/xnet-admin/projects/aiope2/aiope2-debug.apk

# 4. Restart app
adb -s <DEVICE_IP:PORT> shell am force-stop com.aiope2
adb -s <DEVICE_IP:PORT> shell am start -n com.aiope2/.MainActivity

# 5. Commit and push
cd /home/xnet-admin/projects/aiope2
git add -A && git commit -m "description" && git push origin main
```

The phone's ADB address changes between sessions. Always run `adb devices` first to get the current IP:port.

For clean installs (wipes app data):
```bash
adb -s <DEVICE_IP:PORT> uninstall com.aiope2
adb -s <DEVICE_IP:PORT> install aiope2-debug.apk
```

---

## Gateway Build & Deploy (Blue-Green)

The gateway uses zero-downtime blue-green deployment. Blue runs on port 8082, green on 8083. Caddy routes `inf.xnet.ngo` to whichever is live.

### Automated deploy
```bash
ssh ubuntu@inf.xnet.ngo
cd /workspace/aiope-gateway
./deploy.sh
```

`deploy.sh` handles everything:
1. Detects which container is live via Caddy config
2. `git pull origin main`
3. Builds jar in `xnet-dev` container (`./gradlew shadowJar`)
4. Builds Docker image (`Dockerfile.alpine`)
5. Starts idle container on its port
6. Health checks `/v1/data` until ready
7. Switches Caddy to new container
8. Verifies through public URL
9. Stops old container (or rolls back on failure)

### Manual steps (if needed)
```bash
# Build on server
ssh ubuntu@inf.xnet.ngo
cd /workspace/aiope-gateway
git pull origin main
docker exec xnet-dev bash -c "cd /workspace/aiope-gateway && ./gradlew shadowJar"
docker build -f Dockerfile.alpine -t aiope-gateway:alpine .

# Switch Caddy (e.g., 8082 → 8083)
docker exec caddy-l4 sh -c "cat /config/caddy/autosave.json | sed s/8082/8083/g | tee /etc/caddy/caddy.json > /dev/null && caddy reload --config /etc/caddy/caddy.json"
```

### Important notes
- The `xnet-dev` container on the server shares `/workspace` with the host via bind mount
- `git pull` must be run on the **host**, not inside `xnet-dev` (no git credentials in container)
- The build runs inside `xnet-dev` because the host has no JDK
- Caddy config is a bind-mounted JSON file — use `tee` to overwrite (not `cp` or `sed -i`, they fail on bind mounts)
- The gateway's `/v1/data` endpoint (no `q` param) is public — returns available data categories without auth

---

## Code Flow

```
Local edit → docker cp → gradlew build → docker cp APK → adb install → test on phone
                                                                            ↓
                                                                     git push origin main
                                                                            ↓
                                                              ssh → git pull → deploy.sh
                                                                            ↓
                                                              blue-green swap (zero downtime)
```

---

## Repos

- App: `github.com/XNet-NGO/AIOPE` (private)
- Gateway: `github.com/XNet-NGO/aiope-gateway` (private)

## Spotless

The app uses Spotless for Kotlin formatting. If the build fails with `spotlessKotlinCheck` violations, either:
- Fix the formatting manually (trailing commas, single-line params)
- Or run `./gradlew spotlessApply` in the build container
