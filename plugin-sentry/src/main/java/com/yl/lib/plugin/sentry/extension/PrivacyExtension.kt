package com.yl.lib.plugin.sentry.extension

import org.gradle.api.Incubating

/**
 * @author yulun
 * @sinice 2021-12-13 17:28
 */
open class PrivacyExtension {
    // 不修改的黑名单，首先是包括自己
    var blackList: Set<String>? = null
    //配置使用方式 blackList (["org.eclipse.paho.client.mqttv3"] as Set<String>)
    fun blackList(set: Set<String>?) {
        blackList = set
    }

    // 记录所有被替换的方法名+类名,将以单行的形式被写入到文件中
    // 空=不写入
    var replaceFileName: String? = null
    fun replaceFileName(name: String) {
        replaceFileName = name
    }

    // buildType为debug是否应用此插件
    var buildTypeDebugEnable = false

    fun buildTypeDebugEnable(enable: Boolean) {
        buildTypeDebugEnable = enable
    }
}