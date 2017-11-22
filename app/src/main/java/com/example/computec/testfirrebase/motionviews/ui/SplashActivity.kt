package com.example.computec.testfirrebase.motionviews.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import com.example.computec.testfirrebase.R
import com.example.computec.testfirrebase.motionviews.ui.snap.SnapsActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            startActivity(SnapsActivity.startSnapActivty(this))
            finish()
        }, 1000)
    }
}
