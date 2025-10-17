package me.rerere.ai.registry

import me.rerere.ai.provider.Modality
import me.rerere.ai.provider.ModelAbility

fun interface ModelData<T> {
    fun getData(modelId: String): T
}

object ModelRegistry {
    private val GEMINI_20_FLASH = ModelMatcher.containsRegex("gemini-2.0-flash")
    val GEMINI_2_5_FLASH = ModelMatcher.containsRegex("gemini-2.5-flash") and ModelMatcher.containsRegex("image", negated = true)
    val GEMINI_2_5_PRO = ModelMatcher.containsRegex("gemini-2.5-pro")
    val GEMINI_2_5_IMAGE = ModelMatcher.containsRegex("gemini-2.5-flash-image")
    val GEMINI_FLASH_LATEST = ModelMatcher.exact("gemini-flash-latest")
    val GEMINI_PRO_LATEST = ModelMatcher.exact("gemini-pro-latest")
    val GEMINI_LATEST = GEMINI_FLASH_LATEST + GEMINI_PRO_LATEST
    val GEMINI_SERIES = GEMINI_20_FLASH + GEMINI_2_5_FLASH + GEMINI_2_5_PRO + GEMINI_LATEST

    val VISION_MODELS = GEMINI_SERIES
    val TOOL_MODELS = GEMINI_SERIES
    val REASONING_MODELS = GEMINI_2_5_FLASH + GEMINI_2_5_PRO  + GEMINI_LATEST
    val CHAT_IMAGE_GEN_MODELS = GEMINI_2_5_IMAGE

    val MODEL_INPUT_MODALITIES = ModelData { modelId ->
        if (VISION_MODELS.match(modelId)) {
            listOf(Modality.TEXT, Modality.IMAGE)
        } else {
            listOf(Modality.TEXT)
        }
    }

    val MODEL_OUTPUT_MODALITIES = ModelData { modelId ->
        if(CHAT_IMAGE_GEN_MODELS.match(modelId)) {
            listOf(Modality.TEXT, Modality.IMAGE)
        } else {
            listOf(Modality.TEXT)
        }
    }

    val MODEL_ABILITIES = ModelData { modelId ->
        buildList {
            if (TOOL_MODELS.match(modelId)) {
                add(ModelAbility.TOOL)
            }
            if (REASONING_MODELS.match(modelId)) {
                add(ModelAbility.REASONING)
            }
        }
    }
}
