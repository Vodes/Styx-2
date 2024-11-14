package moe.styx.logic.viewmodels

import moe.styx.common.compose.viewmodels.OverviewViewModel
import moe.styx.logic.utils.MpvUtils

class DesktopOverViewModel : OverviewViewModel() {
    override fun isUpToDate(): Boolean = moe.styx.logic.utils.isUpToDate()
    override fun checkPlayerVersion() = MpvUtils.checkVersionAndDownload()
}