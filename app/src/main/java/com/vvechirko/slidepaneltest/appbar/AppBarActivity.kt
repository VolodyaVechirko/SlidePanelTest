package com.vvechirko.slidepaneltest.appbar

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vvechirko.slidepaneltest.R

class AppBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_bar)

        val offsetChangedListener = object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                Log.d("AppBarActivity", "verticalOffset $verticalOffset")
            }
        }

        findViewById<AppBarLayout>(R.id.appBarLayout).addOnOffsetChangedListener(offsetChangedListener)
    }
}