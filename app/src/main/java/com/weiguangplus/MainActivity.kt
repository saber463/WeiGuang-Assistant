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
import com.weiguangplus.ui.screen.auth.LoginScreen
import com.weiguangplus.ui.screen.auth.RegisterScreen
import com.weiguangplus.ui.screen.caption.CaptionSettingsScreen
import com.weiguangplus.ui.screen.drug.DrugRecognitionScreen
import com.weiguangplus.ui.screen.transcript.TranscriptHistoryScreen
import com.weiguangplus.ui.screen.quickphrase.QuickPhraseManagerScreen
import com.weiguangplus.ui.screen.accessibility.AccessibilityReadScreen
import com.weiguangplus.ui.screen.perception.SoundWaveScreen
import com.weiguangplus.ui.screen.rehab.RehabScreen
import com.weiguangplus.ui.screen.home.MainScreen
import com.weiguangplus.ui.screen.signlanguage.SignLanguageScreen
import com.weiguangplus.ui.screen.signlanguage.SignLearningScreen
import com.weiguangplus.ui.screen.sos.SosScreen
import com.weiguangplus.ui.screen.alert.EmergencyContactScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                "sign_learn" -> SignLearningScreen(onBack = { currentScreen = "home" })
                "rehab" -> RehabScreen(onBack = { currentScreen = "home" })
                "call" -> CallAssistantScreen()
                "alert" -> AlertSettingsScreen(onNavigate = { currentScreen = it })
                "soundwave" -> SoundWaveScreen(onBack = { currentScreen = "home" })
                "caption" -> CaptionSettingsScreen(onBack = { currentScreen = "home" })
                "sos" -> SosScreen(onBack = { currentScreen = "home" })
                "transcript" -> TranscriptHistoryScreen(onBack = { currentScreen = "home" })
                "accessibility_read" -> AccessibilityReadScreen(onBack = { currentScreen = "home" })
                "quickphrase" -> QuickPhraseManagerScreen(onBack = { currentScreen = "home" })
                "emergency_contacts" -> EmergencyContactScreen(onBack = { currentScreen = "alert" })
                "login" -> LoginScreen(
                    onLoginSuccess = { currentScreen = "home" },
                    onNavigateToRegister = { currentScreen = "register" }
                )
                "register" -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = "home" },
                    onNavigateToLogin = { currentScreen = "login" }
                )
                "settings" -> MainScreen(onNavigate = { currentScreen = "home" })
                else -> MainScreen(onNavigate = { currentScreen = "home" })
            }
        }
    }
}
