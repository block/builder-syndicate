package xyz.block.buildersyndicate.app

import misk.config.Config
import misk.metrics.backends.prometheus.PrometheusConfig
import misk.web.WebConfig

data class BuilderSyndicateConfig(
    val web: WebConfig,
    val prometheus: PrometheusConfig
) : Config
