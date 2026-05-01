package com.example.datemaker.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datemaker.databinding.CardDrawBinding
import kotlin.random.Random



class CardDrawActivity : AppCompatActivity() {

    private lateinit var binding: CardDrawBinding
    private lateinit var cards: MutableList<String>
    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CardDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra("category") ?: "Unknown"
        binding.txtCategory.text = category

        cards = CardRepository.getCards(category).toMutableList()

        binding.btnDraw.setOnClickListener {
            drawCard()
        }
    }

    private fun drawCard() {
        if (cards.isEmpty()) {
            binding.txtCard.text = "Deck empty — reshuffling!"
            cards = CardRepository.getCards(category).toMutableList()
            return
        }

        val index = Random.nextInt(cards.size)
        val question = cards[index]

        binding.txtCard.text = question
        cards.removeAt(index)
    }
}
