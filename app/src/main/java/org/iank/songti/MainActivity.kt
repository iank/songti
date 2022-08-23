package org.iank.songti

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.BufferedReader

private const val TAG = "SongTiMainActivity"

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

        vocab = Vocabulary(this)

        var rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener { rollDice() }
    }

    @RequiresApi(Build.VERSION_CODES.O) // XML font support
    private fun rollDice() {
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