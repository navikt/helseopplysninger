package e2e

data class Config(val api: Api) {
    data class Api(
        val github: BaseUrl,
        val hops: Hops,
    )

    data class Hops(
        val api: HopsService,
        val eventreplaykafka: HopsService,
        val eventsinkkafka: HopsService,
        val eventstore: HopsService,
        val fileshare: HopsService,
        val testExternal: HopsService,
    )

    data class BaseUrl(val baseUrl: String)
    data class HopsService(val host: String)
}
