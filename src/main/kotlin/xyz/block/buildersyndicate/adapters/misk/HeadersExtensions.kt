package xyz.block.buildersyndicate.adapters.misk

import okhttp3.Headers

/**
 * Creates [Headers] from key-value pairs.
 *
 * Example:
 * ```
 * headersOf("Content-Type" to "application/json", "Accept" to "text/plain")
 * ```
 */
fun headersOf(vararg pairs: Pair<String, String>): Headers =
  Headers.headersOf(*pairs.flatMap { listOf(it.first, it.second) }.toTypedArray())
