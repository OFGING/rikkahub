package me.rerere.rikkahub.data.datastore

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import me.rerere.ai.provider.BalanceOption
import me.rerere.ai.provider.Modality
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ModelAbility
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import kotlin.uuid.Uuid

val GEMINI_2_5_PRO_ID = Uuid.parse("e2a1b0c9-9c8d-4f7e-a6b5-3d2c1b0a9f8e")
val GEMINI_2_5_FLASH_ID = Uuid.parse("a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6")

val DEFAULT_PROVIDERS = listOf(
    ProviderSetting.Google(
        id = Uuid.parse("6ab18148-c138-4394-a46f-1cd8c8ceaa6d"),
        name = "Gemini",
        apiKey = "",
        enabled = true,
        builtIn = true,
        models = listOf(
            Model(
                id = GEMINI_2_5_PRO_ID,
                modelId = "gemini-2.5-pro",
                displayName = "Gemini 2.5 Pro"
            ),
            Model(
                id = GEMINI_2_5_FLASH_ID,
                modelId = "gemini-2.5-flash",
                displayName = "Gemini 2.5 Flash"
            )
        )
    )
)
