package org.eu.exodus_privacy.exodusprivacy.core.packageInfo

import android.content.pm.PackageInfo
import org.eu.exodus_privacy.exodusprivacy.data.model.Application
import org.eu.exodus_privacy.exodusprivacy.data.model.Permission

interface PackageInfoManager {

    suspend fun getApplicationList(
        validPackages: List<PackageInfo>,
    ): List<Application>

    fun getValidPackageList(): List<PackageInfo>

    suspend fun generatePermissionsMap(
        packages: List<PackageInfo>,
    ): Map<String, List<Permission>>

}