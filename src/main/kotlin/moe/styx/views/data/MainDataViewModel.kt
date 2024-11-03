package moe.styx.views.data

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getBlocking
import moe.styx.common.compose.settings
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.Log
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainDataViewModel : ScreenModel {
    private val _storageFlow = MutableStateFlow(MainDataViewModelStorage())
    val storageFlow = _storageFlow.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), MainDataViewModelStorage())

    init {
        Log.d { "Initializing MainDataViewModel" }
        updateData(true)
        screenModelScope.launch {
            delay(20000)
            while (true) {
                delay(5.toDuration(DurationUnit.MINUTES))
                Log.d("MainDataViewModel") { "Running automatic data refresh." }
                Storage.loadData()
                updateData()
            }
        }
    }

    fun updateData(forceUpdate: Boolean = false) {
        screenModelScope.launch {
            if (forceUpdate)
                _storageFlow.emit(getUpdatedStorage())
            else
                _storageFlow.getAndUpdate { getUpdatedStorage(it.updated) }
        }
    }

    private fun getUpdatedStorage(unixSeconds: Long? = null): MainDataViewModelStorage {
        return MainDataViewModelStorage(
            Storage.stores.mediaStore.getBlocking(),
            Storage.stores.entryStore.getBlocking(),
            Storage.stores.imageStore.getBlocking(),
            Storage.stores.categoryStore.getBlocking(),
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
    val updated: Long = 0L
) {
    override fun hashCode() = (updated + mediaList.size + entryList.size + imageList.size + categoryList.size).hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is MainDataViewModelStorage)
            return super.equals(other)
        return updated == other.updated &&
                mediaList.size == other.mediaList.size &&
                entryList.size == other.entryList.size &&
                imageList.size == other.imageList.size &&
                categoryList.size == other.categoryList.size
    }
}