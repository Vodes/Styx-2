package moe.styx.logic.viewmodels

import moe.styx.common.compose.viewmodels.OverviewViewModel
import moe.styx.logic.utils.MpvUtils

class DesktopOverViewModel : OverviewViewModel() {
    override fun checkPlayerVersion() = MpvUtils.checkVersionAndDownload()
}