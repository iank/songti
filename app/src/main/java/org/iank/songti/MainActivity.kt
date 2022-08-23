package org.iank.songti

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.BufferedReader
import java.util.*

private const val TAG = "SongTiMainActivity"

/**
 * TODO: submit text and deal w/ it
 * TODO: submit text when enter is pressed
 * TODO: make sure keyboard doesn't cover layout
 */

/**
 * This activity displays a random vocabulary word in a random font, and presents
 * a text entry. Once the user inputs text it will mark it correct and move to the
 * next word/font, or mark it incorrect, display the answer, and allow the user to
 * proceed to the next word/font at their leisure.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var vocab: Vocabulary

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set preferred language for text input
        var guessInput: EditText = findViewById(R.id.editTextGuess)
        guessInput.setImeHintLocales(LocaleList(Locale("zh", "CN")))

        // Load vocabulary and display an initial word
        vocab = Vocabulary(this)
        updateWord()

        // Set up button click action
        var rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener { updateWord() }
    }

    @RequiresApi(Build.VERSION_CODES.O) // XML font support
    private fun updateWord() {
        // Access vocabulary storage and select a word
        val vocabWord = vocab.roll()

        // Update screen with new word
        val resultTextView: TextView = findViewById(R.id.textView)
        resultTextView.text = vocabWord

        // Select a font and update style
        val typeface = Fonts().roll()
        resultTextView.typeface = resources.getFont(typeface.num)

        // Update typeface name on screen
        val typefaceNameView: TextView = findViewById(R.id.typefaceNameView)
        typefaceNameView.text = typeface.name
    }
}

/**
 * This class models the available fonts
 */
class Fonts() {
    fun roll(): SongTiTypeface {
        val fontFields = R.font::class.java.fields
        val fonts = arrayListOf<SongTiTypeface>()

        for (field in fontFields) {
            try {
                fonts.add(SongTiTypeface(field.name, field.getInt(null)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return fonts.random()
    }
}

/**
 * Just a name<->Int mapping
 */
class SongTiTypeface(val name: String, val num: Int) {
}

/**
 * This class models the vocabulary storage
 *
 * TODO: import from Skritter
 */
class Vocabulary(context: Context) {
    val vocabList: MutableList<String> = mutableListOf()

    init {
        val reader = context.assets.open("words.txt").bufferedReader()
        reader.forEachLine {
            vocabList.add(it)
        }
    }

    fun roll(): String {
        return vocabList.random()
    }
}