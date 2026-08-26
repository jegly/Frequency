package com.jegly.frequency.utils

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

object TamperDetection {

    private const val EXPECTED_SHA256 =
        "98d324d4106a368c62729a0a24d9ac9a6b47f8ac4c6585348531f0ee4eb6a04c"

    fun isValid(context: Context): Boolean = try {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
        val signing = info.signingInfo ?: return false
        val certs = if (signing.hasMultipleSigners()) signing.apkContentsSigners
                    else signing.signingCertificateHistory
        certs.any { cert ->
            MessageDigest.getInstance("SHA-256")
                .digest(cert.toByteArray())
                .joinToString("") { "%02x".format(it) } == EXPECTED_SHA256
        }
    } catch (_: Exception) {
        false
    }
}
