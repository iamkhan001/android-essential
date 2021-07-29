package sg.mirobotic.learnandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import sg.mirobotic.learnandroid.databinding.ActivitySpeechToTextBinding
import java.util.*


class SpeechToTextActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySpeechToTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpeechToTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textRecognizer = registerForActivityResult(
            StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val text = result.data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )

                text?.let {
                    binding.tvText.text = it[0]
                }

            }
        }

        binding.btnSpeak.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            textRecognizer.launch(intent)
        }

    }

}