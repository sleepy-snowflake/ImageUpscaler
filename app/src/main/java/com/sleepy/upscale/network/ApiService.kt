package com.sleepy.upscale.network

import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class UpscaleConfig(
    val token: String,
    val taskId: String,
    val server: String,
)

object ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"

    private val configPattern = Pattern.compile(
        "ilovepdfConfig\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL
    )
    private val taskPattern = Pattern.compile(
        "ilovepdfConfig\\.taskId\\s*=\\s*['\"]([^'\"]+)['\"]"
    )

    fun getConfig(): UpscaleConfig {
        val request = Request.Builder()
            .url("https://www.iloveimg.com/upscale-image")
            .header("User-Agent", UA)
            .build()

        val response = client.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("Config request failed: HTTP ${resp.code}")
            }
            val html = resp.body?.string() ?: throw IOException("Failed to fetch config page")

            val configMatcher = configPattern.matcher(html)
            if (!configMatcher.find()) throw IOException("Could not find config in page")
            val json = JSONObject(configMatcher.group(1)!!)

            val token = json.getString("token")
            val server = json.getJSONArray("servers").getString(0)!!

            val taskMatcher = taskPattern.matcher(html)
            if (!taskMatcher.find()) throw IOException("Could not find taskId")
            val taskId = taskMatcher.group(1)!!

            return UpscaleConfig(token = token, taskId = taskId, server = "https://$server.iloveimg.com/v1")
        }
    }

    fun uploadImage(config: UpscaleConfig, imageData: ByteArray, fileName: String): String {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("task", config.taskId)
            .addFormDataPart("file", fileName, imageData.toRequestBody("application/octet-stream".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("${config.server}/upload")
            .header("User-Agent", UA)
            .header("Authorization", "Bearer ${config.token}")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                val errorBody = resp.body?.string() ?: "unknown"
                throw IOException("Upload failed: HTTP ${resp.code} - $errorBody")
            }
            val bodyStr = resp.body?.string() ?: throw IOException("Empty upload response")
            val json = JSONObject(bodyStr)
            return json.getString("server_filename")
        }
    }

    fun upscaleImage(
        config: UpscaleConfig,
        serverFilename: String,
        scale: Int,
        onProgress: (Long, Long) -> Unit,
    ): ByteArray {
        val formBody = FormBody.Builder()
            .add("task", config.taskId)
            .add("server_filename", serverFilename)
            .add("scale", scale.toString())
            .build()

        val request = Request.Builder()
            .url("${config.server}/upscale")
            .header("User-Agent", UA)
            .header("Authorization", "Bearer ${config.token}")
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                val errorBody = resp.body?.string() ?: "unknown"
                throw IOException("Upscale failed: HTTP ${resp.code} - $errorBody")
            }
            val body = resp.body ?: throw IOException("Empty upscale response")
            val contentLength = body.contentLength()

            val bytes = body.byteStream().use { input ->
                val buffer = java.io.ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                var total = 0L
                var read: Int
                while (input.read(chunk).also { read = it } != -1) {
                    buffer.write(chunk, 0, read)
                    total += read
                    if (contentLength > 0) {
                        onProgress(total, contentLength)
                    }
                }
                buffer.toByteArray()
            }

            if (bytes.isEmpty()) throw IOException("Empty response from upscale")
            return bytes
        }
    }
}
