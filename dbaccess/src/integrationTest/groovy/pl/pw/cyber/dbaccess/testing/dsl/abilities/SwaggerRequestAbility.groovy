package pl.pw.cyber.dbaccess.testing.dsl.abilities

trait SwaggerRequestAbility implements MakeRequestAbility {

    String getSwaggerHtmlPage() {
        requestedBy("user")
                .withUrl("/swagger-ui.html")
                .makeRequestForHtml()
    }
}
