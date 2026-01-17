package xyz.block.buildersyndicate.adapters.markdown

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import xyz.block.buildersyndicate.core.posts.MarkdownRenderer

class SanitizingMarkdownRenderer : MarkdownRenderer {
  private val parser: Parser = Parser.builder().build()
  private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

  private val policy: PolicyFactory = HtmlPolicyBuilder()
    .allowElements(
      "p", "br", "hr",
      "h1", "h2", "h3", "h4", "h5", "h6",
      "ul", "ol", "li",
      "blockquote", "pre", "code",
      "strong", "em", "b", "i", "u", "s", "del",
      "a", "img",
      "table", "thead", "tbody", "tr", "th", "td",
    )
    .allowAttributes("href").onElements("a")
    .allowAttributes("src", "alt", "title").onElements("img")
    .allowUrlProtocols("http", "https", "mailto")
    .requireRelNofollowOnLinks()
    .toFactory()

  override fun render(markdown: String): String {
    val document = parser.parse(markdown)
    val rawHtml = renderer.render(document)
    return policy.sanitize(rawHtml)
  }
}
