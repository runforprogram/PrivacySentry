package com.yl.lib.plugin.sentry

import com.android.build.api.variant.VariantInfo

fun VariantInfo?.isApply(): Boolean {
    return if (this == null) {
        println("privacy variant is null")
        false
    } else {
        println("privacy variant is not null")
        this.buildTypeName == "release"
    }
}