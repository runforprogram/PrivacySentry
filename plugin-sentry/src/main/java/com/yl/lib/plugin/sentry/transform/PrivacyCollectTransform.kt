package com.yl.lib.plugin.sentry.transform

import com.android.build.api.transform.*
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.yl.lib.plugin.sentry.extension.PrivacyExtension
import com.yl.lib.plugin.sentry.isApply
import com.yl.lib.plugin.sentry.runCommand
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.internal.impldep.software.amazon.ion.impl.PrivateIonConstants.True
import org.gradle.util.GFileUtils
import java.io.File
import kotlin.math.log

/**
 * @author yulun
 * @sinice 2021-12-31 11:36
 * 收集带有 com.yl.lib.privacy_annotation.PrivacyMethodProxy 的方法
 */
class PrivacyCollectTransform : Transform {
    private var logger:Logger 

    private var extension : PrivacyExtension
    constructor(logger: Logger,extension: PrivacyExtension) {
        this.logger =logger
        this.extension=extension
    }

    override fun getName(): String {
        return "PrivacyCollectTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun applyToVariant(variant: VariantInfo?): Boolean {
        return variant.isApply()
    }
    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)

        // 非增量，删掉所有
        if (transformInvocation?.isIncremental == false) {
            transformInvocation.outputProvider.deleteAll()
        }
  
        transformInvocation?.inputs?.forEach {
            handleJar(
                it,
                transformInvocation.outputProvider,
                transformInvocation.isIncremental,
                extension 
            )
            handleDirectory(
                it,
                transformInvocation.outputProvider,
                transformInvocation.isIncremental,extension 
            )
        }
    }


    // 处理jar
    private fun handleJar(
        transformInput: TransformInput, outputProvider: TransformOutputProvider,
        incremental: Boolean,
        extension: PrivacyExtension
    ) {
        transformInput.jarInputs.forEach {
            println("outputreal it name ="+it.name)
            var output =
                if (it.name.endsWith("jar")) {
                    outputProvider.getContentLocation(
                        "pc-"+it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.JAR
                    )
                } else {
                    outputProvider.getContentLocation(
                        it.file.name,
                        it.contentTypes,
                        it.scopes,
                        Format.JAR
                    )
                }
            println("outputreal ="+output.absolutePath)
            var destFile=File(output.parent,it.file.name)
            destFile=output
            println("output ="+destFile.absolutePath)
            val command= ("jarsigner -verify " +it.file.absolutePath)
            println("command=$command")
            val signCheck=command.runCommand(it.file)
            if (signCheck == null) {
                println("$command run error******************")
                return
            }
            println("sign check =  $signCheck")
            val signed=signCheck.contains("已验证")
            val nameSkip=it.name.contains("mqttv3")
            if (signed) {
                GFileUtils.copyFile(it.file, output)
            }else{
                if (incremental) {
                    when (it.status) {
                        Status.ADDED, Status.CHANGED -> {
                            logger.info("directory status is ${it.status}  file is:" + it.file.absolutePath)
                            PrivacyClassProcessor.processJar(
                                logger,
                                it.file,
                                extension,
                                runAsm = { input, PrivacyExtension ->
                                    PrivacyClassProcessor.runCollect(
                                        input,
                                        extension
                                    )
                                })
                            GFileUtils.deleteQuietly(destFile)
                            GFileUtils.copyFile(it.file, destFile)
                        }
                        Status.REMOVED -> {
                            logger.info("jar REMOVED file is:" + it.file.absolutePath)
                            GFileUtils.deleteQuietly(destFile)
                        }
                    }
                } else {
                    logger.info("jar incremental false file is:" + it.file.absolutePath)
                    if (it.file.absolutePath.contains("mqttv3")) {

                    }
                    PrivacyClassProcessor.processJar(
                        logger,
                        it.file,
                        extension,
                        runAsm = { input, project ->
                            PrivacyClassProcessor.runCollect(
                                input,
                                project
                            )
                        })
                    println("destfiel="+destFile.absolutePath)
                    GFileUtils.deleteQuietly(destFile)
                    GFileUtils.copyFile(it.file, destFile)
                }
            }
        }
    }

    // 处理directory
    private fun handleDirectory(
        transformInput: TransformInput,
        outputProvider: TransformOutputProvider,
        incremental: Boolean,
        extension: PrivacyExtension
    ) {
        transformInput.directoryInputs.forEach {
            var inputDir = it.file
            var outputDir = outputProvider.getContentLocation(
                it.name,
                it.contentTypes,
                it.scopes,
                Format.DIRECTORY
            )
            if (incremental) {
                for ((inputFile, status) in it.changedFiles) {
                    val destFileChild= inputFile.toRelativeString(it.file)
                    val destFile=File(outputDir,destFileChild)
                    when (status) {
                        Status.REMOVED -> {
                            logger.info("directory REMOVED file is:" + inputFile.absolutePath)
                            GFileUtils.deleteQuietly(inputFile)
                        }
                        Status.ADDED, Status.CHANGED -> {
                            logger.info("directory status is $status $ file is:" + inputFile.absolutePath)
                            PrivacyClassProcessor.processDirectory(
                                logger,
                                inputDir,
                                inputFile,
                                extension,
                                runAsm = { input, extension ->
                                    PrivacyClassProcessor.runCollect(
                                        input,
                                        extension
                                    )
                                }
                            )
                            if (inputFile.exists()) {
                                GFileUtils.deleteQuietly(destFile)
                                FileUtils.copyFile(inputFile,destFile)
                            }
                        }
                    }
                }
            } else {
                logger.info("directory incremental false  file is:" + inputDir.absolutePath)
                inputDir.walk().forEach { file ->
                    if (!file.isDirectory) {
                        PrivacyClassProcessor.processDirectory(
                            logger,
                            inputDir,
                            file,
                            extension,
                            runAsm = { input, project ->
                                PrivacyClassProcessor.runCollect(
                                    input,
                                    project
                                )
                            })
                    }
                }

                // 保险起见，删一次
                GFileUtils.deleteQuietly(outputDir)
                FileUtils.copyDirectory(inputDir, outputDir)
            }
        }
    }
}