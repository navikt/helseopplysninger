import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import okhttp3.mockwebserver.MockResponse

object MockServers {
    val oAuth = MockOAuth2Server(
//        OAuth2Config(
//
//        )
    )
    const val gcsFileInfoResponse = """
                    {
                        "bucket": "hops",
                        "name": "file-name",
                        "contentType": "image/png",
                        "contentEncoding": "",
                        "crc32c": "S8lmMw==",
                        "md5Hash": "qKYDeNcopo1BFSYyeKjkbw==",
                        "acl": [
                            {
                                "entity": "projectOwner",
                                "entityId": "",
                                "role": "OWNER",
                                "domain": "",
                                "email": "",
                                "projectTeam": null
                            }
                        ],
                        "created": "2021-08-24T07:57:10.91259Z",
                        "updated": "2021-08-24T07:57:10.912632Z",
                        "deleted": "0001-01-01T00:00:00Z"
                    }
                        """
    val gcs = MockServer().apply {
        matchRequest(
            { request -> request.method == "POST" && request.path?.startsWith("/upload/storage/v1/b") ?: false },
            {
                MockResponse()
                    .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    .setBody(
                        gcsFileInfoResponse.trimIndent()
                    )
            }
        )
        matchRequest(
            { request ->
                request.method == "GET" && request.path?.startsWith("/storage/v1/b") ?: false && request.path?.contains(
                    "?alt=json"
                ) ?: false
            },
            {
                MockResponse()
                    .setResponseCode(HttpStatusCode.NotFound.value)
            }
        )
        matchRequest(
            { request ->
                request.method == "GET" && request.path?.startsWith("/storage/v1/b") ?: false && request.path?.contains(
                    "?alt=media"
                ) ?: false
            },
            {
                MockResponse()
                    .setHeader(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    .setBody("Content")
            }
        )
    }
    val gcpMetadata = MockServer().apply {
        anyRequest {
            MockResponse()
                .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                .setBody("""{"access_token":"ya29.c.KtECDwgryW8_sV7xqm8sc4yNAyQQuT8wXuN3j8wegpTyoNt5HPMWJUfFq5ubF2EWadqHPYFEwybxtmzsNwAxFbYWnwiJ-u1S6F6J7Sdi4x12YBaA07QbsFX_44jAdDixz5t3PocIdu5fMJl2FtP4IyfcVSAz1WE50FMpVJM0JWq1JPuNE2FhWcd4uHDIMdXWEMpblH87Zp9OXlMo8urA0OJJCm-LQ1RAmkCNteOsfgTL7YLkdaxKq9eFz-SlKSo-fd8iBcmQc8aGGXwZoVMcfvBms__f-IZW5Gf4Wmgm2LpokidHfyMq0C--cRydSmKmiq15ltgDHgumCHRp96xqBVjC7m1eDnCYJHJ6iC8DOHD2eorxWWC-AP4UqOF_rlhyTHHx2RI3XRtg9crpc6_YhYMykUnSoHffh8rRkQ7cWTxU76ibU-PA-7GlCUyhFb_VUmTLqg","expires_in":3599,"token_type":"Bearer"}""")
        }
    }
    val virusScanner = MockServer().apply {
        anyRequest {
            MockResponse()
                .setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                .setBody("""[{"result":"OK"}]""")
        }
    }
}
