package moe.styx.views.data

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.get
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.Stores
import moe.styx.common.compose.files.getBlocking
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.Log
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainDataViewModel : ScreenModel {
    private val _storageFlow = MutableStateFlow(MainDataViewModelStorage())
    val storageFlow = _storageFlow.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), MainDataViewModelStorage())

    val loadingStateFlow = Storage.loadingProgress.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), "")
    private val _isLoadingStateFlow = MutableStateFlow(false)
    val isLoadingStateFlow = _isLoadingStateFlow.combine(Storage.isLoaded.asStateFlow()) { a, b -> a || !b }
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), false)

    init {
        Log.d { "Initializing MainDataViewModel" }
        startUpdateLoop()
    }

    private fun startUpdateLoop() = screenModelScope.launch {
        _isLoadingStateFlow.emit(true)
        updateData()
        while ((ServerStatus.lastKnown == ServerStatus.UNKNOWN || login == null) && isActive) {
            Log.d("MainDataViewModel") { "Waiting for login..." }
            delay(2000)
        }
        ensureActive()
        updateData(forceUpdate = true, updateStores = true)
        _isLoadingStateFlow.emit(false)

        delay(20000)
        while (isActive) {
            delay(5.toDuration(DurationUnit.MINUTES))
            ensureActive()
            Log.d("MainDataViewModel") { "Running automatic data refresh." }
            _isLoadingStateFlow.emit(true)
            updateData(updateStores = true)
            _isLoadingStateFlow.emit(false)
        }
    }

    fun updateData(forceUpdate: Boolean = false, updateStores: Boolean = false) {
        screenModelScope.launch {
            if (updateStores) {
                launch { Storage.loadData() }.also { Storage.refreshDataJob = it }.join()
                Storage.refreshDataJob = null
            }
            if (forceUpdate)
                _storageFlow.emit(getUpdatedStorage())
            else
                _storageFlow.getAndUpdate { getUpdatedStorage(it.updated) }
        }
    }

    private fun getUpdatedStorage(unixSeconds: Long? = null): MainDataViewModelStorage {
        return MainDataViewModelStorage(
            Stores.mediaStore.getBlocking(),
            Stores.entryStore.getBlocking(),
            Stores.imageStore.getBlocking(),
            Stores.categoryStore.getBlocking(),
            Stores.favouriteStore.getBlocking(),
            Stores.watchedStore.getBlocking(),
            unixSeconds ?: currentUnixSeconds()
        )
    }

    fun getMediaStorageForID(id: String, storage: MainDataViewModelStorage): MediaStorage {
        Log.d { "Fetching metadata for: $id" }
        val media = storage.mediaList.find { it.GUID eqI id }!!
        val prequel = if (media.prequel.isNullOrBlank()) null else storage.mediaList.find { it.GUID eqI media.prequel }
        val sequel = if (media.sequel.isNullOrBlank()) null else storage.mediaList.find { it.GUID eqI media.sequel }
        val filtered = storage.entryList.filter { it.mediaID eqI media.GUID }
        val entries = if (settings["episode-asc", false]) filtered.sortedBy {
            it.entryNumber.toDoubleOrNull() ?: 0.0
        } else filtered.sortedByDescending { it.entryNumber.toDoubleOrNull() ?: 0.0 }
        return MediaStorage(
            media,
            storage.imageList.find { it.GUID eqI media.thumbID },
            prequel,
            prequel?.let { storage.imageList.find { it.GUID eqI prequel.thumbID } },
            sequel,
            sequel?.let { storage.imageList.find { it.GUID eqI sequel.thumbID } },
            entries
        )
    }
}

data class MediaStorage(
    val media: Media,
    val image: Image?,
    val prequel: Media?,
    val prequelImage: Image?,
    val sequel: Media?,
    val sequelImage: Image?,
    val entries: List<MediaEntry>,
)

data class MainDataViewModelStorage(
    val mediaList: List<Media> = emptyList(),
    val entryList: List<MediaEntry> = emptyList(),
    val imageList: List<Image> = emptyList(),
    val categoryList: List<Category> = emptyList(),
    val favouritesList: List<Favourite> = emptyList(),
    val watchedList: List<MediaWatched> = emptyList(),
    val updated: Long = 0L
) {
    override fun hashCode() =
        (updated + mediaList.size + entryList.size + imageList.size + categoryList.size + favouritesList.size + watchedList.size).hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is MainDataViewModelStorage)
            return super.equals(other)
        return updated == other.updated &&
                mediaList.size == other.mediaList.size &&
                entryList.size == other.entryList.size &&
                imageList.size == other.imageList.size &&
                watchedList.size == other.watchedList.size &&
                favouritesList.size == other.favouritesList.size &&
                categoryList.size == other.categoryList.size
    }
}