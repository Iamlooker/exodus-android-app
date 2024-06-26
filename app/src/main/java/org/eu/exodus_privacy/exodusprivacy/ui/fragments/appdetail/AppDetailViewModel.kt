package org.eu.exodus_privacy.exodusprivacy.ui.fragments.appdetail

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.data.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.data.database.model.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.data.database.model.TrackerData
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val exodusDatabaseRepository: ExodusDatabaseRepository,
) : ViewModel() {

    val app: MutableLiveData<ExodusApplication> = MutableLiveData()
    val trackers: MutableLiveData<List<TrackerData>> = MutableLiveData()

    fun getApp(packageName: String) {
        viewModelScope.launch {
            app.value = exodusDatabaseRepository.getApp(packageName)
        }.invokeOnCompletion {
            if (it == null) {
                getTrackers(app.value!!.exodusTrackers)
            }
        }
    }

    private fun getTrackers(listOfID: List<Int>) {
        viewModelScope.launch {
            trackers.value = exodusDatabaseRepository.getTrackers(listOfID)
        }
    }

    fun getFormattedReportDate(date: String, context: Context): String {
        // Generate date object in currentSDF to format and parse
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date.split("T")[0])
        // Format it
        val sdf = DateFormat.getLongDateFormat(context)
        return sdf.format(currentDate!!)
    }
}
