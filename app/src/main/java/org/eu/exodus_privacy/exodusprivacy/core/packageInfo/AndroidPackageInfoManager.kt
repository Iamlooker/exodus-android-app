package org.eu.exodus_privacy.exodusprivacy.core.packageInfo

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.eu.exodus_privacy.exodusprivacy.data.model.Application
import org.eu.exodus_privacy.exodusprivacy.data.model.Permission
import org.eu.exodus_privacy.exodusprivacy.data.model.Source
import org.eu.exodus_privacy.exodusprivacy.utils.DefaultDispatcher
import org.eu.exodus_privacy.exodusprivacy.utils.getInstalledPackagesList
import org.eu.exodus_privacy.exodusprivacy.utils.getSource
import javax.inject.Inject

class AndroidPackageInfoManager @Inject constructor(
    private val packageManager: PackageManager,
    @DefaultDispatcher val defaultDispatcher: CoroutineDispatcher,
) : PackageInfoManager {

    override suspend fun getApplicationList(
        validPackages: List<PackageInfo>,
    ): List<Application> {
        val permissionsMap = generatePermissionsMap(validPackages.filterWithPermissions())
        return validPackages.map { packageInfo ->
            yield()
            Log.d(TAG, "Found package: ${packageInfo.packageName}.")
            val app = Application(
                name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                packageName = packageInfo.packageName,
                icon = packageInfo.applicationInfo.loadIcon(packageManager)
                    .toBitmap(ICON_SIZE, ICON_SIZE),
                versionName = packageInfo.versionName ?: "",
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                permissions = permissionsMap[packageInfo.packageName] ?: emptyList(),
                source = packageInfo.packageName.getAppStore(),
            )
            Log.d(TAG, "Add app: ${app.name}, ${app.versionName}")
            app
        }.sortedBy { it.name }
    }

    override fun getValidPackageList(): List<PackageInfo> {
        val packageList = packageManager
            .getInstalledPackagesList(PackageManager.GET_PERMISSIONS)
        return packageList.filter(::validPackage)
    }

    override suspend fun generatePermissionsMap(
        packages: List<PackageInfo>,
    ): Map<String, List<Permission>> {
        return withContext(defaultDispatcher) {
            val permissionDeferredSet = hashSetOf<Deferred<Pair<String, List<Permission>>>>()
            packages.forEach { info ->
                val packageNameToPermissions = async {
                    info.packageName to
                            (info.requestedPermissions?.map { generatePermission(it) } ?: emptyList())
                }
                permissionDeferredSet.add(packageNameToPermissions)
            }
            permissionDeferredSet.awaitAll().toMap()
        }
    }

    private suspend fun generatePermission(
        longName: String,
    ): Permission {
        return withContext(defaultDispatcher) {
            ensureActive()
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

    private fun String.getAppStore(): Source {
        val appStore = packageManager.getSource(this)
        Log.d(TAG, "Found AppStore: $appStore for app: ${this}.")
        return when (appStore) {
            GOOGLE_PLAY_STORE -> Source.GOOGLE
            AURORA_STORE -> Source.GOOGLE
            FDROID -> Source.FDROID
            SYSTEM -> Source.SYSTEM
            else -> Source.USER
        }
    }

    private fun validPackage(
        packageInfo: PackageInfo,
    ): Boolean {
        val appInfo = packageInfo.applicationInfo
        val packageName = packageInfo.packageName
        return (
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 ||
                        appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0 ||
                        packageManager.getLaunchIntentForPackage(packageName) != null
                ) &&
                appInfo.enabled
    }

    private fun List<PackageInfo>.filterWithPermissions(): List<PackageInfo> =
        filterNot { it.requestedPermissions == null }

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
