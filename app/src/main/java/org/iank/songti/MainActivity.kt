package org.iank.songti

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.*
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

private const val TAG = "SongTiMainActivity"

/**
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
        val guessInput: EditText = findViewById(R.id.editTextGuess)
        guessInput.imeHintLocales = LocaleList(Locale("zh", "CN"))

        // Load vocabulary and display an initial word
        vocab = Vocabulary(this)
        updateWord()

        // Set up button click action
        val rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener {
            checkGuess()
            clearInput()
        }

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            clearInput()
            clearResult()
            updateWord()
        }

        // Set up text input action
        guessInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (isGuessEmpty()) { nextButton.performClick() }
                else { rollButton.performClick() }

                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun isGuessEmpty(): Boolean {
        val guessInput: EditText = findViewById(R.id.editTextGuess)
        return guessInput.text.isEmpty()
    }

    private fun clearInput() {
        val guessInput: EditText = findViewById(R.id.editTextGuess)
        guessInput.text.clear()
    }

    private fun clearResult() {
        val prevResultView: TextView = findViewById(R.id.prevResultView)
        prevResultView.text = ""
    }

    private fun checkGuess() {
        val guessInput: EditText = findViewById(R.id.editTextGuess)
        val answerDisplay: TextView = findViewById(R.id.textView)
        val prevResultView: TextView = findViewById(R.id.prevResultView)

        val resultString: String
        if (guessInput.text.toString() == answerDisplay.text.toString()) {
            resultString = "correct: ${answerDisplay.text}"
            prevResultView.setTextColor(Color.rgb(0, 0, 0))
        } else {
            Log.i(TAG, "wrong: [${guessInput.text}] != [${answerDisplay.text}]")
            resultString = ("wrong: ${guessInput.text} != ${answerDisplay.text}" +
                            "(${toPinyin(answerDisplay.text.toString())})")
            prevResultView.setTextColor(Color.rgb(200, 0, 0))
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
    outputFormat.toneType = HanyuPinyinToneType.WITH_TONE_MARK
    outputFormat.vCharType = HanyuPinyinVCharType.WITH_U_UNICODE

    // Build pinyin string from input
    val sb = StringBuilder()
    for (ch in hanzi.iterator()) {
        val py = PinyinHelper.toHanyuPinyinStringArray(ch, outputFormat)
        sb.append(py.first())
    }
    return sb.toString()
}

/**
 * This class models the available fonts
 */
class Fonts {
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
class SongTiTypeface(val name: String, val num: Int)

/**
 * This class models the vocabulary storage
 *
 * TODO: import from Skritter
 */
class Vocabulary(context: Context) {
    private val vocabList: MutableList<String> = mutableListOf()

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