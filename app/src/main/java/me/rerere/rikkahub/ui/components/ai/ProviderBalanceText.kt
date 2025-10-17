package me.rerere.rikkahub.ui.components.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import me.rerere.ai.provider.ProviderSetting

@Composable
fun ProviderBalanceText(
    providerSetting: ProviderSetting,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    // Gemini does not support balance checking, so this is a no-op.
}
