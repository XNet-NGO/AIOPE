package com.aiope2.core.network

object ProviderRegistry {

  val ALL: List<LlmProvider> = listOf(

    // ── Free / no-auth ──────────────────────────────────────────────────
    LlmProvider("pollinations", "Pollinations", "🌸",
      baseUrl = "https://text.pollinations.ai/openai",
      apiKeyHint = "(no key needed)", requiresApiKey = false,
      defaultModels = listOf(
        ModelDef("openai-fast", "GPT-OSS 20B Reasoning", 32_768, true, false, true),
        ModelDef("openai", "GPT-OSS 20B", 32_768),
        ModelDef("openai-large", "GPT-OSS 20B Large", 32_768),
      )),

    LlmProvider("cline", "Cline", "🤖",
      baseUrl = "https://api.cline.bot/api/v1",
      apiKeyHint = "(no key needed)", requiresApiKey = false,
      defaultModels = listOf(
        ModelDef("kwaipilot/kat-coder-pro", "Kat Coder Pro", 32_768),
        ModelDef("minimax/minimax-m2.5", "MiniMax M2.5", 32_768),
        ModelDef("z-ai/glm-5", "GLM-5", 32_768),
      )),

    LlmProvider("zen", "Zen (OpenCode)", "🧘",
      baseUrl = "https://opencode.ai/zen/v1",
      apiKeyHint = "(no key needed)", requiresApiKey = false,
      defaultModels = listOf(
        ModelDef("anthropic/claude-sonnet-4-20250514", "Claude Sonnet 4", 200_000),
      )),

    // ── Tier 1: major cloud APIs ────────────────────────────────────────
    LlmProvider("openai", "OpenAI", "🤖",
      apiKeyHint = "sk-…",
      defaultModels = listOf(
        ModelDef("gpt-4o", "GPT-4o", 128_000, true, true, false, 2.5, 10.0),
        ModelDef("gpt-4o-mini", "GPT-4o mini", 128_000, true, true, false, 0.15, 0.60),
        ModelDef("o3", "o3", 200_000, true, true, true, 10.0, 40.0),
        ModelDef("o4-mini", "o4-mini", 200_000, true, true, true, 1.1, 4.4),
      )),

    LlmProvider("anthropic", "Anthropic", "🧠",
      apiKeyHint = "sk-ant-…",
      defaultModels = listOf(
        ModelDef("claude-sonnet-4-20250514", "Claude Sonnet 4", 200_000, true, true, false, 3.0, 15.0),
        ModelDef("claude-3-5-haiku-20241022", "Claude 3.5 Haiku", 200_000, true, true, false, 0.8, 4.0),
      )),

    LlmProvider("google_ai_studio", "Google AI Studio", "✨",
      baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
      apiKeyHint = "AIza…", supportsVision = true,
      defaultModels = listOf(
        ModelDef("gemini-2.0-flash", "Gemini 2.0 Flash", 1_000_000, true, true, false, 0.075, 0.30),
        ModelDef("gemini-2.0-flash-lite", "Gemini 2.0 Flash Lite", 1_000_000),
      )),

    LlmProvider("deepseek", "DeepSeek", "🔍",
      apiKeyHint = "sk-…",
      defaultModels = listOf(
        ModelDef("deepseek-chat", "DeepSeek V3", 64_000, true, false, false, 0.27, 1.10),
        ModelDef("deepseek-reasoner", "DeepSeek R1", 64_000, false, false, true, 0.55, 2.19),
      )),

    LlmProvider("xai", "xAI (Grok)", "𝕏",
      apiKeyHint = "xai-…",
      defaultModels = listOf(
        ModelDef("grok-3-latest", "Grok 3", 131_072, true, false, false, 3.0, 15.0),
        ModelDef("grok-3-mini-latest", "Grok 3 Mini", 131_072, true, false, true, 0.3, 0.5),
      )),

    // ── Tier 2: aggregators ─────────────────────────────────────────────
    LlmProvider("openrouter", "OpenRouter", "🔀",
      baseUrl = "https://openrouter.ai/api/v1",
      apiKeyHint = "sk-or-…", supportsVision = true,
      defaultModels = listOf(
        ModelDef("anthropic/claude-sonnet-4-5", "Claude Sonnet 4.5", 200_000, true, true, true),
        ModelDef("openai/gpt-4o", "GPT-4o", 128_000, true, true),
        ModelDef("deepseek/deepseek-r1", "DeepSeek R1", 64_000, false, false, true),
        ModelDef("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash (free)", 1_000_000),
      )),

    LlmProvider("github_models", "GitHub Models", "🐙",
      baseUrl = "https://models.github.ai/inference",
      apiKeyHint = "github_pat_…", supportsVision = true,
      defaultModels = listOf(
        ModelDef("gpt-4o", "GPT-4o", 131_072, true, true),
        ModelDef("gpt-4.1", "GPT-4.1", 1_048_576, true, true),
        ModelDef("o4-mini", "o4-mini", 200_000, true, true, true),
        ModelDef("DeepSeek-R1", "DeepSeek R1", 128_000, false, false, true),
      )),

    // ── Tier 3: specialist ──────────────────────────────────────────────
    LlmProvider("groq", "Groq", "⚡",
      apiKeyHint = "gsk_…",
      defaultModels = listOf(
        ModelDef("llama-3.3-70b-versatile", "Llama 3.3 70B", 128_000),
        ModelDef("llama-3.1-8b-instant", "Llama 3.1 8B", 128_000),
      )),

    LlmProvider("mistral", "Mistral AI", "🌊",
      apiKeyHint = "…",
      defaultModels = listOf(
        ModelDef("mistral-large-latest", "Mistral Large", 128_000),
        ModelDef("codestral-latest", "Codestral", 256_000),
      )),

    LlmProvider("cohere", "Cohere", "🌀",
      baseUrl = "https://api.cohere.ai/compatibility/v1",
      apiKeyHint = "…",
      defaultModels = listOf(
        ModelDef("command-a-03-2025", "Command A", 256_000),
        ModelDef("command-r-08-2024", "Command R", 128_000),
      )),

    LlmProvider("together_ai", "Together AI", "🤝",
      apiKeyHint = "…",
      defaultModels = listOf(
        ModelDef("meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo", "Llama 3.1 70B Turbo", 131_072),
        ModelDef("deepseek-ai/DeepSeek-V3", "DeepSeek V3", 128_000),
      )),

    LlmProvider("fireworks_ai", "Fireworks AI", "🎆",
      baseUrl = "https://api.fireworks.ai/inference/v1",
      apiKeyHint = "fw_…",
      defaultModels = listOf(
        ModelDef("accounts/fireworks/models/llama-v3p1-70b-instruct", "Llama 3.1 70B", 131_072),
        ModelDef("accounts/fireworks/models/deepseek-r1", "DeepSeek R1", 163_840, false, false, true),
      )),

    LlmProvider("cerebras", "Cerebras", "🧬",
      apiKeyHint = "csk-…",
      defaultModels = listOf(
        ModelDef("llama-3.3-70b", "Llama 3.3 70B", 128_000),
        ModelDef("qwen-3-32b", "Qwen 3 32B", 128_000),
      )),

    LlmProvider("sambanova", "SambaNova", "🍎",
      apiKeyHint = "…",
      defaultModels = listOf(
        ModelDef("DeepSeek-R1", "DeepSeek R1", 131_072, false, false, true),
        ModelDef("Meta-Llama-3.3-70B-Instruct", "Llama 3.3 70B", 128_000),
      )),

    // ── AWS Bedrock ─────────────────────────────────────────────────────
    LlmProvider("aws_bedrock", "AWS Bedrock (Mantle)", "🏗",
      baseUrl = "https://bedrock-mantle.us-east-1.api.aws/v1",
      apiKeyHint = "Bedrock Mantle API key", supportsVision = true,
      defaultModels = listOf(
        ModelDef("us.anthropic.claude-sonnet-4-5-20250929-v1:0", "Claude Sonnet 4.5 (US)", 200_000, true, true, true, 3.0, 15.0),
        ModelDef("us.anthropic.claude-3-5-haiku-20241022-v1:0", "Claude 3.5 Haiku (US)", 200_000, true, true, false, 0.8, 4.0),
        ModelDef("us.amazon.nova-pro-v1:0", "Nova Pro (US)", 300_000, true, true),
        ModelDef("us.deepseek.r1-v1:0", "DeepSeek R1 (US)", 128_000, false, false, true),
      )),

    // ── Cloudflare ──────────────────────────────────────────────────────
    LlmProvider("cloudflare", "Cloudflare AI", "🌐",
      apiKeyHint = "CF API Token", supportsTools = false,
      defaultModels = listOf(
        ModelDef("@cf/meta/llama-3.3-70b-instruct-fp8-fast", "Llama 3.3 70B Fast", 8_192, false),
      )),

    // ── Local ───────────────────────────────────────────────────────────
    LlmProvider("ollama", "Ollama (local)", "🦙",
      baseUrl = "http://localhost:11434",
      apiKeyHint = "(no key needed)", requiresApiKey = false,
      defaultModels = listOf(
        ModelDef("llama3.2", "Llama 3.2", 128_000),
        ModelDef("qwen2.5", "Qwen 2.5", 128_000),
        ModelDef("deepseek-r1", "DeepSeek R1", 64_000, false, false, true),
      )),

    // ── Custom ──────────────────────────────────────────────────────────
    LlmProvider("custom", "Custom / OpenAI-compat", "⚙️",
      apiKeyHint = "API key (if required)", requiresApiKey = false,
      defaultModels = emptyList()),
  )

  val byId: Map<String, LlmProvider> = ALL.associateBy { it.id }
  fun get(id: String): LlmProvider? = byId[id]
}
