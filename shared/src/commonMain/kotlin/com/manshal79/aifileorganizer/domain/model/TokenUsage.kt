package com.manshal79.aifileorganizer.domain.model

/**
 * Tokens consumed by a single LLM call, as reported by the provider.
 *
 * Ollama returns these as `prompt_eval_count` / `eval_count`, which Koog surfaces on
 * `Message.Assistant.metaInfo`. They are nullable there — a provider that reports no
 * usage lands here as zero rather than inflating a running total with a guess.
 */
data class TokenUsage(
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
) {
    val totalTokens: Int
        get() = inputTokens + outputTokens

    operator fun plus(other: TokenUsage): TokenUsage = TokenUsage(
        inputTokens = inputTokens + other.inputTokens,
        outputTokens = outputTokens + other.outputTokens,
    )

    companion object {
        val Zero = TokenUsage()
    }
}
