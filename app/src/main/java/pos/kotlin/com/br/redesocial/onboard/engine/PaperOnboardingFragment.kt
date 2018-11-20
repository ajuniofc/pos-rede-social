package pos.kotlin.com.br.onboarding.onboard.engine

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pos.kotlin.com.br.onboarding.onboard.listeners.PaperOnboardingOnChangeListener
import pos.kotlin.com.br.onboarding.onboard.listeners.PaperOnboardingOnLeftOutListener
import pos.kotlin.com.br.onboarding.onboard.listeners.PaperOnboardingOnRightOutListener
import pos.kotlin.com.br.redesocial.R
import java.util.ArrayList

class PaperOnboardingFragment: Fragment() {


    private var mOnChangeListener: PaperOnboardingOnChangeListener? = null
    private var mOnRightOutListener: PaperOnboardingOnRightOutListener? = null
    private var mOnLeftOutListener: PaperOnboardingOnLeftOutListener? = null
    private lateinit var mElements: ArrayList<PaperOnboardingPage>

    companion object {
        private val ELEMENTS_PARAM = "elements"

        private lateinit var mOnBoardingButton: OnBoardingButton

        fun newInstance(elements: ArrayList<PaperOnboardingPage>, onBoardingButton: OnBoardingButton): PaperOnboardingFragment {
            mOnBoardingButton = onBoardingButton
            val fragment = PaperOnboardingFragment()
            val args = Bundle()
            args.putSerializable(ELEMENTS_PARAM, elements)
            fragment.arguments = args
            return fragment
        }
    }

    fun newInstance(elements: ArrayList<PaperOnboardingPage>, onBoardingButton: OnBoardingButton): PaperOnboardingFragment {
        mOnBoardingButton = onBoardingButton
        val fragment = PaperOnboardingFragment()
        val args = Bundle()
        args.putSerializable(ELEMENTS_PARAM, elements)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null){
            mElements = arguments!!.get(ELEMENTS_PARAM) as ArrayList<PaperOnboardingPage>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.onboarding_main_layout, container, false)

        // create engine for onboarding element
        val mPaperOnboardingEngine: PaperOnboardingEngine = PaperOnboardingEngine(view.findViewById(R.id.onboardingRootView),
                mElements, mOnBoardingButton, activity!!.applicationContext)
        // set listeners
        mPaperOnboardingEngine.setOnChangeListener(mOnChangeListener)
        mPaperOnboardingEngine.setOnLeftOutListener(mOnLeftOutListener)
        mPaperOnboardingEngine.setOnRightOutListener(mOnRightOutListener)

        return view
    }
}