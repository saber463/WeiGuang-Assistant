package com.weiguangplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.weiguangplus.ui.screen.alert.AlertSettingsScreen
import com.weiguangplus.ui.screen.call.CallAssistantScreen
import com.weiguangplus.ui.screen.chat.ChatListScreen
import com.weiguangplus.ui.screen.chat.ChatDetailScreen
import com.weiguangplus.ui.screen.drug.DrugRecognitionScreen
import com.weiguangplus.ui.screen.home.MainScreen
import com.weiguangplus.ui.screen.signlanguage.SignLanguageScreen
import com.weiguangplus.ui.screen.sos.SosScreen
import com.weiguangplus.ui.screen.alert.EmergencyContactScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("home") }
            var chatConvId by remember { mutableStateOf("") }
            var chatConvName by remember { mutableStateOf("") }

            when (currentScreen) {
                "home" -> MainScreen(onNavigate = { currentScreen = it })
                "chat" -> ChatListScreen(
                    onChatClick = { id, name ->
                        chatConvId = id
                        chatConvName = name
                        currentScreen = "chat_detail"
                    },
                    onBack = { currentScreen = "home" }
                )
                "chat_detail" -> ChatDetailScreen(
                    conversationId = chatConvId,
                    contactName = chatConvName,
                    onBack = { currentScreen = "chat" }
                )
                "drug" -> DrugRecognitionScreen()
                "sign" -> SignLanguageScreen()
                "call" -> CallAssistantScreen()
                "alert" -> AlertSettingsScreen(onNavigate = { currentScreen = it })
                "sos" -> SosScreen(onBack = { currentScreen = "home" })
                "emergency_contacts" -> EmergencyContactScreen(onBack = { currentScreen = "alert" })
                "settings" -> MainScreen(onNavigate = { currentScreen = "home" })
                else -> MainScreen(onNavigate = { currentScreen = "home" })
            }
        }
    }
}
