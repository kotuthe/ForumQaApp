package net.tttttt.www.forum_qa_app.view

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.tttttt.www.forum_qa_app.FragmentMyPage
import net.tttttt.www.forum_qa_app.*
import net.tttttt.www.forum_qa_app.value.Constants

/**
 * BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT：Fragmentがオンスクリーンになった時、onResumeを呼ぶ
 */
class TabMainAdapter(fm: FragmentManager, private val context: Context): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val TAB_NUM = 5

    override fun getItem(position: Int): Fragment {
        when (Constants.TAB_ITEM.values()[position]) {
            Constants.TAB_ITEM.TOP -> {
                return FragmentTop()
            }
            Constants.TAB_ITEM.NEIGHBOR -> {
                return FragmentSpotNeighborList()
            }
            Constants.TAB_ITEM.SEARCH -> {
                return FragmentSpotSearch()
            }
            Constants.TAB_ITEM.RANKING -> {
                return FragmentPostQa()
            }
            Constants.TAB_ITEM.MY_PAGE -> {
                return FragmentMyPage()
            }
            else -> {
                return Fragment()
            }
        }
    }


    override fun getCount(): Int {
        return TAB_NUM
    }
}