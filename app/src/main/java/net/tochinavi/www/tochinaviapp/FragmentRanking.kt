package net.tochinavi.www.tochinaviapp


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A simple [Fragment] subclass.
 */
class FragmentRanking : Fragment() {

    companion object {
        val TAG = "Ranking"

        fun newInstance(): FragmentRanking {
            //var playListFragment = FragmentRanking()
            return FragmentRanking()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onResume() {
        super.onResume()
        Log.i(">> $TAG", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(">> $TAG", "onPause")
    }




}
