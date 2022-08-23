package org.iank.songti

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi

private const val TAG = "SongTiMainActivity"

/**
 * This activity displays a random vocabulary word in a random font, and presents
 * a text entry. Once the user inputs text it will mark it correct and move to the
 * next word/font, or mark it incorrect, display the answer, and allow the user to
 * proceed to the next word/font at their leisure.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var rollButton: Button = findViewById(R.id.button)
        rollButton.setOnClickListener { rollDice() }
    }

    @RequiresApi(Build.VERSION_CODES.O) // XML font support
    private fun rollDice() {
        // Access vocabulary storage and select a word
        val vocabWord = Vocabulary().roll()

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
 * TODO: database/persistence. Singleton pattern or something
 * TODO: import from Skritter/text file
 */
class Vocabulary() {
    val vocabList = listOf("宋体", "你好", "推荐", "苹果")
    fun roll(): String {
        return vocabList.random()
    }
}