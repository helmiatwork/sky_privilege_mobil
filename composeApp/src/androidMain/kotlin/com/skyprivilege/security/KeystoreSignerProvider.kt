package com.skyprivilege.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

class KeystoreSignerProvider {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SkyPrivilegeDeviceSigningKey"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }

    init {
        ensureKeyPairGenerated()
    }

    private fun ensureKeyPairGenerated() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setDigests(KeyProperties.DIGEST_SHA256)
                setUserAuthenticationRequired(false)
                build()
            }
            kpg.initialize(parameterSpec)
            kpg.generateKeyPair()
        }
    }

    fun signData(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry).privateKey

        return Signature.getInstance(SIGNATURE_ALGORITHM).run {
            initSign(privateKey)
            update(data)
            sign()
        }
    }

    fun getPublicKeyBase64(): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val cert = keyStore.getCertificate(KEY_ALIAS)
        return Base64.encodeToString(cert.publicKey.encoded, Base64.NO_WRAP)
    }
}
