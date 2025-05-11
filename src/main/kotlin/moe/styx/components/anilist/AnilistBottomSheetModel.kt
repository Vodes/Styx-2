package moe.styx.components.anilist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.http.login
import moe.styx.common.data.Media
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.common.util.Log
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.MediaListEntry
import pw.vodes.anilistkmp.ext.fetchUserMediaList
import pw.vodes.anilistkmp.ext.fetchViewer
import pw.vodes.anilistkmp.ext.searchMedia
import pw.vodes.anilistkmp.graphql.fragment.MediaBig
import pw.vodes.anilistkmp.graphql.fragment.User

typealias AlMedia = MediaBig
typealias AlUserEntry = MediaListEntry
typealias AlMediaList = List<MediaBig>
typealias AlUserMediaEntries = List<MediaListEntry>

class AnilistBottomSheetModel : ScreenModel {
    val client: AnilistApiClient

    var viewer by mutableStateOf<User?>(null)
        private set
    
    private val _anilistData = MutableStateFlow<AnilistMediaState?>(null)
    val anilistData = _anilistData.stateIn<AnilistMediaState?>(screenModelScope, SharingStarted.WhileSubscribed(2000), null)

    init {
        client = AnilistApiClient(login?.anilistData?.accessToken)
        runBlocking { fetchViewer() }
    }

    private fun fetchViewer() = screenModelScope.launch {
        val response = client.fetchViewer()
        if (response.data == null && login?.anilistData?.accessToken != null) {
            Log.e(exception = response.exception) { "Failed to fetch anilist viewer: ${response.errors?.map { it.message }?.joinToString { "\n" }}" }
        }
        response.data?.let { viewer = it }
    }

    fun fetchMediaState(media: Media) = screenModelScope.launch {
        val mappings = media.decodeMapping() ?: return@launch

        if (mappings.anilistMappings.isEmpty())
            return@launch

        val fetchedAlMedia = client.searchMedia(idIn = mappings.anilistMappings.map { it.remoteID })
        val userAlMedia =
            viewer?.let { user -> client.fetchUserMediaList(userID = user.id, mediaIdIn = mappings.anilistMappings.map { it.remoteID }) }

        if (fetchedAlMedia.data.isEmpty()) {
            val messageString = fetchedAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            Log.e(exception = fetchedAlMedia.exception) { "Failed to fetch anilist media: $messageString" }
            _anilistData.emit(AnilistMediaState(emptyList(), errored = true))
            return@launch
        }

        if (userAlMedia != null && (!userAlMedia.errors.isNullOrEmpty() || userAlMedia.exception != null)) {
            val messageString = userAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            Log.e(exception = userAlMedia.exception) { "Failed to fetch anilist user medialist: $messageString" }
            _anilistData.emit(AnilistMediaState(fetchedAlMedia.data, null, true))
            return@launch
        }
        _anilistData.emit(AnilistMediaState(fetchedAlMedia.data, userAlMedia?.data, false))
    }
}

data class AnilistMediaState(val alMedia: AlMediaList, val userMedia: AlUserMediaEntries? = null, val errored: Boolean = false)