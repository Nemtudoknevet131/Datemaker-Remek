package com.example.datemaker.game

object CardRepository {

    private val cardsMap = mutableMapOf(
        "Intimacy" to mutableListOf(
            "What makes you feel most loved?",
            "Describe a moment you felt closest to your partner.",
            "How can your partner support you better emotionally?"
        ),
        "18+" to mutableListOf(
            "What do you find most attractive about your partner?",
            "What is a fantasy you’ve never shared?",
            "Describe how you like to be kissed."
        ),
        "Funny" to mutableListOf(
            "Tell the worst joke you know.",
            "Imitate your partner for 10 seconds.",
            "What’s the dumbest thing you’ve ever done?"
        ),
        "Romantic" to mutableListOf(
            "What was your first impression of your partner?",
            "Describe your dream date.",
            "What do you love most about your partner?"
        ),
        "Deep Talk" to mutableListOf(
            "What scares you the most about the future?",
            "What’s something you never tell people?",
            "What makes you feel most insecure?"
        )
    )

    fun getCards(category: String): List<String> {
        return cardsMap[category] ?: listOf("No cards available for this category")
    }

    fun getCategories(): List<String> {
        return cardsMap.keys.toList()
    }

    fun addCard(category: String, cardContent: String) {
        if (!cardsMap.containsKey(category)) {
            cardsMap[category] = mutableListOf()
        }
        cardsMap[category]?.add(cardContent)
    }
}
