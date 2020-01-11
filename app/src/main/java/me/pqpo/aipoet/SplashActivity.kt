package me.pqpo.aipoet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val sharedView = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this@SplashActivity, icon, "icon_yz")
            ActivityCompat.startActivity(this@SplashActivity,
                Intent(this@SplashActivity, MainActivity::class.java), sharedView.toBundle())
            finish()
        }, 2000)
    }
}
