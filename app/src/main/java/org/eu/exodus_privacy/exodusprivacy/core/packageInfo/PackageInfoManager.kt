package org.eu.exodus_privacy.exodusprivacy.core.packageInfo

import android.graphics.Bitmap
import org.eu.exodus_privacy.exodusprivacy.data.model.Application
import org.eu.exodus_privacy.exodusprivacy.data.model.Permission

interface PackageInfoManager {

    val validPackages: Int

    suspend fun getApplications(
        validPackages: List<ExodusPackageInfo>,
    ): List<Application>

    fun getValidPackages(): List<ExodusPackageInfo>

    suspend fun generatePermissionsMap(
        packages: List<ExodusPackageInfo>,
    ): Map<String, List<Permission>>

}

data class ExodusPackageInfo(
    val packageName: String,
    val requestedPermissions: List<String>,
    val versionCode: Long,
    val versionName: String,
    val name: String,
    val icon: Bitmap,
)
