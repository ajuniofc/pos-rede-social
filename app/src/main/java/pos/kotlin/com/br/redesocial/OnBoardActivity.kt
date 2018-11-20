package pos.kotlin.com.br.redesocial

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import pos.kotlin.com.br.onboarding.onboard.engine.OnBoardingButton
import pos.kotlin.com.br.onboarding.onboard.engine.PaperOnboardingFragment
import pos.kotlin.com.br.onboarding.onboard.engine.PaperOnboardingPage
import java.util.ArrayList

class OnBoardActivity : AppCompatActivity() {

    private lateinit var onboardingFragment: PaperOnboardingFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board)

        onboardingFragment = PaperOnboardingFragment.newInstance(listOfSreens(), OnBoardingButton(getString(R.string.entrar), object : View.OnClickListener {
            override fun onClick(view: View) {
                nextView()
            }
        }))
    }

    override fun onResume() {
        super.onResume()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, onboardingFragment)
        fragmentTransaction.commit()
    }

    private fun listOfSreens(): ArrayList<PaperOnboardingPage> {
        val scr1 = PaperOnboardingPage(getString(R.string.aprender), getString(R.string.aprender_descricao),
                ContextCompat.getColor(this, R.color.colorPrimary), R.drawable.ic_aprender, R.drawable.ic_aprender_mini)
        val scr2 = PaperOnboardingPage(getString(R.string.produzir), getString(R.string.produzir_descricao),
                ContextCompat.getColor(this, R.color.colorPrimary), R.drawable.ic_produzir, R.drawable.ic_produzir_mini)
        val scr3 = PaperOnboardingPage(getString(R.string.teoria_pratica), getString(R.string.teoria_pratica_descricao),
                ContextCompat.getColor(this, R.color.colorPrimary), R.drawable.ic_star, R.drawable.ic_star_mini)

        val elements = ArrayList<PaperOnboardingPage>()
        elements.add(scr1)
        elements.add(scr2)
        elements.add(scr3)
        return elements
    }

    private fun nextView() {
        startActivity(Intent(this, SingUPActivity::class.java))
        finish()
    }
}
