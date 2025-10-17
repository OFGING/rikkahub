package me.rerere.rikkahub.ui.pages.backup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.rerere.ai.provider.Modality
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ModelAbility
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.sync.WebDavBackupItem
import me.rerere.rikkahub.data.sync.WebdavSync
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.UiState
import java.io.File

private const val TAG = "BackupVM"

class BackupVM(
    private val settingsStore: SettingsStore,
    private val webdavSync: WebdavSync,
) : ViewModel() {
    val settings = settingsStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Settings.dummy()
    )

    val webDavBackupItems = MutableStateFlow<UiState<List<WebDavBackupItem>>>(UiState.Idle)

    init {
        loadBackupFileItems()
    }

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsStore.update(settings)
        }
    }

    fun loadBackupFileItems() {
        viewModelScope.launch {
            runCatching {
                webDavBackupItems.emit(UiState.Loading)
                webDavBackupItems.emit(
                    value = UiState.Success(
                        data = webdavSync.listBackupFiles(
                            webDavConfig = settings.value.webDavConfig
                        ).sortedByDescending { it.lastModified }
                    )
                )
            }.onFailure {
                webDavBackupItems.emit(UiState.Error(it))
            }
        }
    }

    suspend fun testWebDav() {
        webdavSync.testWebdav(settings.value.webDavConfig)
    }

    suspend fun backup() {
        webdavSync.backupToWebDav(settings.value.webDavConfig)
    }

    suspend fun restore(item: WebDavBackupItem) {
        webdavSync.restoreFromWebDav(webDavConfig = settings.value.webDavConfig, item = item)
    }

    suspend fun deleteWebDavBackupFile(item: WebDavBackupItem) {
        webdavSync.deleteWebDavBackupFile(settings.value.webDavConfig, item)
    }

    suspend fun exportToFile(): File {
        return webdavSync.prepareBackupFile(settings.value.webDavConfig.copy())
    }

    suspend fun restoreFromLocalFile(file: File) {
        webdavSync.restoreFromLocalFile(file, settings.value.webDavConfig)
    }

    fun restoreFromChatBox(file: File) {
        val importProviders = arrayListOf<ProviderSetting>()

        val jsonElements = JsonInstant.parseToJsonElement(file.readText()).jsonObject
        val settingsObj = jsonElements["settings"]?.jsonObject
        if (settingsObj != null) {
            settingsObj["providers"]?.jsonObject?.let { providers ->
                providers["gemini"]?.jsonObject?.let { gemini ->
                    val apiHost = gemini["apiHost"]?.jsonPrimitive?.contentOrNull
                        ?: "https://generativelanguage.googleapis.com"
                    val apiKey = gemini["apiKey"]?.jsonPrimitive?.contentOrNull ?: ""
                    if (apiKey.isNotBlank()) importProviders.add(
                        ProviderSetting.Google(
                            name = "Gemini",
                            baseUrl = "$apiHost/v1beta",
                            apiKey = apiKey,
                        )
                    )
                }
            }
        }

        Log.i(TAG, "restoreFromChatBox: import ${importProviders.size} providers: $importProviders")

        updateSettings(
            settings.value.copy(
                providers = importProviders + settings.value.providers,
            )
        )
    }
}
