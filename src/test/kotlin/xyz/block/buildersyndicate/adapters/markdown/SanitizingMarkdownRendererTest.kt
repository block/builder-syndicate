package xyz.block.buildersyndicate.adapters.markdown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SanitizingMarkdownRendererTest {

  private val renderer = SanitizingMarkdownRenderer()

  @Test
  fun `renders markdown to HTML`() {
    val html = renderer.render("Hello **world**")

    assertEquals("<p>Hello <strong>world</strong></p>\n", html)
  }

  @Test
  fun `strips script tags from output`() {
    val html = renderer.render("Hello <script>alert('xss')</script> world")

    assertFalse(html.contains("<script>"))
    assertFalse(html.contains("alert"))
    assertTrue(html.contains("Hello"))
    assertTrue(html.contains("world"))
  }

  @Test
  fun `strips inline script handlers`() {
    val html = renderer.render("<img src=\"x\" onerror=\"alert('xss')\">")

    assertFalse(html.contains("onerror"))
    assertFalse(html.contains("alert"))
  }

  @Test
  fun `allows safe markdown elements`() {
    val markdown = """
            # Header
            
            - Item 1
            - Item 2
            
            **bold** and *italic*
            
            [link](https://example.com)
    """.trimIndent()

    val html = renderer.render(markdown)

    assertTrue(html.contains("<h1>"))
    assertTrue(html.contains("<li>"))
    assertTrue(html.contains("<strong>"))
    assertTrue(html.contains("<em>"))
    assertTrue(html.contains("<a "))
    assertTrue(html.contains("href=\"https://example.com\""))
  }

  @Test
  fun `strips javascript URLs`() {
    val html = renderer.render("[click me](javascript:alert('xss'))")

    assertFalse(html.contains("javascript:"))
    assertFalse(html.contains("alert"))
  }

  @Test
  fun `adds nofollow to links`() {
    val html = renderer.render("[link](https://example.com)")

    assertTrue(html.contains("rel=\"nofollow\""))
  }
}
