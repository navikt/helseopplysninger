package e2e

data class Config(val api: Api) {
    data class Api(val hops: Hops)
    data class App(val host: String)
    data class Hops(
        val api: App,
        val eventreplaykafka: App,
        val eventsinkkafka: App,
        val eventstore: App,
        val fileshare: App,
    )
}
