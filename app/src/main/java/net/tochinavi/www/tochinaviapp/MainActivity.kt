package net.tochinavi.www.tochinaviapp


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tab_item_main.view.*
import net.tochinavi.www.tochinaviapp.value.Constants
import net.tochinavi.www.tochinaviapp.view.TabMainAdapter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initLayout()
    }

    private fun initLayout() {
        // ヘッダー
        setActionBar(0)
        setStatusBar(0)

        // フッタータブ
        val adapter = TabMainAdapter(supportFragmentManager,this)
        viewPager.adapter = adapter
        viewPager.setPagingEnabled(false)
        viewPager.currentItem = Constants.TAB_ITEM.TOP.ordinal
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                setActionBar(position)
                setStatusBar(position)
            }
        })
        setTabLayout(viewPager)
    }

    /**
     * ActionBarのレイアウト
     */
    @SuppressLint("RestrictedApi")
    private fun setActionBar(position: Int) {
        if (supportActionBar == null) { return }
        supportActionBar!!.setShowHideAnimationEnabled(false)
        if (position == 0) {
            supportActionBar!!.hide()
        } else {
            supportActionBar!!.show()
        }
    }

    /** ステータスバーの色変更  */
    private fun setStatusBar(position: Int) {
        if (position == 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(
                applicationContext,
                R.color.colorTopStatus
            )
        } else {
            window.statusBarColor = ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimaryDark
            )
        }
    }

    /**
     * タブのレイアウト
     */
    private fun setTabLayout(viewPager: ViewPager) {
        tabLayout.setupWithViewPager(viewPager)

        // Top
        val itemTop: View = createTabItem(R.drawable.ic_tab_top, "写真")
        tabLayout.getTabAt(Constants.TAB_ITEM.TOP.ordinal)!!.customView = itemTop

        // 周辺
        val itemNeighbor: View = createTabItem(R.drawable.ic_tab_top, "周辺")
        tabLayout.getTabAt(Constants.TAB_ITEM.NEIGHBOR.ordinal)!!.customView = itemNeighbor

        // カテゴリー
        val itemSearch: View = createTabItem(R.drawable.ic_tab_top, "カテゴリー", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.SEARCH.ordinal)!!.customView = itemSearch

        // ランキング
        val itemRanking: View = createTabItem(R.drawable.ic_tab_top, "ランキング", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.RANKING.ordinal)!!.customView = itemRanking

        // マイページ
        val itemMyPage: View = createTabItem(R.drawable.ic_tab_top, "Myページ", 9f)
        tabLayout.getTabAt(Constants.TAB_ITEM.MY_PAGE.ordinal)!!.customView = itemMyPage

    }

    /**
     * タブのアイテムの作成
     */
    private fun createTabItem(icon: Int, title: String, titleSize: Float = 10f): View {
        val inflater = LayoutInflater.from(this)
        val itemView: View = inflater.inflate(R.layout.tab_item_main, null)
        itemView.tabIcon.setImageDrawable(ContextCompat.getDrawable(this, icon))
        itemView.tabText.setText(title)
        itemView.tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
        return itemView
    }
}
