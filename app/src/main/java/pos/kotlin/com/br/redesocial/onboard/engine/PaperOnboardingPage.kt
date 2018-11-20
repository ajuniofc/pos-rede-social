package pos.kotlin.com.br.onboarding.onboard.engine

import java.io.Serializable

class PaperOnboardingPage(public val titleText: String,
                          val descriptionText: String,
                          val bgColor: Int,
                          val contentIconRes: Int,
                          val bottomBarIconRes: Int): Serializable {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as PaperOnboardingPage?

        if (bgColor != that!!.bgColor) return false
        if (contentIconRes != that.contentIconRes) return false
        if (bottomBarIconRes != that.bottomBarIconRes) return false
        if (if (titleText != null) titleText != that.titleText else that.titleText != null)
            return false
        return if (descriptionText != null) descriptionText == that.descriptionText else that.descriptionText == null

    }

    override fun hashCode(): Int {
        var result = titleText?.hashCode() ?: 0
        result = 31 * result + (descriptionText?.hashCode() ?: 0)
        result = 31 * result + bgColor
        result = 31 * result + contentIconRes
        result = 31 * result + bottomBarIconRes
        return result
    }

    override fun toString(): String {
        return "PaperOnboardingPage{" +
                "titleText='" + titleText + '\''.toString() +
                ", descriptionText='" + descriptionText + '\''.toString() +
                ", bgColor=" + bgColor +
                ", contentIconRes=" + contentIconRes +
                ", bottomBarIconRes=" + bottomBarIconRes +
                '}'.toString()
    }
}