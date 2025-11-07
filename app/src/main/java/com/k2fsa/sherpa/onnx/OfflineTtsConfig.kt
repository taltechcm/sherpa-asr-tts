package com.k2fsa.sherpa.onnx

data class OfflineTtsConfig(
    var model: OfflineTtsModelConfig = OfflineTtsModelConfig(),
    var ruleFsts: String = "",
    var ruleFars: String = "",
    var maxNumSentences: Int = 1,
    var silenceScale: Float = 0.2f,
) {
    companion object {
        // please refer to
        // https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/index.html
        // to download models
        fun getOfflineTtsConfig(
            modelDir: String,
            modelName: String, // for VITS
            acousticModelName: String, // for Matcha
            vocoder: String, // for Matcha
            voices: String, // for Kokoro or kitten
            lexicon: String,
            dataDir: String,
            dictDir: String, // unused
            ruleFsts: String,
            ruleFars: String,
            numThreads: Int? = null,
            isKitten: Boolean = false
        ): OfflineTtsConfig {
            // For Matcha TTS, please set
            // acousticModelName, vocoder

            // For Kokoro TTS, please set
            // modelName, voices

            // For Kitten TTS, please set
            // modelName, voices, isKitten

            // For VITS, please set
            // modelName

            val numberOfThreads = if (numThreads != null) {
                numThreads
            } else if (voices.isNotEmpty()) {
                // for Kokoro and Kitten TTS models, we use more threads
                4
            } else {
                2
            }

            if (modelName.isEmpty() && acousticModelName.isEmpty()) {
                throw IllegalArgumentException("Please specify a TTS model")
            }

            if (modelName.isNotEmpty() && acousticModelName.isNotEmpty()) {
                throw IllegalArgumentException("Please specify either a VITS or a Matcha model, but not both")
            }

            if (acousticModelName.isNotEmpty() && vocoder.isEmpty()) {
                throw IllegalArgumentException("Please provide vocoder for Matcha TTS")
            }

            val vits = if (modelName.isNotEmpty() && voices.isEmpty()) {
                OfflineTtsVitsModelConfig(
                    model = "$modelDir/$modelName",
                    lexicon = "$modelDir/$lexicon",
                    tokens = "$modelDir/tokens.txt",
                    dataDir = dataDir,
                )
            } else {
                OfflineTtsVitsModelConfig()
            }

            val matcha = if (acousticModelName.isNotEmpty()) {
                OfflineTtsMatchaModelConfig(
                    acousticModel = "$modelDir/$acousticModelName",
                    vocoder = vocoder,
                    lexicon = "$modelDir/$lexicon",
                    tokens = "$modelDir/tokens.txt",
                    dataDir = dataDir,
                )
            } else {
                OfflineTtsMatchaModelConfig()
            }

            val kokoro = if (voices.isNotEmpty() && !isKitten) {
                OfflineTtsKokoroModelConfig(
                    model = "$modelDir/$modelName",
                    voices = "$modelDir/$voices",
                    tokens = "$modelDir/tokens.txt",
                    dataDir = dataDir,
                    lexicon = when {
                        lexicon == "" -> lexicon
                        "," in lexicon -> lexicon
                        else -> "$modelDir/$lexicon"
                    },
                )
            } else {
                OfflineTtsKokoroModelConfig()
            }

            val kitten = if (isKitten) {
                OfflineTtsKittenModelConfig(
                    model = "$modelDir/$modelName",
                    voices = "$modelDir/$voices",
                    tokens = "$modelDir/tokens.txt",
                    dataDir = dataDir,
                )
            } else {
                OfflineTtsKittenModelConfig()
            }

            return OfflineTtsConfig(
                model = OfflineTtsModelConfig(
                    vits = vits,
                    matcha = matcha,
                    kokoro = kokoro,
                    kitten = kitten,
                    numThreads = numberOfThreads,
                    debug = true,
                    provider = "cpu",
                ),
                ruleFsts = ruleFsts,
                ruleFars = ruleFars,
            )
        }
    }
}