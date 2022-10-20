package com.yl.lib.plugin.sentry

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.yl.lib.plugin.sentry.extension.PrivacyExtension
import com.yl.lib.plugin.sentry.transform.PrivacyCollectTransform
import com.yl.lib.plugin.sentry.transform.PrivacySentryTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author yulun
 * @sinice 2021-12-13 17:05
 */
class PrivacySentryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        //只在application下生效
        if (!target.plugins.hasPlugin("com.android.application")) {
            return
        }
        val extensionConfig = target.extensions.create("privacy", PrivacyExtension::class.java)
        var android = target.extensions.getByType(AppExtension::class.java)
//        android.buildTypes.forEach {
//            println(it.name + "privacy" + it.isDebuggable)
//        }
//        android.productFlavors.forEach {
//            println(it.name + "productFlavors")
//        }
        // 收集注解信息的任务
        android.registerTransform(PrivacyCollectTransform(target.logger, extensionConfig))
        // 执行字节码替换的任务
        android.registerTransform(
            PrivacySentryTransform(
                target.logger,
                extensionConfig,
                target.buildDir.absolutePath
            )
        )
//        androidComponents.onVariants { variant ->
//            if (variant.buildType.equals("release") || extensionConfig.isDebugEnable) {
//                println("xxxxxxxxxxxxxxxxxxxxxxx,${variant.name}")
//                var android = target.extensions.getByType(AppExtension::class.java)
//            } else {
//                target.logger.info("releae才应用此插件，如果需要在debug应用，在privacy中添加 isDebugEnable = true")
//            }
//            variant.instrumentation.transformClassesWith(TimeCostTransform::class.java,
//                InstrumentationScope.PROJECT) {}
//            variant.instrumentation.setAsmFramesComputationMode(
//                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
//            )
//        }

    }
}