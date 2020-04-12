package coffeebot.commands

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
            requestTimeoutMillis = 45_000
            socketTimeoutMillis = 45_000
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
private val indexRegex = "!index (.*)".toRegex()

val miltonIndex = Command("!index", "Index an article to https://milton.terbium.io/") { valid ->
    val match = indexRegex.matchEntire(valid.contents.trim())
            ?: throw IllegalStateException("clearly this should never happen")
    val urlValue = match.groups[1]!!.value
    val url = try {
        URL(urlValue)
    } catch (e: MalformedURLException) {
        valid.reply("invalid URL: $urlValue")
        return@Command
    }
    miltonClient.index(url)
}
