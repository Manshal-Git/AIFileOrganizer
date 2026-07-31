package com.manshal79.aifileorganizer.domain.model

/**
 * The model answered, but not in the JSON shape the prompt asked for — prose, a partial object,
 * a stray `<think>` block, whatever. Distinct from a transport failure: the request worked and
 * the tokens were spent, so retrying the same model unchanged tends to fail the same way.
 *
 * The malformed output itself goes to the log, not into [message] — [message] is read by a user.
 */
class ModelOutputException(
    val modelId: String,
    cause: Throwable,
) : Exception("$modelId didn't answer in the expected format", cause)
