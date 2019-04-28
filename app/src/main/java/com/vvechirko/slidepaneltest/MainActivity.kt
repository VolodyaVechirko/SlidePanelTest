package com.vvechirko.slidepaneltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.vvechirko.slidepaneltest.appbar.AppBarActivity
import com.vvechirko.slidepaneltest.graph.GraphActivity
import com.vvechirko.slidepaneltest.sliding.SlidingActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn1).setOnClickListener {
            startActivity(Intent(this, SlidingActivity::class.java))
        }

        findViewById<View>(R.id.btn2).setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
        }

        findViewById<View>(R.id.btn3).setOnClickListener {
            startActivity(Intent(this, FitWindowTestActivity::class.java))
        }

        findViewById<View>(R.id.btn4).setOnClickListener {
            startActivity(Intent(this, SwipeLayoutActivity::class.java))
        }

        findViewById<View>(R.id.btn5).setOnClickListener {
            startActivity(Intent(this, AppBarActivity::class.java))
        }
    }
}
