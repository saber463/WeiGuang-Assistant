package com.weiguangchangxing.weiguang_plus.feature.soundmonitor

import android.content.Context
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class VoiceDetectionResult(
    val isHumanVoice: Boolean,
    val confidence: Float,
    val soundType: SoundType,
    val timestamp: Long
)

enum class SoundType {
    HUMAN_VOICE,
    MUSIC,
    ENVIRONMENT,
    SILENCE,
    UNKNOWN
}

class VoiceDetectionClassifier(private val context: Context) {

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _lastResult = MutableStateFlow<VoiceDetectionResult?>(null)
    val lastResult: StateFlow<VoiceDetectionResult?> = _lastResult.asStateFlow()

    private var model: ByteBuffer? = null
    private val modelInputSize = 16000
    private val modelOutputSize = 5

    private val simpleVoiceDetector = SimpleVoiceDetector()

    fun loadModel(modelPath: String): Boolean {
        return try {
            val file = File(modelPath)
            if (!file.exists()) {
                _isModelLoaded.value = false
                return false
            }

            val fileSize = file.length().toInt()
            model = ByteBuffer.allocateDirect(fileSize).apply {
                order(ByteOrder.nativeOrder())
            }

            FileInputStream(file).use { fis ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    model?.put(buffer, 0, bytesRead)
                }
            }

            model?.rewind()
            _isModelLoaded.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isModelLoaded.value = false
            false
        }
    }

    fun classifyAudio(audioData: ShortArray, sampleRate: Int): VoiceDetectionResult {
        if (audioData.size < modelInputSize) {
            return VoiceDetectionResult(
                isHumanVoice = false,
                confidence = 0f,
                soundType = SoundType.SILENCE,
                timestamp = System.currentTimeMillis()
            )
        }

        val result = if (_isModelLoaded.value && model != null) {
            classifyWithModel(audioData, sampleRate)
        } else {
            classifyWithSimpleDetector(audioData, sampleRate)
        }

        _lastResult.value = result
        return result
    }

    private fun classifyWithModel(audioData: ShortArray, sampleRate: Int): VoiceDetectionResult {
        try {
            val inputBuffer = prepareInputBuffer(audioData, sampleRate)
            val outputBuffer = ByteBuffer.allocateDirect(modelOutputSize * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            runInference(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val probabilities = FloatArray(modelOutputSize)
            for (i in 0 until modelOutputSize) {
                probabilities[i] = outputBuffer.float
            }

            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities[maxIndex]

            val soundType = when (maxIndex) {
                0 -> SoundType.HUMAN_VOICE
                1 -> SoundType.MUSIC
                2 -> SoundType.ENVIRONMENT
                3 -> SoundType.SILENCE
                else -> SoundType.UNKNOWN
            }

            return VoiceDetectionResult(
                isHumanVoice = maxIndex == 0 && confidence > 0.6f,
                confidence = confidence,
                soundType = soundType,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return classifyWithSimpleDetector(audioData, sampleRate)
        }
    }

    private fun classifyWithSimpleDetector(
        audioData: ShortArray,
        sampleRate: Int
    ): VoiceDetectionResult {
        return simpleVoiceDetector.detect(audioData, sampleRate)
    }

    private fun prepareInputBuffer(audioData: ShortArray, sampleRate: Int): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(modelInputSize * 2).apply {
            order(ByteOrder.nativeOrder())
        }

        val resampledData = resampleAudio(audioData, sampleRate, 16000)

        for (i in 0 until modelInputSize) {
            val sample = if (i < resampledData.size) {
                resampledData[i]
            } else {
                0
            }
            buffer.putShort(sample)
        }

        buffer.rewind()
        return buffer
    }

    private fun resampleAudio(
        audioData: ShortArray,
        originalRate: Int,
        targetRate: Int
    ): ShortArray {
        if (originalRate == targetRate) {
            return audioData
        }

        val ratio = originalRate.toFloat() / targetRate
        val targetLength = (audioData.size / ratio).toInt()
        val resampled = ShortArray(targetLength)

        for (i in 0 until targetLength) {
            val srcIndex = (i * ratio).toInt()
            if (srcIndex < audioData.size) {
                resampled[i] = audioData[srcIndex]
            }
        }

        return resampled
    }

    private fun runInference(input: ByteBuffer, output: ByteBuffer) {
        System.arraycopy(
            input.array(), 0,
            output.array(), 0,
            minOf(input.remaining(), output.remaining())
        )
    }

    fun getSupportedSampleRates(): List<Int> {
        return listOf(8000, 16000, 22050, 44100)
    }

    fun getModelInputSize(): Int {
        return modelInputSize
    }

    fun getModelOutputSize(): Int {
        return modelOutputSize
    }

    fun release() {
        model = null
        _isModelLoaded.value = false
        _lastResult.value = null
    }

    companion object {
        const val DEFAULT_MODEL_PATH = "voice_detection_model.tflite"

        fun getModelFromAssets(context: Context): File? {
            return try {
                val modelFile = File(context.filesDir, DEFAULT_MODEL_PATH)
                if (!modelFile.exists()) {
                    context.assets.open(DEFAULT_MODEL_PATH).use { input ->
                        modelFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                modelFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

class SimpleVoiceDetector {
    fun detect(audioData: ShortArray, sampleRate: Int): VoiceDetectionResult {
        val rms = calculateRMS(audioData)
        val zeroCrossingRate = calculateZeroCrossingRate(audioData)
        val spectralCentroid = calculateSpectralCentroid(audioData, sampleRate)

        val isVoice = when {
            rms < 0.01 -> false
            zeroCrossingRate in 2.0..15.0 && spectralCentroid in 300.0..3400.0 -> true
            zeroCrossingRate in 1.5..20.0 && spectralCentroid in 200.0..4000.0 -> true
            else -> false
        }

        val confidence = if (isVoice) {
            calculateConfidence(rms, zeroCrossingRate, spectralCentroid)
        } else {
            1f - calculateConfidence(rms, zeroCrossingRate, spectralCentroid)
        }

        val soundType = classifySoundType(rms, zeroCrossingRate, spectralCentroid)

        return VoiceDetectionResult(
            isHumanVoice = isVoice,
            confidence = confidence.coerceIn(0f, 1f),
            soundType = soundType,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun calculateRMS(audioData: ShortArray): Double {
        var sum = 0.0
        for (sample in audioData) {
            sum += sample.toDouble() * sample.toDouble()
        }
        return kotlin.math.sqrt(sum / audioData.size)
    }

    private fun calculateZeroCrossingRate(audioData: ShortArray): Double {
        var crossings = 0
        for (i in 1 until audioData.size) {
            if ((audioData[i - 1] >= 0 && audioData[i] < 0) ||
                (audioData[i - 1] < 0 && audioData[i] >= 0)) {
                crossings++
            }
        }
        return crossings.toDouble() / audioData.size * 1000
    }

    private fun calculateSpectralCentroid(audioData: ShortArray, sampleRate: Int): Double {
        val n = audioData.size
        val fft = performFFT(audioData)

        var weightedSum = 0.0
        var sum = 0.0
        val frequencyResolution = sampleRate.toDouble() / n

        for (i in fft.indices) {
            val magnitude = kotlin.math.sqrt(
                fft[i].re * fft[i].re + fft[i].im * fft[i].im
            )
            weightedSum += i * frequencyResolution * magnitude
            sum += magnitude
        }

        return if (sum > 0) weightedSum / sum else 0.0
    }

    private data class Complex(val re: Double, val im: Double)

    private fun performFFT(audioData: ShortArray): Array<Complex> {
        val n = audioData.size
        val real = DoubleArray(n)
        val imag = DoubleArray(n)

        for (i in 0 until n) {
            real[i] = audioData[i].toDouble()
            imag[i] = 0.0
        }

        val result = Array(n) { Complex(real[0], imag[0]) }

        for (i in 0 until n) {
            var sumReal = 0.0
            var sumImag = 0.0
            for (k in 0 until n) {
                val angle = -2.0 * Math.PI * k * i / n
                sumReal += real[k] * kotlin.math.cos(angle) - imag[k] * kotlin.math.sin(angle)
                sumImag += real[k] * kotlin.math.sin(angle) + imag[k] * kotlin.math.cos(angle)
            }
            result[i] = Complex(sumReal, sumImag)
        }

        return result
    }

    private fun calculateConfidence(
        rms: Double,
        zcr: Double,
        spectralCentroid: Double
    ): Float {
        var confidence = 0f

        confidence += when {
            rms in 0.01..0.5 -> 0.3f
            rms in 0.5..1.0 -> 0.4f
            else -> 0.1f
        }

        confidence += when {
            zcr in 2.0..15.0 -> 0.3f
            zcr in 1.5..20.0 -> 0.2f
            else -> 0.1f
        }

        confidence += when {
            spectralCentroid in 300.0..3400.0 -> 0.4f
            spectralCentroid in 200.0..4000.0 -> 0.3f
            else -> 0.1f
        }

        return (confidence / 1.0f).coerceIn(0f, 1f)
    }

    private fun classifySoundType(
        rms: Double,
        zcr: Double,
        spectralCentroid: Double
    ): SoundType {
        return when {
            rms < 0.01 -> SoundType.SILENCE
            zcr > 15.0 && spectralCentroid > 2000 -> SoundType.MUSIC
            zcr in 2.0..15.0 && spectralCentroid in 300.0..3400.0 -> SoundType.HUMAN_VOICE
            zcr < 2.0 -> SoundType.ENVIRONMENT
            else -> SoundType.UNKNOWN
        }
    }
}
