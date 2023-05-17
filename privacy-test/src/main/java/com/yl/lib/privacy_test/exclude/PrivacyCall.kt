package com.yl.lib.privacy_test.exclude

import android.content.Context
import android.content.pm.PackageManager

object PrivacyCall {
    fun getPackInfo(context: Context){
        context.packageManager.getPackageInfo("com.yl.lib", PackageManager.GET_ACTIVITIES)
    }
}