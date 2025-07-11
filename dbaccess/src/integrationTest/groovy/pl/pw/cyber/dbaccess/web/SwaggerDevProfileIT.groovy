package pl.pw.cyber.dbaccess.web


import pl.pw.cyber.dbaccess.testing.DevBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.SwaggerRequestAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.HtmlAssertion.hasHtml

class SwaggerDevProfileIT extends DevBaseIT implements
        AddExampleUserAbility,
        SwaggerRequestAbility {

    def "swagger should be available for dev profile"() {
        when:
            def response = requestedBy("user")
                    .withUrl("/swagger-ui.html")
                    .makeRequestForHtml()

        then:
            hasHtml(response.body) {
                lang("en")
                head {
                    meta(charset: "UTF-8")
                    title("Swagger UI")
                    link(rel: "stylesheet", type: "text/css", href: "./swagger-ui.css")
                    link(rel: "stylesheet", type: "text/css", href: "index.css")
                    link(rel: "icon", type: "image/png", href: "./favicon-32x32.png", sizes: "32x32")
                    link(rel: "icon", type: "image/png", href: "./favicon-16x16.png", sizes: "16x16")
                }
                body {
                    div(id: "swagger-ui")
                    script(src: "./swagger-ui-bundle.js", charset: "UTF-8")
                    script(src: "./swagger-ui-standalone-preset.js", charset: "UTF-8")
                    script(src: "./swagger-initializer.js", charset: "UTF-8")
                }
            }
    }

    def "swagger should not be rate-limited"() {
        given:
            def requester = requestedBy("swagger-user")

        when:
            (1..10).each {
                def response = requester
                        .withUrl("/swagger-ui/index.html")
                        .makeRequestForHtml()

                assert response.statusCode.value() == 200
            }

        then:
            def finalResponse = requester
                    .withUrl("/swagger-ui/index.html")
                    .makeRequestForHtml()

            assert finalResponse.statusCode.value() == 200
    }

}
