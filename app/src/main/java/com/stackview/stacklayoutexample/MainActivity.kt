package com.stackview.stacklayoutexample

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stackview.stacklayoutexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.stackView.setHeaderText("Stack View")
        val widthDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            86f,
            resources.displayMetrics
        ).toInt()
        val heightDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            48f,
            resources.displayMetrics
        ).toInt()

        val card1 = CardView(this).apply {
            layoutParams = ViewGroup.LayoutParams(widthDp, heightDp)
            setCardBackgroundColor(Color.RED)
            radius = 32f
        }

        val card2 = CardView(this).apply {
            layoutParams = ViewGroup.LayoutParams(widthDp, heightDp)
            setCardBackgroundColor(Color.BLUE)
            radius = 32f
        }

        val card3 = CardView(this).apply {
            layoutParams = ViewGroup.LayoutParams(widthDp, heightDp)
            setCardBackgroundColor(Color.GRAY)
            radius = 32f
        }

        binding.stackView.addStackChild(card1)
        binding.stackView.addStackChild(card2)
        binding.stackView.addStackChild(card3)
    }
}