package com.jegly.frequency.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object TamperDetection {

    // Expected signing-cert SHA-256 digests, stored XOR-masked (never as greppable hex — string
    // literals survive R8) and compared as raw bytes. M = jegly key0 (98d324d4…),
    // P = ML-DSA-87 PQC key (5f99de5b…).
    // API 37+ verifies the APK Signature Scheme v3.2 hybrid block, so the current signer is the PQC
    // key there. key0 is deliberately NOT accepted on API 37+: an APK re-signed with only key0 would
    // otherwise pass. Older releases ignore v3.2 and still see key0.
    private val K = intArrayOf(0xB7, 0xE3, 0x02, 0xCF, 0xBA, 0xA9, 0x61, 0x17, 0x2A)
    private val M = intArrayOf(
        0x2F, 0x30, 0x26, 0x1B, 0xAA, 0xC3, 0x57, 0x9B, 0x48, 0xC5, 0x79, 0x08, 0xEB, 0x63, 0x05, 0xFB,
        0x7C, 0x6D, 0x4F, 0x4F, 0x4E, 0xAA, 0x3F, 0x9D, 0xE4, 0x26, 0xDA, 0x59, 0xAD, 0xB4, 0x6F, 0xF6,
    )
    private val P = intArrayOf(
        0xE8, 0x7A, 0xDC, 0x94, 0x8B, 0x3C, 0x14, 0x2A, 0x65, 0x5F, 0x3E, 0xA9, 0x24, 0x68, 0x52, 0x46,
        0x22, 0x41, 0x96, 0xEA, 0xD4, 0x34, 0x08, 0xEE, 0x7E, 0xDC, 0x97, 0x45, 0xF9, 0x9C, 0xB9, 0x1C,
    )

    /** Reconstruct the expected 32-byte cert digest from the masked form (never materialized as hex). */
    private fun expected(): ByteArray {
        val masked = if (Build.VERSION.SDK_INT >= 37) P else M
        return ByteArray(masked.size) { (masked[it] xor K[it % K.size]).toByte() }
    }

    fun isValid(context: Context): Boolean = try {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
        // Current signer only — never signingCertificateHistory, which still contains key0.
        val signers = info.signingInfo?.apkContentsSigners ?: return false
        val md = MessageDigest.getInstance("SHA-256")
        val want = expected()
        signers.any { sig -> md.digest(sig.toByteArray()).contentEquals(want) }
    } catch (_: Exception) {
        false
    }
}
