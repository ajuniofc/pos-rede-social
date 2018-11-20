package pos.kotlin.com.br.onboarding.onboard.listeners

import android.animation.Animator

abstract class AnimatorEndListener: Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator) {
        //do nothing
    }

    override fun onAnimationCancel(animation: Animator) {
        //do nothing
    }

    override fun onAnimationRepeat(animation: Animator) {
        //do nothing
    }
}