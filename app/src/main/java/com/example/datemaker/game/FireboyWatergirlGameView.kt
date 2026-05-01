package com.example.datemaker.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.example.datemaker.utils.AdPopupManager

class FireboyWatergirlGameView(context: Context) : View(context) {

    private val paint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // Colors
    private val fireColor = Color.RED
    private val waterColor = Color.BLUE
    private val platformColor = Color.DKGRAY
    private val goalColor = Color.GREEN

    // Game Objects
    private data class Player(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        val color: Int,
        var isGrounded: Boolean = false
    )

    private data class Platform(val rect: RectF)
    private data class Diamond(val x: Float, val y: Float, val color: Int, var collected: Boolean = false)
    private data class Door(val rect: RectF, val color: Int)

    private val fireboy = Player(100f, 600f, 0f, 0f, fireColor)
    private val watergirl = Player(100f, 800f, 0f, 0f, waterColor)

    private val platforms = mutableListOf<Platform>()
    private val diamonds = mutableListOf<Diamond>()
    private val doors = mutableListOf<Door>()

    // Physics
    private val gravity = 1.5f
    private val jumpForce = -35f
    private val moveSpeed = 10f
    private val playerSize = 60f

    // Controls
    private val buttons = mutableListOf<Button>()
    
    private data class Button(
        val rect: RectF, 
        val label: String, 
        val action: (Boolean) -> Unit
    )

    // Input State
    private var p1Left = false
    private var p1Right = false
    private var p2Left = false
    private var p2Right = false
    
    private var levelComplete = false
    private var adShown = false

    init {
        setupLevel()
    }

    private fun setupLevel() {
        if (width == 0 || height == 0) return

        platforms.clear()
        diamonds.clear()
        doors.clear()

        val w = width.toFloat()
        val h = height.toFloat()
        
        // Ground
        platforms.add(Platform(RectF(0f, h - 200f, w, h - 150f)))
        
        // Platforms (Lowered for easier jumping)
        platforms.add(Platform(RectF(300f, h - 500f, 800f, h - 450f)))
        platforms.add(Platform(RectF(0f, h - 800f, 400f, h - 750f)))
        platforms.add(Platform(RectF(600f, h - 1100f, 1000f, h - 1050f)))

        // Diamonds
        diamonds.add(Diamond(350f, h - 550f, fireColor))
        diamonds.add(Diamond(700f, h - 850f, waterColor))

        // Doors
        doors.add(Door(RectF(900f, h - 1200f, 980f, h - 1100f), fireColor))
        doors.add(Door(RectF(50f, h - 900f, 130f, h - 800f), waterColor))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        setupLevel()
        // Reset players to ground
        fireboy.x = 100f; fireboy.y = h.toFloat() - 300f
        watergirl.x = 200f; watergirl.y = h.toFloat() - 300f
        
        buttons.clear()
        val btnSize = 120f
        val margin = 30f
        val yPos = h - btnSize - margin

        // Fireboy Controls (Right side)
        buttons.add(Button(RectF(w - btnSize*3 - margin*3, yPos, w - btnSize*2 - margin*3, yPos + btnSize), "<") { p1Left = it })
        buttons.add(Button(RectF(w - btnSize*2 - margin*2, yPos, w - btnSize - margin*2, yPos + btnSize), ">") { p1Right = it })
        buttons.add(Button(RectF(w - btnSize - margin, yPos, w - margin, yPos + btnSize), "^") { if(it && fireboy.isGrounded) fireboy.vy = jumpForce })

        // Watergirl Controls (Left side)
        buttons.add(Button(RectF(margin, yPos, margin + btnSize, yPos + btnSize), "<") { p2Left = it })
        buttons.add(Button(RectF(margin + btnSize + margin, yPos, margin + btnSize*2 + margin, yPos + btnSize), ">") { p2Right = it })
        buttons.add(Button(RectF(margin + btnSize*2 + margin*2, yPos, margin + btnSize*3 + margin*2, yPos + btnSize), "^") { if(it && watergirl.isGrounded) watergirl.vy = jumpForce })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        updatePhysics()

        // Draw Platforms
        paint.color = platformColor
        for (p in platforms) {
            canvas.drawRect(p.rect, paint)
        }

        // Draw Doors
        for (d in doors) {
            paint.color = d.color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            canvas.drawRect(d.rect, paint)
            paint.style = Paint.Style.FILL
        }

        // Draw Diamonds
        for (d in diamonds) {
            if (!d.collected) {
                paint.color = d.color
                canvas.drawCircle(d.x, d.y, 20f, paint)
            }
        }

        // Draw Players
        drawPlayer(canvas, fireboy)
        drawPlayer(canvas, watergirl)

        // Draw Controls
        paint.color = Color.DKGRAY
        paint.alpha = 150
        for (btn in buttons) {
            canvas.drawRoundRect(btn.rect, 20f, 20f, paint)
            canvas.drawText(btn.label, btn.rect.centerX(), btn.rect.centerY() + 20f, textPaint)
        }

        invalidate()
    }

