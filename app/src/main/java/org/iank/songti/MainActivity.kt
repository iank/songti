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
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

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
     * TODO: color output
     */
    private fun checkGuess() {
        var guessInput: EditText = findViewById(R.id.editTextGuess)
        var answerDisplay: TextView = findViewById(R.id.textView)
        var prevResultView: TextView = findViewById(R.id.prevResultView)

        var resultString = ""
        if (guessInput.text.toString().equals(answerDisplay.text.toString())) {
            resultString = "correct: ${answerDisplay.text}"
        } else {
            Log.i(TAG, "wrong: [${guessInput.text}] != [${answerDisplay.text}]")
            resultString = ("wrong: ${guessInput.text} != ${answerDisplay.text}" +
                            "(${toPinyin(answerDisplay.text.toString())})")
        }
        prevResultView.text = resultString
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
 * Return a pinyin representation of the input string.
 *
 * !! Note that in cases when characters have multiple pronunciations we just return the first
 * from a list, which could be wrong.
 */
fun toPinyin(hanzi: String): String {
    // Set up output format
    val outputFormat = HanyuPinyinOutputFormat()
    outputFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK)
    outputFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE)

    // Build pinyin string from input
    val sb = StringBuilder()
    for (ch in hanzi.iterator()) {
        var py = PinyinHelper.toHanyuPinyinStringArray(ch, outputFormat)
        sb.append(py.first())
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