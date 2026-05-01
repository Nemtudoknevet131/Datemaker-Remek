package com.example.datemaker.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FlappyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameView = FlappyGameView(this)

        val avatarUrl = intent.getStringExtra("AVATAR_URL")
        gameView.setBirdAvatar(avatarUrl)

        setContentView(gameView)
    }
}
