package sg.mirobotic.learnandroid

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import sg.mirobotic.learnandroid.databinding.ActivityTextToSpeechBinding
import java.util.*

class TextToSpeechActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextToSpeechBinding
    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextToSpeechBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSpeak.setOnClickListener {

            val text = binding.etText.text.toString().trim()

            if (text.isEmpty()) {
                binding.etText.error = "Enter text to speak"
                return@setOnClickListener
            }

            textToSpeech?.speak(text,TextToSpeech.QUEUE_FLUSH,null, null)
        }

        textToSpeech = TextToSpeech(
            applicationContext
        ) { i -> if (i != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.ENGLISH
            }
        }

    }

}