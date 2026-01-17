package xyz.block.buildersyndicate.core.posts

interface MarkdownRenderer {
  fun render(markdown: String): String
}
