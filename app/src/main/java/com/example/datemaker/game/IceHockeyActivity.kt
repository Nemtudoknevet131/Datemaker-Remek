package com.example.datemaker.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class IceHockeyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameView = IceHockeyGameView(this)
        setContentView(gameView)
    }
}
