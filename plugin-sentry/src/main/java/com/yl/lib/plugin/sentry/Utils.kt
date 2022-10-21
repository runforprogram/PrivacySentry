package com.yl.lib.plugin.sentry

import com.android.build.api.variant.VariantInfo
import com.yl.lib.plugin.sentry.extension.PrivacyExtension
import org.gradle.api.logging.Logger
import java.io.File

object Utils {
    @JvmStatic
    fun isApply(variant: VariantInfo?, privacyExtension: PrivacyExtension): Boolean {
        return if (variant == null) {
            false
        } else {
            variant.buildTypeName == "release" || privacyExtension.buildTypeDebugEnable
        }
    }
    @JvmStatic
    fun isSign(file:File,logger:Logger):Boolean{
        val command= ("jarsigner -verify " +file.absolutePath)
        logger.info("command=$command")
        val signCheck=command.runCommand(file)
        if (signCheck == null) {
            logger.info("$command run error******************")
            return false
        }
        logger.info("sign check =  $signCheck")
        val signed=signCheck.contains("已验证")
        return signed
    }
}