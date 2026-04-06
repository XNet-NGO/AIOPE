package com.aiope2.core.network

object ProviderDefaults {
  val providers = listOf(
    LlmProvider("Pollinations", "https://text.pollinations.ai/v1/", defaultModel = "openai-fast"),
    LlmProvider("GitHub Models", "https://models.github.ai/inference/", defaultModel = "gpt-4o-mini"),
    LlmProvider("Google AI Studio", "https://generativelanguage.googleapis.com/v1beta/openai/", defaultModel = "gemini-2.0-flash"),
    LlmProvider("OpenRouter", "https://openrouter.ai/api/v1/", defaultModel = "deepseek/deepseek-chat-v3-0324:free"),
    LlmProvider("Cloudflare AI", "https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/v1/", defaultModel = "@cf/meta/llama-3.1-8b-instruct"),
    LlmProvider("Cohere", "https://api.cohere.ai/compatibility/v1/", defaultModel = "command-r"),
    LlmProvider("Cline", "https://api.cline.bot/api/v1/", defaultModel = "claude-3.5-sonnet"),
    LlmProvider("OpenAI", "https://api.openai.com/v1/", defaultModel = "gpt-4o-mini"),
    LlmProvider("Anthropic", "https://api.anthropic.com/v1/", defaultModel = "claude-sonnet-4-20250514"),
    LlmProvider("DeepSeek", "https://api.deepseek.com/v1/", defaultModel = "deepseek-chat"),
  )
}
