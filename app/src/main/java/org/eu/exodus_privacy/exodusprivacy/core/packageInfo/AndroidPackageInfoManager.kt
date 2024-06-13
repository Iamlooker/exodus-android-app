package org.eu.exodus_privacy.exodusprivacy.core.packageInfo

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.data.model.Application
import org.eu.exodus_privacy.exodusprivacy.data.model.Permission
import org.eu.exodus_privacy.exodusprivacy.data.model.Source
import org.eu.exodus_privacy.exodusprivacy.utils.DefaultDispatcher
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import org.eu.exodus_privacy.exodusprivacy.utils.getInstalledPackagesList
import org.eu.exodus_privacy.exodusprivacy.utils.getSource
import javax.inject.Inject

class AndroidPackageInfoManager @Inject constructor(
    private val packageManager: PackageManager,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher,
) : PackageInfoManager {

    override suspend fun getApplicationList(
        validPackages: List<PackageInfo>,
    ): List<Application> {
        val permissionsMap = generatePermissionsMap(validPackages, packageManager)
        val applicationList = mutableListOf<Application>()
        validPackages.forEach { packageInfo ->
            Log.d(TAG, "Found package: ${packageInfo.packageName}.")
            val app = Application(
                name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                packageName = packageInfo.packageName,
                icon = packageInfo.applicationInfo.loadIcon(packageManager)
                    .toBitmap(ICON_SIZE, ICON_SIZE),
                versionName = packageInfo.versionName ?: "",
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                permissions = permissionsMap[packageInfo.packageName] ?: emptyList(),
                source = getAppStore(packageInfo.packageName, packageManager),
            )
            Log.d(TAG, "Add app: ${app.name}, ${app.versionName}")
            applicationList.add(app)
        }
        applicationList.sortBy { it.name }
        return applicationList
    }

    override fun getValidPackageList(): List<PackageInfo> {
        val packageList = packageManager
            .getInstalledPackagesList(PackageManager.GET_PERMISSIONS)
        return packageList.filter { validPackage(it, packageManager) }
    }

    override suspend fun generatePermissionsMap(
        packages: List<PackageInfo>,
        packageManager: PackageManager,
    ): Map<String, List<Permission>> {
        return withContext(defaultDispatcher) {
            val packagesWithPermissions = packages.filterNot { it.requestedPermissions == null }
            Log.d(TAG, "Packages with perms: $packagesWithPermissions")
            val permissionInfoSet = packagesWithPermissions.fold(
                hashSetOf<String>(),
            ) { acc, next ->
                if (next.requestedPermissions != null) {
                    acc.addAll(next.requestedPermissions)
                }
                acc
            }
            Log.d(TAG, "Permission Info Set: $permissionInfoSet")
            val permissionMap = hashMapOf<String, List<Permission>>()
            var permissionList = permissionInfoSet.map { permissionName ->
                generatePermission(permissionName, packageManager)
            }
            permissionList = permissionList.sortedWith(PermissionComparator)
            Log.d(TAG, "Permission List: $permissionList")
            packagesWithPermissions.forEach { packageInfo ->
                permissionMap[packageInfo.packageName] = permissionList.filter { perm ->
                    packageInfo.requestedPermissions.any { reqPerm ->
                        reqPerm == perm.longName
                    }
                }
            }
            return@withContext permissionMap
        }
    }

    private suspend fun generatePermission(
        longName: String,
        packageManager: PackageManager,
    ): Permission {
        return withContext(defaultDispatcher) {
            val shortName = longName
                .substringAfterLast('.')
                .run {
                    if (matches("[a-z].".toRegex())) { // we may
                        longName.replace("[^>]*[a-z][.]".toRegex(), "")
                    } else {
                        this
                    }
                }
            val permInfo: PermissionInfo = try {
                packageManager.getPermissionInfo(
                    longName,
                    PackageManager.GET_META_DATA,
                )
            } catch (exception: PackageManager.NameNotFoundException) {
                Log.d(TAG, "Unable to find info about $longName.")
                return@withContext Permission(
                    shortName = shortName,
                    longName = longName,
                    label = longName,
                )
            }
            Permission(
                shortName = shortName,
                longName = longName,
                label = permInfo.loadLabel(packageManager).toString(),
            )
        }
    }

    private fun getAppStore(packageName: String, packageManager: PackageManager): Source {
        val appStore = packageManager.getSource(packageName)
        Log.d(TAG, "Found AppStore: $appStore for app: $packageName.")
        return when (appStore) {
            GOOGLE_PLAY_STORE -> Source.GOOGLE
            AURORA_STORE -> Source.GOOGLE
            FDROID -> Source.FDROID
            SYSTEM -> Source.SYSTEM
            else -> Source.USER
        }
    }

    private fun validPackage(packageInfo: PackageInfo, packageManager: PackageManager): Boolean {
        val appInfo = packageInfo.applicationInfo
        val packageName = packageInfo.packageName
        return (
            appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
                packageManager.getLaunchIntentForPackage(packageName) != null
            ) &&
            appInfo.enabled
    }

    private companion object {
        const val TAG = "AndroidPackageInfoManager"
        const val GOOGLE_PLAY_STORE = "com.android.vending"
        const val AURORA_STORE = "com.aurora.store"
        const val FDROID = "org.fdroid.fdroid"
        val SYSTEM: String? = null
        const val ICON_SIZE = 96

        val PermissionComparator = compareBy<Permission, String>(String.CASE_INSENSITIVE_ORDER) {
            it.shortName
        }
    }
}
