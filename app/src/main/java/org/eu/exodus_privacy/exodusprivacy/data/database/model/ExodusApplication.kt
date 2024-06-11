package org.eu.exodus_privacy.exodusprivacy.data.database.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.eu.exodus_privacy.exodusprivacy.data.model.Permission
import org.eu.exodus_privacy.exodusprivacy.data.model.Source

@Entity
data class ExodusApplication(
    @PrimaryKey val packageName: String = String(),
    val name: String = String(),
    val icon: Bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.RGB_565),
    val versionName: String = String(),
    val versionCode: Long = 0L,
    val permissions: List<Permission> = emptyList(),
    val exodusVersionName: String = String(),
    val exodusVersionCode: Long = 0L,
    val exodusTrackers: List<Int> = emptyList(),
    val source: Source = Source.GOOGLE,
    val report: Int = 0,
    val created: String = String(),
    val updated: String = String(),
)
