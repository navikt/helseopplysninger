package questionnaire

import java.net.URL

data class Config(val github: Github) {
    data class Github(val api: Api) {
        data class Api(val url: URL, val repo: String)
    }
}
