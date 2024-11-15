package moe.styx.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel

class ListPosViewModel : ScreenModel {
    var scrollIndex: Int by mutableStateOf(0)
    var scrollOffset: Int by mutableStateOf(0)

    init {
        println("Created new ListPosModel")
    }
}