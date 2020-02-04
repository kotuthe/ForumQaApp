package net.tochinavi.www.tochinaviapp


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import coil.api.load
import coil.decode.DataSource
import coil.request.Request
import kotlinx.android.synthetic.main.fragment_top.*
import net.tochinavi.www.tochinaviapp.storage.*
import net.tochinavi.www.tochinaviapp.value.MySharedPreferences
import net.tochinavi.www.tochinaviapp.value.convertString

/**
 * A simple [Fragment] subclass.
 */
class FragmentTop : Fragment() {

    companion object {
        val TAG = "FragmentTop"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_top,container,false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DBHelper(context!!)
        try {
            Log.i(">> ", "app_data-----------")
            val app_data = DBTableAppData(context!!).getAll(db)
            for (i in 0..app_data.size - 1) {
                app_data[i]._debug_log()
            }

            Log.i(">> ", "category1_data-----------")
            val category1_data = DBTableCategory1(context!!).getAll(db)
            for (i in 0..category1_data.size - 1) {
                category1_data[i]._debug_log()
            }

            Log.i(">> ", "category2_data-----------")
            val category2_data = DBTableCategory2(context!!).getAll(db)
            for (i in 0..category2_data.size - 1) {
                category2_data[i]._debug_log()
            }

            Log.i(">> ", "category3_data-----------")
            val category3_data = DBTableCategory3(context!!).getAll(db)
            for (i in 0..category3_data.size - 1) {
                category3_data[i]._debug_log()
            }

            Log.i(">> ", "area1_data-----------")
            val area1_data = DBTableArea1(context!!).getAll(db)
            for (i in 0..area1_data.size - 1) {
                area1_data[i]._debug_log()
            }

            Log.i(">> ", "area2_data-----------")
            val area2_data = DBTableArea2(context!!).getAll(db)
            for (i in 0..area2_data.size - 1) {
                area2_data[i]._debug_log()
            }

        } catch (e: Exception) {
            Log.e(TAG, "" + e.message)
        } finally {
            db.cleanup()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*
        imageViewTest.load("https://www.olympus-imaging.jp/product/dslr/e30/sample/images/index_image_07_l.jpg") {
            placeholder(R.drawable.ic_image_placeholder)

            listener(object : Request.Listener {
                override fun onError(data: Any, throwable: Throwable) {
                    super.onError(data, throwable)
                    throwable.printStackTrace()
                }
                override fun onCancel(data: Any) {
                    super.onCancel(data)
                    Log.i(">>", "image onCancel")
                }
                override fun onStart(data: Any) {
                    super.onStart(data)
                    Log.i(">>", "image onStart")
                }
                override fun onSuccess(data: Any, source: DataSource) {
                    super.onSuccess(data, source)
                    Log.i(">>", "image onSuccess")
                }
            })

        }
        */


    }

}
