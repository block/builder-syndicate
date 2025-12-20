package xyz.block.buildersyndicate.app

import misk.MiskApplication
import misk.MiskRealServiceModule
import misk.config.ConfigModule
import misk.config.MiskConfig
import misk.environment.DeploymentModule
import misk.metrics.backends.prometheus.PrometheusMetricsServiceModule
import misk.web.MiskWebModule
import wisp.deployment.Deployment

fun main(args: Array<String>) {
    val deployment = Deployment(name = "buildersyndicate", isLocalDevelopment = true)
    val config = MiskConfig.load<BuilderSyndicateConfig>("buildersyndicate", deployment)

    MiskApplication(
        MiskRealServiceModule(),
        MiskWebModule(config.web),
        ConfigModule.create("buildersyndicate", config),
        DeploymentModule(deployment),
        PrometheusMetricsServiceModule(config.prometheus),
        BuilderSyndicateModule()
    ).run(args)
}
