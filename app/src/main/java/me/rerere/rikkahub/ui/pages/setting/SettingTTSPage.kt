package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CircleStop
import com.composables.icons.lucide.GripHorizontal
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings2
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Volume2
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.DEFAULT_SYSTEM_TTS_ID
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.AutoAIIcon
import me.rerere.rikkahub.ui.components.ui.Tag
import me.rerere.rikkahub.ui.components.ui.TagType
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalTTSState
import me.rerere.rikkahub.ui.pages.setting.components.TTSProviderConfigure
import me.rerere.rikkahub.utils.plus
import me.rerere.tts.provider.TTSProviderSetting
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SettingTTSPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val navController = LocalNavController.current
    var editingProvider by remember { mutableStateOf<TTSProviderSetting?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.setting_tts_page_title))
                },
                navigationIcon = {
                    BackButton()
                },
                actions = {
                    AddTTSProviderButton {
                        vm.updateSettings(
                            settings.copy(
                                ttsProviders = listOf(it) + settings.ttsProviders
                            )
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val newProviders = settings.ttsProviders.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            vm.updateSettings(settings.copy(ttsProviders = newProviders))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = innerPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyListState
        ) {
            items(settings.ttsProviders, key = { it.id }) { provider ->
                ReorderableItem(
                    state = reorderableState,
                    key = provider.id
                ) { isDragging ->
                    TTSProviderItem(
                        modifier = Modifier
                            .scale(if (isDragging) 0.95f else 1f)
                            .fillMaxWidth(),
                        provider = provider,
                        dragHandle = {
                            val haptic = LocalHapticFeedback.current
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .longPressDraggableHandle(
                                        onDragStarted = {
                                            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = Lucide.GripHorizontal,
                                    contentDescription = null
                                )
                            }
                        },
                        isSelected = settings.selectedTTSProviderId == provider.id,
                        onSelect = {
                            vm.updateSettings(settings.copy(selectedTTSProviderId = provider.id))
                        },
                        onEdit = {
                            editingProvider = provider
                        },
                        onDelete = {
                            val newProviders = settings.ttsProviders - provider
                            val newSelectedId =
                                if (settings.selectedTTSProviderId == provider.id) DEFAULT_SYSTEM_TTS_ID else settings.selectedTTSProviderId
                            vm.updateSettings(
                                settings.copy(
                                    ttsProviders = newProviders,
                                    selectedTTSProviderId = newSelectedId
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    // Edit TTS Provider Bottom Sheet
    editingProvider?.let { provider ->
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var currentProvider by remember(provider) { mutableStateOf(provider) }

        ModalBottomSheet(
            onDismissRequest = {
                editingProvider = null
            },
            sheetState = bottomSheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(0.8f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.setting_tts_page_edit_provider),
                    style = MaterialTheme.typography.headlineSmall
                )

                TTSProviderConfigure(
                    setting = currentProvider,
                    onValueChange = { newState ->
                        currentProvider = newState
                    },
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            editingProvider = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            val newProviders = settings.ttsProviders.map {
                                if (it.id == provider.id) currentProvider else it
                            }
                            vm.updateSettings(settings.copy(ttsProviders = newProviders))
                            editingProvider = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.chat_page_save))
                    }
                }
            }
        }
    }
}
