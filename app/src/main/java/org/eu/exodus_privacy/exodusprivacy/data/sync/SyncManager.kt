package org.eu.exodus_privacy.exodusprivacy.data.sync

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okio.IOException
import org.eu.exodus_privacy.exodusprivacy.core.packageInfo.PackageInfoManager
import org.eu.exodus_privacy.exodusprivacy.data.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.data.database.model.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.data.database.model.TrackerData
import org.eu.exodus_privacy.exodusprivacy.data.model.Application
import org.eu.exodus_privacy.exodusprivacy.data.remote.ExodusService
import org.eu.exodus_privacy.exodusprivacy.data.remote.model.AppDetails
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import org.eu.exodus_privacy.exodusprivacy.utils.propagateCancellation
import org.eu.exodus_privacy.exodusprivacy.utils.updateAndGet
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val databaseRepository: ExodusDatabaseRepository,
    private val apiRepository: ExodusService,
    private val packageRepository: PackageInfoManager,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
) {

    private val countLock = Mutex()

    suspend fun sync(
        onTrackerSyncDone: suspend () -> Unit,
        onAppSync: suspend () -> Unit,
    ) {
        coroutineScope {
            val trackerJob = launch {
                syncAllTrackers()
                onTrackerSyncDone()
            }
            val appsWithTrackers = mutableListOf<AppDetails>()
            val appDetailJob = launch {
                fetchAppDetails { appDetail ->
                    onAppSync()
                    if (appDetail.trackers.isNotEmpty()) {
                        countLock.withLock { appsWithTrackers.add(appDetail) }
                    }
                }
            }
            joinAll(trackerJob, appDetailJob)
            updateTrackerDetails(appsWithTrackers)
        }
    }

    private suspend fun fetchAppDetails(
        onAppSync: suspend (appDetail: AppDetails) -> Unit,
    ) {
        withContext(ioDispatcher) {
            val validPackages = packageRepository.getValidPackages()
            val currentAppList = packageRepository.getApplications(validPackages)
            val syncedApps = mutableListOf<ExodusApplication>()

            launch {
                removeDeletedApps(currentAppList)
            }
            currentAppList.forEach { app ->
                ensureActive()

                val appDetails = try {
                    apiRepository.getAppDetails(app.packageName)
                } catch (e: SocketTimeoutException) {
                    Log.w(TAG, "Socket Timeout", e)
                    emptyList()
                }

                val latestApp = appDetails
                    .find { it.version_code.toLongOrZero() == app.versionCode }
                    ?: appDetails.maxByOrNull { it.version_code.toLongOrZero() }
                    ?: AppDetails()

                val exodusApp = ExodusApplication(
                    packageName = app.packageName,
                    name = app.name,
                    icon = app.icon,
                    versionName = app.versionName,
                    versionCode = app.versionCode,
                    permissions = app.permissions,
                    exodusVersionName = latestApp.version_name,
                    exodusVersionCode = latestApp.version_code.toLongOrZero(),
                    exodusTrackers = latestApp.trackers,
                    source = app.source,
                    report = latestApp.report,
                    created = latestApp.created,
                    updated = latestApp.updated,
                )

                syncedApps.add(exodusApp)

                onAppSync(latestApp)
            }
            syncedApps.forEach { exodusApp ->
                databaseRepository.saveApp(exodusApp)
            }
        }
    }

    private suspend fun syncAllTrackers() = supervisorScope {
        val list = try {
            apiRepository.getAllTrackers()
        } catch (e: Exception) {
            e.propagateCancellation()
            Log.e(TAG, "Unable to fetch trackers.", e)
            null
        }
        list?.trackers?.forEach { (key, tracker) ->
            launch {
                ensureActive()
                val trackerData = TrackerData(
                    id = key.toInt(),
                    categories = tracker.categories,
                    code_signature = tracker.code_signature,
                    creation_date = tracker.creation_date,
                    description = tracker.description,
                    name = tracker.name,
                    network_signature = tracker.network_signature,
                    website = tracker.website,
                )
                try {
                    databaseRepository.saveTrackerData(trackerData)
                } catch (e: IOException) {
                    Log.e(TAG, "Unable to save trackers.", e)
                }
            }
        }
    }

    private suspend fun updateTrackerDetails(
        appDetails: List<AppDetails>,
    ) {
        supervisorScope {
            appDetails.forEach { detail ->
                launch {
                    detail.trackers.forEach { trackerId ->
                        setTrackerActive(trackerId, detail.handle)
                    }
                }
            }
            updateAppsWithTrackersCount(appDetails.size)
        }
    }

    private suspend fun removeDeletedApps(
        currentApps: List<Application>,
    ) {
        withContext(ioDispatcher) {
            try {
                val savedApps = databaseRepository.getAllPackageNames()
                val currentPackageNames = currentApps.map { it.packageName }
                val isAppListUpdated = currentPackageNames.size != savedApps.size ||
                        savedApps.isEmpty() ||
                        (savedApps - currentPackageNames.toSet()).isNotEmpty()
                if (!isAppListUpdated) return@withContext
                databaseRepository.deleteApps(savedApps - currentPackageNames.toSet())
            } catch (e: Exception) {
                e.propagateCancellation()
                Log.e(TAG, "Unable to remove apps.", e)
            }
        }
    }

    private suspend fun updateAppsWithTrackersCount(
        count: Int,
    ) {
        val trackers = databaseRepository.getAllTrackers().value ?: return
        trackers.forEach { tracker ->
            yield()
            val updatedTracker = tracker.copy(totalNumberOfAppsHavingTrackers = count)
            databaseRepository.saveTrackerData(updatedTracker)
        }
    }

    private suspend fun setTrackerActive(
        trackerId: Int,
        packageName: String,
    ) {
        val tracker = databaseRepository.getTracker(trackerId)
            .run {
                copy(
                    presentOnDevice = true,
                    exodusApplications = exodusApplications
                        .updateAndGet { add(packageName) },
                )
            }
        databaseRepository.saveTrackerData(tracker)
    }

    private companion object {
        const val TAG = "SyncManager"

        private fun String.toLongOrZero(): Long {
            return if (this.isNotBlank()) {
                this.toLong()
            } else {
                0L
            }
        }
    }
}
