package pos.kotlin.com.br.onboarding.onboard.engine

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import pos.kotlin.com.br.onboarding.onboard.listeners.*
import pos.kotlin.com.br.redesocial.R
import java.util.ArrayList

class PaperOnboardingEngine(var mOnBoardingButton: OnBoardingButton, val mAppContext: Context) {
    val TAG = "POB"
    // animation and view settings
    val ANIM_PAGER_BAR_MOVE_TIME: Long = 700
    val ANIM_PAGER_ICON_TIME: Long = 350
    val ANIM_BACKGROUND_TIME: Long = 450
    val CONTENT_TEXT_POS_DELTA_Y_DP: Int = 50
    val ANIM_CONTENT_TEXT_SHOW_TIME: Long = 800
    val ANIM_CONTENT_TEXT_HIDE_TIME: Long = 200
    val CONTENT_ICON_POS_DELTA_Y_DP: Int = 50
    val ANIM_CONTENT_ICON_SHOW_TIME: Long = 800
    val ANIM_CONTENT_ICON_HIDE_TIME: Long = 200

    val PAGER_ICON_SHAPE_ALPHA = 0.5f

    val ANIM_CONTENT_CENTERING_TIME: Long = 800

    var mElements: ArrayList<PaperOnboardingPage>? = null

    private lateinit var mRootLayout: RelativeLayout

    private lateinit var mContentTextContainer: FrameLayout

    private lateinit var mContentIconContainer: FrameLayout

    private lateinit var mBackgroundContainer: FrameLayout

    private lateinit var mPagerIconsContainer: LinearLayout

    private lateinit var mButton: Button

    private lateinit var mContentRootLayout: RelativeLayout

    private lateinit var mContentCenteredContainer: LinearLayout

    private var dpToPixelsScaleFactor: Float = 0.0f

    private var mActiveElementIndex = 0

    // params for Pager position calculations, virtually final, but initializes in onGlobalLayoutListener
    private var mPagerElementActiveSize: Int = 0
    private var mPagerElementNormalSize: Int = 0
    private var mPagerElementLeftMargin: Int = 0
    private var mPagerElementRightMargin: Int = 0

    // Listeners
    private var mOnLeftOutListener: PaperOnboardingOnLeftOutListener? = null
    private var mOnChangeListener: PaperOnboardingOnChangeListener? = null
    private var mOnRightOutListener: PaperOnboardingOnRightOutListener? = null

