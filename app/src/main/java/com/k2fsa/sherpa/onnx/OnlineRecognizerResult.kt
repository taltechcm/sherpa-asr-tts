package com.k2fsa.sherpa.onnx

data class OnlineRecognizerResult(
    val text: String,
    val tokens: Array<String>,
    val timestamps: FloatArray,
    val ysProbs: FloatArray,
    // TODO(fangjun): Add more fields
)