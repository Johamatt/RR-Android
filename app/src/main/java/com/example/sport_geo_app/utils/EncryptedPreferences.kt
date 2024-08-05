package com.example.sport_geo_app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.sport_geo_app.utils.Constants.PREFS_FILENAME
import java.security.GeneralSecurityException

object EncryptedPreferencesUtil {
    fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun clearEncryptedPreferences(context: Context) {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        val editor = sharedPreferences.edit()
        try {
            editor.clear()
            editor.apply()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
    }


}

