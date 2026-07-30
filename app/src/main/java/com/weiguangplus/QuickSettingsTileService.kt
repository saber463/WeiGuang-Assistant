package com.weiguangplus

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.weiguangplus.core.emergency.EmergencyContactManager
import com.weiguangplus.core.emergency.SosManager

/**
 * SOS 快捷设置磁贴
 * 用户可在通知栏快速下拉中一键触发 SOS 求救
 */
class QuickSettingsTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        if (!EmergencyContactManager.hasContacts()) {
            // 无联系人时点击跳转到主应用
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("navigate_to", "alert")
            }
            startActivityAndCollapse(intent)
            return
        }

        // 切换 SOS 状态
        if (tile.state == Tile.STATE_ACTIVE) {
            // 已在激活状态，触发 SOS
            SosManager.trigger(this) { result ->
                tile.state = if (result.success) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.subtitle = if (result.success)
                    "已发送 ${result.smsSent} 条"
                else
                    result.error ?: "发送失败"
                tile.updateTile()
            }
        } else {
            // 首次点击，激活并触发
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
            SosManager.trigger(this) { result ->
                tile.state = if (result.success) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.subtitle = if (result.success)
                    "已发送 ${result.smsSent} 条"
                else
                    result.error ?: "发送失败"
                tile.updateTile()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.label = "SOS求救"
        tile.state = Tile.STATE_INACTIVE
        tile.subtitle = if (EmergencyContactManager.hasContacts()) "点击发送求救" else "需先设置联系人"
        tile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        onStartListening()
    }
}
