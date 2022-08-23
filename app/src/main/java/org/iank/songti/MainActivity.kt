package org.iank.songti

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.BufferedReader
import java.util.*
import net.sourceforge.pinyin4j.PinyinHelper;

private const val TAG = "SongTiMainActivity"

/**
 * TODO: make sure keyboard doesn't cover layout
 * TODO: stats per font, weight randomness
 */

/**
 * This activity displays a random vocabulary word in a random font, and presents
 * a text entry. Once the user inputs text it will mark it correct and move to the
 * next word/font, or mark it incorrect, display the answer, and allow the user to
 * proceed to the next word/font at their leisure.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var vocab: Vocabulary

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
        rollButton.setOnClickListener {
            checkGuess()
            updateWord()
            clearInput()
        }

        // Set up text input action
        guessInput.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                rollButton.performClick()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun clearInput() {
        var guessInput: EditText = findViewById(R.id.editTextGuess)
        guessInput.getText().clear()
    }

    /**
     * TODO: don't queue toasts
     * TODO: better output display (pause so I can see char)
     */
    private fun checkGuess() {
        var guessInput: EditText = findViewById(R.id.editTextGuess)
        var answerDisplay: TextView = findViewById(R.id.textView)
        if (guessInput.text.toString().equals(answerDisplay.text.toString())) {
            Toast.makeText(this, "correct: ${answerDisplay.text}", Toast.LENGTH_SHORT).show()
        } else {
            Log.i(TAG, "wrong: [${guessInput.text}] != [${answerDisplay.text}]")
            Toast.makeText(this, "wrong: ${guessInput.text} != ${answerDisplay.text} (${toPinyin(answerDisplay.text.toString())})", Toast.LENGTH_SHORT).show()
        }
    }

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
 * TODO: accent type
 * TODO: pick one
 */
fun toPinyin(hanzi: String): String {
    val sb = StringBuilder()
    for (ch in hanzi.iterator()) {
        var py = PinyinHelper.toHanyuPinyinStringArray(ch)
        sb.append(py.joinToString())
    }
    return sb.toString()
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