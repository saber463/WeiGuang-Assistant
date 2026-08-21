package com.weiguangplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.weiguangplus.ui.screen.family.FamilyModeScreen

/**
 * 家庭模式（独立 Activity 入口）
 *
 * 渲染与 MainActivity 内 `family` 路由相同的家庭看护中心（配对码 + 家人管理 + SOS 联动），
 * 供需要以独立 Activity 方式打开的入口复用。实现由 FamilyModeScreen 承载。
 */
class FamilyModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 独立 Activity 无上层导航栈，onBack 触发时直接结束自身
            FamilyModeScreen(
                onBack = { finish() },
                onNavigate = { /* 独立入口暂不提供跨屏跳转，仅保留本页看护能力 */ }
            )
        }
    }
}