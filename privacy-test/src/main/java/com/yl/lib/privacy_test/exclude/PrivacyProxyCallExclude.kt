package com.yl.lib.privacy_test.exclude

import android.content.*
import android.content.pm.*
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.yl.lib.privacy_annotation.MethodInvokeOpcode
import com.yl.lib.privacy_annotation.PrivacyClassProxy
import com.yl.lib.privacy_annotation.PrivacyMethodProxy

@Keep
open class PrivacyProxyCallExclude {
    // kotlin里实际解析的是这个PrivacyProxyCall$Proxy 内部类
    @PrivacyClassProxy
    @Keep
    object Proxy {
        @RequiresApi(Build.VERSION_CODES.O)
        @PrivacyMethodProxy(
            originalClass = PackageManager::class,
            originalMethod = "getPackageInfo",
            originalOpcode = MethodInvokeOpcode.INVOKEVIRTUAL,
            ignoreClass = false,
            includePackageNames = [],
            excludePackageNames = ["com.yl.lib.privacy_test.exclude","com.yl.lib.privacy_test.include"]
        )
        @JvmStatic
        fun getPackageInfo(
            manager: PackageManager,
            packageName:String,
            flags: Int
        ): PackageInfo? {
            return manager.getPackageInfo(packageName,flags)
        }
    }
}