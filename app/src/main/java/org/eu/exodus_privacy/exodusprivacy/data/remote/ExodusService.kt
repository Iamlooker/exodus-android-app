package org.eu.exodus_privacy.exodusprivacy.data.remote

import org.eu.exodus_privacy.exodusprivacy.data.remote.model.AppDetails
import org.eu.exodus_privacy.exodusprivacy.data.remote.model.Trackers

interface ExodusService {

    suspend fun getAllTrackers(): Trackers

    suspend fun getAppDetails(packageName: String): List<AppDetails>

}