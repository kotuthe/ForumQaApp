package net.tochinavi.www.tochinaviapp.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// とりあえず使わない方向で
class TabInitDescriptionAdapter(fm: FragmentManager, objects: List<Int>): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val datas: List<Int> = objects

    override fun getItem(position: Int): Fragment {
        // return FragmentInitDescription.newInstance(position, datas.get(position));
        return Fragment()
    }



    override fun getCount(): Int {
        return datas.size
    }

    /*
    private List<Integer> datas = new ArrayList<Integer>();

    public AdapterPagerFragmentInitDescription(FragmentManager fm, List<Integer> objects) {
        super(fm);
        datas = objects;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentInitDescription.newInstance(position, datas.get(position));
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    /**
     * 画像が重いため解放時にFragmentも解除する
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        Fragment f = (Fragment) object;
        f = null;
    }
     */
}