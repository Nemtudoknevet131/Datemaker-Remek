package com.example.datemaker.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datemaker.game.TankTroubleGameView
class TankTroubleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameView = TankTroubleGameView(this)
        setContentView(gameView)
    }
}
