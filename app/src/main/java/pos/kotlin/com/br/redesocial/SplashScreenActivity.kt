package pos.kotlin.com.br.redesocial

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {
    private val seconds: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            startActivity(Intent(this, SingUPActivity::class.java))
        }, seconds)
    }
}
