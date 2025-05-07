package pl.pw.cyber.dbaccess.testing.dsl.assertions

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class HtmlAssertion {

    static void hasHtml(String html, @DelegatesTo(HtmlDsl) Closure<?> spec) {
        Document doc = Jsoup.parse(html)
        HtmlDsl dsl = new HtmlDsl(doc)
        spec.delegate = dsl
        spec.resolveStrategy = Closure.DELEGATE_FIRST
        spec.call()
        dsl.verify()
    }

    static class HtmlDsl {
        private final Document doc
        private Closure<?> headSpec
        private Closure<?> bodySpec
        private String expectedLang

        HtmlDsl(Document doc) {
            this.doc = doc
        }

        void lang(String lang) {
            this.expectedLang = lang
        }

        void head(@DelegatesTo(ElementDsl) Closure<?> closure) {
            this.headSpec = closure
        }

        void body(@DelegatesTo(ElementDsl) Closure<?> closure) {
            this.bodySpec = closure
        }

        void verify() {
            Element html = doc.selectFirst("html")
            assert html != null : "<html> tag not found"
            if (expectedLang) {
                assert html.attr("lang") == expectedLang : "Expected lang='${expectedLang}', got '${html.attr("lang")}'"
            }

            if (headSpec) {
                Element head = doc.head()
                def dsl = new ElementDsl(head)
                headSpec.delegate = dsl
                headSpec.resolveStrategy = Closure.DELEGATE_FIRST
                headSpec.call()
                dsl.verify()
            }

            if (bodySpec) {
                Element body = doc.body()
                def dsl = new ElementDsl(body)
                bodySpec.delegate = dsl
                bodySpec.resolveStrategy = Closure.DELEGATE_FIRST
                bodySpec.call()
                dsl.verify()
            }
        }
    }

    static class ElementDsl {
        private final Element element
        private final List<ExpectedElement> expectedElements = []

        ElementDsl(Element element) {
            this.element = element
        }

        void meta(Map<String, String> attrs) { expectedElements << new ExpectedElement("meta", attrs) }
        void title(String text) { expectedElements << new ExpectedElement("title", [:], text) }
        void link(Map<String, String> attrs) { expectedElements << new ExpectedElement("link", attrs) }
        void script(Map<String, String> attrs) { expectedElements << new ExpectedElement("script", attrs) }
        void div(Map<String, String> attrs) { expectedElements << new ExpectedElement("div", attrs) }

        void verify() {
            expectedElements.each { expected ->
                def found = element.children().find {
                    it.tagName() == expected.tag &&
                            expected.attrs.every { k, v -> it.attr(k) == v } &&
                            (expected.text == null || it.text().trim() == expected.text.trim())
                }
                assert found : "Expected <${expected.tag}> with ${expected.attrs}, text='${expected.text}' not found in:\n${element}"
            }
        }
    }

    private static class ExpectedElement {
        String tag
        Map<String, String> attrs
        String text

        ExpectedElement(String tag, Map<String, String> attrs = [:], String text = null) {
            this.tag = tag
            this.attrs = attrs
            this.text = text
        }
    }
}

