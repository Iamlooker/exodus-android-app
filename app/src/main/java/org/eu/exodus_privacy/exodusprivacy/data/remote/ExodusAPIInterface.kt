package org.eu.exodus_privacy.exodusprivacy.data.remote

import org.eu.exodus_privacy.exodusprivacy.data.remote.model.AppDetails
import org.eu.exodus_privacy.exodusprivacy.data.remote.model.Trackers
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExodusAPIInterface {

    companion object {
        const val BASE_URL = "https://reports.exodus-privacy.eu.org/api/"
    }

    @GET("trackers")
    suspend fun getAllTrackers(): Response<Trackers>

    @GET("search/{packageName}/details")
    suspend fun getAppDetails(
        @Path("packageName") packageName: String,
    ): Response<List<AppDetails>>
}
