package cl.ipvg.docentecalma.ai

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

internal class GeminiRestClient(
    private val apiKey: String
) {
    fun listModelsSupportingGenerateContent(pageSize: Int = 200): List<String> {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey&pageSize=$pageSize")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
        }

        val code = conn.responseCode
        val body = conn.readBody(code)
        if (code !in 200..299) {
            throw GeminiHttpException(code = code, body = body)
        }

        val json = JSONObject(body)
        val models = json.optJSONArray("models") ?: JSONArray()

        val out = ArrayList<String>(models.length())
        for (i in 0 until models.length()) {
            val m = models.optJSONObject(i) ?: continue
            val name = m.optString("name").takeIf { it.isNotBlank() } ?: continue
            val supported = m.optJSONArray("supportedGenerationMethods") ?: JSONArray()
            var supportsGenerateContent = false
            for (j in 0 until supported.length()) {
                if (supported.optString(j) == "generateContent") {
                    supportsGenerateContent = true
                    break
                }
            }
            if (supportsGenerateContent) out.add(name) // e.g. "models/gemini-2.0-flash"
        }
        return out
    }

    fun generateContent(
        modelName: String,
        systemInstruction: String,
        contents: List<GeminiContent>
    ): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/$modelName:generateContent?key=$apiKey")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connectTimeout = 15_000
            readTimeout = 60_000
        }

        val requestJson = JSONObject().apply {
            put(
                "system_instruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemInstruction))
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", AiConfig.GENERATION_TEMPERATURE)
                    put("topP", AiConfig.GENERATION_TOP_P)
                    put("topK", AiConfig.GENERATION_TOP_K)
                    put("maxOutputTokens", AiConfig.GENERATION_MAX_OUTPUT_TOKENS)
                }
            )
            put(
                "contents",
                JSONArray().apply {
                    contents.forEach { c ->
                        put(
                            JSONObject().apply {
                                put("role", c.role)
                                put(
                                    "parts",
                                    JSONArray().put(JSONObject().put("text", c.text))
                                )
                            }
                        )
                    }
                }
            )
        }

        conn.outputStream.use { os ->
            os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
        }

        val code = conn.responseCode
        val body = conn.readBody(code)
        if (code !in 200..299) {
            throw GeminiHttpException(code = code, body = body)
        }

        val json = JSONObject(body)
        val candidates = json.optJSONArray("candidates") ?: JSONArray()
        val first = candidates.optJSONObject(0)

        if (first == null) {
            val blockReason = json.optJSONObject("promptFeedback")
                ?.optString("blockReason")
                ?.takeIf { it.isNotBlank() }
            if (blockReason != null) {
                throw GeminiContentBlockedException(reason = blockReason, bodySnippet = body)
            }
            return ""
        }

        val finishReason = first.optString("finishReason").takeIf { it.isNotBlank() }
        if (finishReason != null && finishReason != "STOP" && finishReason != "MAX_TOKENS") {
            if (finishReason == "SAFETY" || finishReason == "BLOCKLIST" || finishReason == "PROHIBITED_CONTENT") {
                throw GeminiContentBlockedException(reason = finishReason, bodySnippet = body)
            }
        }

        val content = first.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: JSONArray()

        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val p = parts.optJSONObject(i) ?: continue
            val t = p.optString("text")
            if (t.isNotBlank()) {
                if (sb.isNotEmpty()) sb.append("\n")
                sb.append(t)
            }
        }
        val text = sb.toString()
        if (text.isBlank()) {
            val pfBlock = json.optJSONObject("promptFeedback")
                ?.optString("blockReason")
                ?.takeIf { it.isNotBlank() }
            if (pfBlock != null) {
                throw GeminiContentBlockedException(reason = pfBlock, bodySnippet = body)
            }
        }
        return text
    }

    private fun HttpURLConnection.readBody(code: Int): String {
        val stream = if (code in 200..299) inputStream else errorStream
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }
}

internal data class GeminiContent(
    val role: String, // "user" | "model"
    val text: String
)

internal class GeminiHttpException(
    val code: Int,
    val body: String
) : Exception("HTTP $code: $body")

/**
 * La API respondió 200 pero bloqueó o no entregó texto por políticas del modelo
 * ([finishReason], [promptFeedback.blockReason], etc.).
 */
internal class GeminiContentBlockedException(
    val reason: String,
    val bodySnippet: String
) : Exception("Gemini blocked: $reason")

