package coffeebot.commands

import coffeebot.message.Valid
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import java.net.MalformedURLException
import java.net.URL

class MiltonClient {
    private val httpClient = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 25_000
            socketTimeoutMillis = 25_000
        }
    }
    private val miltonHost = "https://milton.terbium.io"

    fun index(url: URL): Boolean {
        val result = runBlocking(Dispatchers.IO) {
            httpClient.post<HttpResponse>("$miltonHost/save?url=${url}")
        }
        return result.status == HttpStatusCode.OK
    }
}

private val miltonClient = MiltonClient()
private val urlRegex = "https?://[\\w\\d:#@%/;$()~_?+-=\\\\.&]*".toRegex(RegexOption.IGNORE_CASE)

val milton = PassiveCommand { message ->
    val matches = urlRegex.findAll(message.contents).toList()
    val results = matches.map { match ->
        val urlValue = match.value
        val url = try {
            URL(urlValue)
        } catch (e: MalformedURLException) {
            throw IllegalStateException("invalid url: $urlRegex")
        }
        miltonClient.index(url)
    }
    if (matches.isNotEmpty()) {
        val emoji = if (results.all { it }) "\u2705" else "\u274E"
        message.react(emoji)
    }
}