    constructor(rootLayout: View, contentElements: ArrayList<PaperOnboardingPage>?, onBoardButton: OnBoardingButton, context: Context): this(onBoardButton, context) {
        if (contentElements == null || contentElements.isEmpty())
            throw IllegalArgumentException("No content elements provided")

        this.mElements = contentElements

        this.mRootLayout = rootLayout as RelativeLayout
        this.mContentTextContainer = rootLayout.findViewById(R.id.onboardingContentTextContainer) as FrameLayout
        this.mContentIconContainer = rootLayout.findViewById(R.id.onboardingContentIconContainer) as FrameLayout
        this.mBackgroundContainer = rootLayout.findViewById(R.id.onboardingBackgroundContainer) as FrameLayout
        this.mPagerIconsContainer = rootLayout.findViewById(R.id.onboardingPagerIconsContainer) as LinearLayout
        this.mButton = rootLayout.findViewById(R.id.onboardingButton) as Button


        this.mContentRootLayout = mRootLayout.getChildAt(1) as RelativeLayout
        this.mContentCenteredContainer = mContentRootLayout.getChildAt(0) as LinearLayout

        this.dpToPixelsScaleFactor = this.mAppContext.getResources().getDisplayMetrics().density

        initializeStartingState()

        mRootLayout.setOnTouchListener(object : OnSwipeListener(mAppContext) {
            override fun onSwipeLeft() {
                toggleContent(false)
            }

            override fun onSwipeRight() {
                toggleContent(true)
            }

        })

        mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                } else {
                    mRootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this)
                }

                mPagerElementActiveSize = mPagerIconsContainer.getHeight()
                mPagerElementNormalSize = Math.min(mPagerIconsContainer.getChildAt(0).getHeight(),
                        mPagerIconsContainer.getChildAt(mPagerIconsContainer.getChildCount() - 1).getHeight())

                val layoutParams = mPagerIconsContainer.getChildAt(0).getLayoutParams() as ViewGroup.MarginLayoutParams
                mPagerElementLeftMargin = layoutParams.leftMargin
                mPagerElementRightMargin = layoutParams.rightMargin

                mPagerIconsContainer.setX(calculateNewPagerPosition(0).toFloat())
                mContentCenteredContainer.setY(((mContentRootLayout.getHeight() - mContentCenteredContainer.getHeight()) / 2).toFloat())

            }
        })
    }

    private fun toggleContent(prev: Boolean) {
        val oldElementIndex = mActiveElementIndex
        val newElement = if (prev) toggleToPreviousElement() else toggleToNextElement()
        if (newElement == null) {
            if (prev && this.mOnLeftOutListener != null)
                mOnLeftOutListener?.onLeftOut()
            if (!prev && mOnRightOutListener != null)
                mOnRightOutListener?.onRightOut()
            return
        }

        val newPagerPosX = calculateNewPagerPosition(mActiveElementIndex)

        // 1 - animate BG
        val bgAnimation = createBGAnimatorSet(newElement.bgColor)

        // 2 - animate pager position
        val pagerMoveAnimation = ObjectAnimator.ofFloat(mPagerIconsContainer, "x", mPagerIconsContainer.x, newPagerPosX.toFloat())
        pagerMoveAnimation.setDuration(ANIM_PAGER_BAR_MOVE_TIME)

        // 3 - animate pager icons
        val pagerIconAnimation: AnimatorSet = createPagerIconAnimation(oldElementIndex, mActiveElementIndex)

        // 4 animate content text
        val newContentText = createContentTextView(newElement)
        mContentTextContainer.addView(newContentText)
        val contentTextShowAnimation: AnimatorSet = createContentTextShowAnimation(
                mContentTextContainer.getChildAt(mContentTextContainer.childCount - 2), newContentText)

        // 5 animate content icon
        val newContentIcon = createContentIconView(newElement)
        mContentIconContainer.addView(newContentIcon)
        val contentIconShowAnimation: AnimatorSet = createContentIconShowAnimation(
                mContentIconContainer.getChildAt(mContentIconContainer.childCount - 2), newContentIcon)

        // 6 animate centering of all content
        val centerContentAnimation: Animator = createContentCenteringVerticalAnimation(newContentText, newContentIcon)

        centerContentAnimation.start()
        bgAnimation.start()
        pagerMoveAnimation.start()
        pagerIconAnimation.start()
        contentIconShowAnimation.start()
        contentTextShowAnimation.start()

        if (mOnChangeListener != null)
            mOnChangeListener!!.onPageChanged(oldElementIndex, mActiveElementIndex)

        if (prev == false && oldElementIndex >= mElements!!.size - 2) {
            mButton.visibility = View.VISIBLE
        } else {
            mButton.visibility = View.INVISIBLE
        }
    }

    private fun createContentCenteringVerticalAnimation(newContentText: View?, newContentIcon: View?): Animator {
        newContentText?.measure(View.MeasureSpec.makeMeasureSpec(mContentCenteredContainer.width, View.MeasureSpec.AT_MOST), -2)
        val measuredContentTextHeight = newContentText?.getMeasuredHeight()
        newContentIcon!!.measure(-2, -2)
        val measuredContentIconHeight = newContentIcon?.getMeasuredHeight()

        val newHeightOfContent = measuredContentIconHeight!! + measuredContentTextHeight!! + (mContentTextContainer.layoutParams as ViewGroup.MarginLayoutParams).topMargin
        val centerContentAnimation = ObjectAnimator.ofFloat(mContentCenteredContainer, "y", mContentCenteredContainer.y.toFloat(),
                (mContentRootLayout.height - newHeightOfContent) / 2f)
        centerContentAnimation.setDuration(ANIM_CONTENT_CENTERING_TIME)
        centerContentAnimation.setInterpolator(DecelerateInterpolator())
        return centerContentAnimation
    }

    private fun createContentIconShowAnimation(currentContentIcon: View?, newContentIcon: View?): AnimatorSet {
        val positionDeltaPx = dpToPixels(CONTENT_ICON_POS_DELTA_Y_DP)
        val animations = AnimatorSet()
        val currentContentMoveUp = ObjectAnimator.ofFloat(currentContentIcon, "y", 0f, -positionDeltaPx.toFloat())
        currentContentMoveUp.setDuration(ANIM_CONTENT_ICON_HIDE_TIME)

        currentContentMoveUp.addListener(object : AnimatorEndListener() {
            override fun onAnimationEnd(animation: Animator) {
                mContentIconContainer.removeView(currentContentIcon)
            }
        })
        val currentContentFadeOut = ObjectAnimator.ofFloat(currentContentIcon, "alpha", 1f, 0f)
        currentContentFadeOut.setDuration(ANIM_CONTENT_ICON_HIDE_TIME)

        animations.playTogether(currentContentMoveUp, currentContentFadeOut)

        val newContentMoveUp = ObjectAnimator.ofFloat(newContentIcon, "y", positionDeltaPx.toFloat(), 0f)
        newContentMoveUp.setDuration(ANIM_CONTENT_ICON_SHOW_TIME)

        val newContentFadeIn = ObjectAnimator.ofFloat(newContentIcon, "alpha", 0f, 1f)
        newContentFadeIn.setDuration(ANIM_CONTENT_ICON_SHOW_TIME)

        animations.playTogether(newContentMoveUp, newContentFadeIn)

        animations.interpolator = DecelerateInterpolator()

        return animations
    }

    private fun createContentTextShowAnimation(currentContentText: View?, newContentText: View?): AnimatorSet {
        val positionDeltaPx = dpToPixels(CONTENT_TEXT_POS_DELTA_Y_DP)
        val animations = AnimatorSet()
        val currentContentMoveUp = ObjectAnimator.ofFloat(currentContentText, "y", 0f, -positionDeltaPx.toFloat())
        currentContentMoveUp.setDuration(ANIM_CONTENT_TEXT_HIDE_TIME)
        currentContentMoveUp.addListener(object : AnimatorEndListener() {
            override fun onAnimationEnd(animation: Animator) {
                mContentTextContainer.removeView(currentContentText)
            }
        })
        val currentContentFadeOut = ObjectAnimator.ofFloat(currentContentText, "alpha", 1f, 0f)
        currentContentFadeOut.setDuration(ANIM_CONTENT_TEXT_HIDE_TIME)

        animations.playTogether(currentContentMoveUp, currentContentFadeOut)

        val newContentMoveUp = ObjectAnimator.ofFloat(newContentText, "y", positionDeltaPx.toFloat(), 0f)
        newContentMoveUp.setDuration(ANIM_CONTENT_TEXT_SHOW_TIME)

        val newContentFadeIn = ObjectAnimator.ofFloat(newContentText, "alpha", 0f, 1f)
        newContentFadeIn.setDuration(ANIM_CONTENT_TEXT_SHOW_TIME)

        animations.playTogether(newContentMoveUp, newContentFadeIn)

        animations.interpolator = DecelerateInterpolator()

        return animations
    }

    private fun createPagerIconAnimation(oldIndex: Int, newIndex: Int): AnimatorSet {
        val animations = AnimatorSet()
        animations.setDuration(ANIM_PAGER_ICON_TIME)

        // scale down whole old element
        val oldActiveItem = mPagerIconsContainer.getChildAt(oldIndex) as ViewGroup
        val oldActiveItemParams = oldActiveItem.layoutParams as LinearLayout.LayoutParams
        val oldItemScaleDown = ValueAnimator.ofInt(mPagerElementActiveSize, mPagerElementNormalSize)
        oldItemScaleDown.addUpdateListener { valueAnimator ->
            oldActiveItemParams.height = valueAnimator.animatedValue as Int
            oldActiveItemParams.width = valueAnimator.animatedValue as Int
            oldActiveItem.requestLayout()
        }

        // fade out old new element icon
        val oldActiveIcon = oldActiveItem.getChildAt(1)
        val oldActiveIconFadeOut = ObjectAnimator.ofFloat(oldActiveIcon, "alpha", 1f, 0f)

        // fade in old element shape
        val oldActiveShape = oldActiveItem.getChildAt(0) as ImageView
        oldActiveShape.setImageResource(if (oldIndex - newIndex > 0) R.drawable.onboarding_pager_circle_icon else R.drawable.onboarding_pager_round_icon)
        val oldActiveShapeFadeIn = ObjectAnimator.ofFloat(oldActiveShape, "alpha", 0f, PAGER_ICON_SHAPE_ALPHA)
        // add animations
        animations.playTogether(oldItemScaleDown, oldActiveIconFadeOut, oldActiveShapeFadeIn)

        // scale up whole new element
        val newActiveItem = mPagerIconsContainer.getChildAt(newIndex) as ViewGroup
        val newActiveItemParams = newActiveItem.layoutParams as LinearLayout.LayoutParams
        val newItemScaleUp = ValueAnimator.ofInt(mPagerElementNormalSize, mPagerElementActiveSize)
        newItemScaleUp.addUpdateListener { valueAnimator ->
            newActiveItemParams.height = valueAnimator.animatedValue as Int
            newActiveItemParams.width = valueAnimator.animatedValue as Int
            newActiveItem.requestLayout()
        }

        // fade in new element icon
        val newActiveIcon = newActiveItem.getChildAt(1)
        val newActiveIconFadeIn = ObjectAnimator.ofFloat(newActiveIcon, "alpha", 0f, 1f)

        // fade out new element shape
        val newActiveShape = newActiveItem.getChildAt(0) as ImageView
        val newActiveShapeFadeOut = ObjectAnimator.ofFloat(newActiveShape, "alpha", PAGER_ICON_SHAPE_ALPHA, 0f)

        // add animations
        animations.playTogether(newItemScaleUp, newActiveShapeFadeOut, newActiveIconFadeIn)

        animations.interpolator = DecelerateInterpolator()
        return animations
    }

    private fun createBGAnimatorSet(color: Int): AnimatorSet {
        val bgColorView = ImageView(mAppContext)
        bgColorView.layoutParams = RelativeLayout.LayoutParams(mRootLayout.width, mRootLayout.height)
        bgColorView.setBackgroundColor(color)
        mBackgroundContainer.addView(bgColorView)

        val pos = calculateCurrentCenterCoordinatesOfPagerElement(mActiveElementIndex)

        val finalRadius = (if (mRootLayout.width > mRootLayout.height) mRootLayout.width else mRootLayout.height).toFloat()

        val bgAnimSet = AnimatorSet()
        val fadeIn = ObjectAnimator.ofFloat(bgColorView, "alpha", 0f, 1f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val circularReveal = ViewAnimationUtils.createCircularReveal(bgColorView, pos[0], pos[1], 0f, finalRadius)
            circularReveal.interpolator = AccelerateInterpolator()
            bgAnimSet.playTogether(circularReveal, fadeIn)
        } else {
            bgAnimSet.playTogether(fadeIn)
        }

        bgAnimSet.setDuration(ANIM_BACKGROUND_TIME)
        bgAnimSet.addListener(object : AnimatorEndListener() {
            override fun onAnimationEnd(animation: Animator) {
                mRootLayout.setBackgroundColor(color)
                bgColorView.visibility = View.GONE
                mBackgroundContainer.removeView(bgColorView)
            }
        })
        return bgAnimSet
    }

    protected fun calculateCurrentCenterCoordinatesOfPagerElement(activeElementIndex: Int): IntArray {
        val y = (mPagerIconsContainer.y + mPagerIconsContainer.height / 2).toInt()

        if (activeElementIndex >= mPagerIconsContainer.childCount)
            return intArrayOf(mRootLayout.width / 2, y)

        val pagerElem = mPagerIconsContainer.getChildAt(activeElementIndex)
        val x = (mPagerIconsContainer.x + pagerElem.x + (pagerElem.width / 2).toFloat()).toInt()
        return intArrayOf(x, y)
    }

    private fun calculateNewPagerPosition(newActiveElement: Int): Int {
        var newElement = newActiveElement + 1
//        mActiveElementIndex = newElement
        if (newElement <= 0)
            newElement = 1
        val pagerActiveElemCenterPosX = (mPagerElementActiveSize / 2
                + newActiveElement * mPagerElementLeftMargin
                + (newActiveElement - 1) * (mPagerElementNormalSize + mPagerElementRightMargin))
        return mRootLayout.width / 2 - pagerActiveElemCenterPosX
    }

    protected fun toggleToNextElement(): PaperOnboardingPage? {
        if (mActiveElementIndex + 1 < mElements!!.size) {
            mActiveElementIndex++
            return if (mElements!!.size > mActiveElementIndex) mElements?.get(mActiveElementIndex) else null
        } else
            return null
    }

    protected fun toggleToPreviousElement(): PaperOnboardingPage? {
        if (mActiveElementIndex - 1 >= 0) {
            mActiveElementIndex--
            return if (mElements!!.size > mActiveElementIndex) mElements?.get(mActiveElementIndex) else null
        } else
            return null
    }

    private fun initializeStartingState() {
        // Create bottom bar icons for all elements with big first icon
        for (i in this.mElements!!.indices) {
            val paperOnboardingPage = mElements?.get(i)
            val bottomBarIconElement = createPagerIconElement(paperOnboardingPage?.bottomBarIconRes!!, i == 0)
            mPagerIconsContainer.addView(bottomBarIconElement)
        }
        // Initialize first element on screen
        val activeElement: PaperOnboardingPage? = getActiveElement()
        // initial content texts
        val initialContentText = createContentTextView(activeElement)
        mContentTextContainer.addView(initialContentText)
        // initial content icons
        val initContentIcon = createContentIconView(activeElement)
        mContentIconContainer.addView(initContentIcon)
        // initial bg color
        mRootLayout.setBackgroundColor(activeElement!!.bgColor)

        mButton.text = mOnBoardingButton.title
        mButton.setOnClickListener(mOnBoardingButton.listener)
    }

    private fun createContentIconView(paperOnboardingPage: PaperOnboardingPage?): View? {
        val contentIcon = ImageView(mAppContext)
        contentIcon.setImageResource(paperOnboardingPage!!.contentIconRes)
        val iconLP = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        iconLP.gravity = Gravity.CENTER
        contentIcon.layoutParams = iconLP
        return contentIcon
    }

    private fun createContentTextView(paperOnboardingPage: PaperOnboardingPage?): View? {
        val vi = LayoutInflater.from(mAppContext)
        val contentTextView = vi.inflate(R.layout.onboarding_text_content_layout, mContentTextContainer, false) as ViewGroup
        val contentTitle = contentTextView.getChildAt(0) as TextView
        contentTitle.setText(paperOnboardingPage?.titleText)
        val contentText = contentTextView.getChildAt(1) as TextView
        contentText.setText(paperOnboardingPage?.descriptionText)
        return contentTextView
    }

    private fun getActiveElement(): PaperOnboardingPage? {
        return if (mElements?.size!! > mActiveElementIndex)
            mElements?.get(mActiveElementIndex)
        else null
    }

    private fun createPagerIconElement(iconDrawableRes: Int, isActive: Boolean): View? {
        val vi = LayoutInflater.from(mAppContext)
        val bottomBarElement = vi.inflate(R.layout.onboarding_pager_layout, mPagerIconsContainer, false) as FrameLayout
        val elementShape = bottomBarElement.getChildAt(0) as ImageView
        val elementIcon = bottomBarElement.getChildAt(1) as ImageView
        elementIcon.setImageResource(iconDrawableRes)
        if (isActive) {
            val layoutParams = bottomBarElement.layoutParams as LinearLayout.LayoutParams
            layoutParams.width = mPagerIconsContainer.layoutParams.height
            layoutParams.height = mPagerIconsContainer.layoutParams.height
            elementShape.alpha = 0f
            elementIcon.alpha = 1f
        } else {
            elementShape.setAlpha(PAGER_ICON_SHAPE_ALPHA)
            elementIcon.alpha = 0f
        }
        return bottomBarElement
    }

    protected fun dpToPixels(dpValue: Int): Int {
        return (dpValue * dpToPixelsScaleFactor + 0.5f).toInt()
    }

    fun setOnChangeListener(onChangeListener: PaperOnboardingOnChangeListener?) {
        this.mOnChangeListener = onChangeListener
    }

    fun setOnRightOutListener(onRightOutListener: PaperOnboardingOnRightOutListener?) {
        this.mOnRightOutListener = onRightOutListener
    }

    fun setOnLeftOutListener(onLeftOutListener: PaperOnboardingOnLeftOutListener?) {
        this.mOnLeftOutListener = onLeftOutListener
    }
}