    private fun drawPlayer(canvas: Canvas, p: Player) {
        paint.color = p.color
        paint.alpha = 255
        canvas.drawRect(p.x, p.y, p.x + playerSize, p.y + playerSize, paint)
    }

    private fun updatePhysics() {
        if (levelComplete) return
        updatePlayer(fireboy, p1Left, p1Right)
        updatePlayer(watergirl, p2Left, p2Right)
        checkWin()
    }

    private fun checkWin() {
        val fireRect = RectF(fireboy.x, fireboy.y, fireboy.x + playerSize, fireboy.y + playerSize)
        val waterRect = RectF(watergirl.x, watergirl.y, watergirl.x + playerSize, watergirl.y + playerSize)
        
        var fireHome = false
        var waterHome = false

        for (d in doors) {
            if (d.color == fireColor && RectF.intersects(fireRect, d.rect)) fireHome = true
            if (d.color == waterColor && RectF.intersects(waterRect, d.rect)) waterHome = true
        }

        if (fireHome && waterHome) {
            levelComplete = true
            if (!adShown) {
                adShown = true
                AdPopupManager.onGameEnd(context) {
                    resetLevel()
                }
            }
        }
    }

    private fun resetLevel() {
        levelComplete = false
        adShown = false
        val h = height.toFloat()
        fireboy.x = 100f; fireboy.y = h - 300f; fireboy.vx = 0f; fireboy.vy = 0f
        watergirl.x = 200f; watergirl.y = h - 300f; watergirl.vx = 0f; watergirl.vy = 0f
        setupLevel()
        invalidate()
    }

    private fun updatePlayer(p: Player, left: Boolean, right: Boolean) {
        // Horizontal Movement
        if (left) p.vx = -moveSpeed
        else if (right) p.vx = moveSpeed
        else p.vx = 0f

        p.x += p.vx

        // Gravity
        p.vy += gravity
        p.y += p.vy

        // Collision Detection
        p.isGrounded = false
        val playerRect = RectF(p.x, p.y, p.x + playerSize, p.y + playerSize)

        for (plat in platforms) {
            if (RectF.intersects(playerRect, plat.rect)) {
                // Simple collision resolution
                // Check if landing on top
                if (p.vy > 0 && p.y + playerSize - p.vy <= plat.rect.top) {
                    p.y = plat.rect.top - playerSize
                    p.vy = 0f
                    p.isGrounded = true
                }
                // Hitting head
                else if (p.vy < 0 && p.y - p.vy >= plat.rect.bottom) {
                    p.y = plat.rect.bottom
                    p.vy = 0f
                }
                // Side collision
                else if (p.vx > 0 && p.x + playerSize - p.vx <= plat.rect.left) {
                    p.x = plat.rect.left - playerSize
                }
                else if (p.vx < 0 && p.x - p.vx >= plat.rect.right) {
                    p.x = plat.rect.right
                }
            }
        }

        // Screen bounds
        if (p.x < 0) p.x = 0f
        if (p.x > width - playerSize) p.x = width - playerSize.toFloat()
        if (p.y > height) { // Fell off
            p.x = 100f
            p.y = 100f
            p.vy = 0f
        }

        // Collect Diamonds
        for (d in diamonds) {
            if (!d.collected && d.color == p.color) {
                val dRect = RectF(d.x - 20, d.y - 20, d.x + 20, d.y + 20)
                if (RectF.intersects(playerRect, dRect)) {
                    d.collected = true
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val ptrIndex = event.actionIndex
        
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.getX(ptrIndex)
                val y = event.getY(ptrIndex)
                checkButtons(x, y, true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val x = event.getX(ptrIndex)
                val y = event.getY(ptrIndex)
                checkButtons(x, y, false)
            }
            MotionEvent.ACTION_MOVE -> {
                // For simplicity in multi-touch, just re-check all pointers? 
                // Or just handle down/up for buttons. 
                // Better to track pointer IDs for buttons but for this simple version:
                // We will just rely on Down/Up for toggling state, 
                // but Move might slide off button. 
                // Let's keep it simple: Tap to activate, Release to deactivate.
            }
        }
        return true
    }

    private fun checkButtons(x: Float, y: Float, pressed: Boolean) {
        for (btn in buttons) {
            if (btn.rect.contains(x, y)) {
                btn.action(pressed)
            }
        }
    }
}
