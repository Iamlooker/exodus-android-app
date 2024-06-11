package org.eu.exodus_privacy.exodusprivacy.ui.fragments.trackers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.eu.exodus_privacy.exodusprivacy.data.database.ExodusDatabaseRepository
import org.eu.exodus_privacy.exodusprivacy.data.database.model.TrackerData
import javax.inject.Inject

@HiltViewModel
class TrackersViewModel @Inject constructor(
    exodusDatabaseRepository: ExodusDatabaseRepository,
) : ViewModel() {

    val trackersList: LiveData<List<TrackerData>> = exodusDatabaseRepository.getActiveTrackers()
}
