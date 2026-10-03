package com.example.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isBanglaSupported = MutableStateFlow(false)
    val isBanglaSupported: StateFlow<Boolean> = _isBanglaSupported.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            // Test Bangla locale support
            val banglaLocale = Locale("bn", "BD")
            val result = tts?.isLanguageAvailable(banglaLocale)
            if (result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                tts?.language = banglaLocale
                _isBanglaSupported.value = true
            } else {
                // Fallback to English/default
                tts?.language = Locale.US
                _isBanglaSupported.value = false
            }
        } else {
            Log.e("SpeechManager", "TTS initialization failed code=$status")
        }
    }

    fun speak(text: String, rate: Float = 1.0f, pitch: Float = 1.0f, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) {
            onDone?.invoke()
            return
        }

        try {
            tts?.setSpeechRate(rate)
            tts?.setPitch(pitch)

            val containsBangla = text.any { it in '\u0980'..'\u09FF' }
            if (containsBangla) {
                val bnLocale = Locale("bn", "BD")
                if (tts?.isLanguageAvailable(bnLocale) == TextToSpeech.LANG_AVAILABLE ||
                    tts?.isLanguageAvailable(bnLocale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    tts?.language = bnLocale
                }
            } else {
                tts?.language = Locale.US
            }

            val utteranceId = "utt_${System.currentTimeMillis()}"
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("SpeechManager", "TTS speak failed", e)
            _isSpeaking.value = false
            onDone?.invoke()
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
