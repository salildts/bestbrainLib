package com.app.bestbrain.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast

class AudioRecording(
    private val context: Context?,
    private val recordCompleteListener: RecordCompleteListener
) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun initAudioRecording() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // This is triggered as audio input changes
            }

            override fun onBufferReceived(buffer: ByteArray) {
                // Handle audio buffer
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech ended")
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error: $error")
                recordCompleteListener.onRecordError("")
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recordCompleteListener.onRecordComplete(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle) {
                // Handle partial results if required
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                // Handle other events if required
            }
        })
    }

    fun startRecording() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        speechRecognizer!!.startListening(intent)
    }

    fun stopRecording() {
        speechRecognizer!!.stopListening()
    }

    fun destroyAudioRecording() {
        speechRecognizer!!.destroy()
    }

    interface RecordCompleteListener {
        fun onRecordComplete(outputText: String?)

        fun onRecordError(outputText: String?)
    }
}