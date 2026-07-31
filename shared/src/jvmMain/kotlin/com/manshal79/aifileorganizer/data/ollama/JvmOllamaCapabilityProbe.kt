package com.manshal79.aifileorganizer.data.ollama

import ai.koog.prompt.executor.ollama.client.OllamaClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class JvmOllamaCapabilityProbe(
    private val baseUrl: String = OllamaClient.DEFAULT_BASE_URL,
) : OllamaCapabilityProbe {

    // One client for the whole app lifetime — it owns a connection pool and an executor,
    // both wasted if rebuilt per request.
    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun capabilities(modelName: String): Set<String> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/$SHOW_MODEL_PATH"))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(ShowRequest(modelName))))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != HTTP_OK) {
            error("Ollama /api/show returned ${response.statusCode()} for $modelName")
        }
        json.decodeFromString<ShowResponse>(response.body()).capabilities
            .map { it.lowercase() }
            .toSet()
    }

    @Serializable
    private data class ShowRequest(@SerialName("model") val model: String)

    @Serializable
    private data class ShowResponse(val capabilities: List<String> = emptyList())

    private companion object {
        const val SHOW_MODEL_PATH = "api/show"
        const val HTTP_OK = 200
        val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(3)
        val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(10)
    }
}
