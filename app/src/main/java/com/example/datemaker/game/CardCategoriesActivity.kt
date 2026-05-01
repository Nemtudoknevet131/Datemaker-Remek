package com.example.datemaker.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.datemaker.databinding.ActivityCardCategoriesBinding

class CardCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshButtons()

        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun refreshButtons() {
        binding.buttonsContainer.removeAllViews()
        // Add Title back (it was removed by removeAllViews)
        val title = android.widget.TextView(this).apply {
            text = "Choose a Category"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 26f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 100)
            }
        }
        binding.buttonsContainer.addView(title)

        val categories = CardRepository.getCategories()
        for (category in categories) {
            val btn = Button(this).apply {
                text = category
                setTextColor(android.graphics.Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 50)
                }
                setOnClickListener { openDeck(category) }
            }
            binding.buttonsContainer.addView(btn)
        }
    }

    private fun showAddDialog() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val spinner = Spinner(this)
        val categories = CardRepository.getCategories().toMutableList()
        categories.add("New Category...")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinner.adapter = adapter
        dialogView.addView(spinner)

        val newCategoryInput = EditText(this).apply {
            hint = "Enter new category name"
            visibility = View.GONE
        }
        dialogView.addView(newCategoryInput)

        val cardInput = EditText(this).apply {
            hint = "Enter card text"
        }
        dialogView.addView(cardInput)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (categories[position] == "New Category...") {
                    newCategoryInput.visibility = View.VISIBLE
                } else {
                    newCategoryInput.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Card")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val selectedCategory = spinner.selectedItem.toString()
                val cardText = cardInput.text.toString()

                if (cardText.isNotEmpty()) {
                    if (selectedCategory == "New Category...") {
                        val newCat = newCategoryInput.text.toString()
                        if (newCat.isNotEmpty()) {
                            CardRepository.addCard(newCat, cardText)
                            refreshButtons()
                        }
                    } else {
                        CardRepository.addCard(selectedCategory, cardText)
                    }
                    Toast.makeText(this, "Card Added!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openDeck(category: String) {
        val i = Intent(this, CardDrawActivity::class.java)
        i.putExtra("category", category)
        startActivity(i)
    }
}
