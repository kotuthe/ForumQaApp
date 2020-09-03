package net.tochinavi.www.tochinaviapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import net.tochinavi.www.tochinaviapp.entities.DataSpotList
import net.tochinavi.www.tochinaviapp.network.FirebaseHelper
import net.tochinavi.www.tochinaviapp.value.MyImage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import kotlin.math.roundToInt


// 取得したデータのみを表示する

class ActivitySpotNeighborMap :
    AppCompatActivity(),
    OnMapReadyCallback {

    companion object {
        val TAG = "ActivitySpotNeighborMap"
        val TAG_SHORT = "SpotNeighborMap"
    }

    private lateinit var firebase: FirebaseHelper

    private lateinit var myPosition: LatLng
    private lateinit var spotArray: ArrayList<DataSpotList>
    private lateinit var mGoogleMap: GoogleMap

    private lateinit var mClusterManager: ClusterManager<MyItem>
    private var activeItem: MyItem? = null

    private var handlerMarkerAnime: Handler? = null
    private var runnableMarkerAnime: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_neighbor_map)

        firebase = FirebaseHelper(applicationContext)

        spotArray = intent.getSerializableExtra("spotArray") as ArrayList<DataSpotList>

        // 現在地をカメラの中心に。なければ宇都宮駅
        myPosition = LatLng(
            intent.getDoubleExtra("latitude", 36.559170),
            intent.getDoubleExtra("longitude", 139.898505))


        if (supportActionBar != null) {
            supportActionBar!!.title = "周辺地図"
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onPause() {
        super.onPause()

        // アニメーションリセット
        if (handlerMarkerAnime != null && runnableMarkerAnime != null) {
            handlerMarkerAnime!!.removeCallbacks(runnableMarkerAnime!!)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap.isMyLocationEnabled = true
        // 初期カメラ
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15f))

        // Cluster
        mClusterManager = ClusterManager(this, mGoogleMap)
        mGoogleMap.setOnCameraIdleListener(mClusterManager)
        mGoogleMap.setOnMarkerClickListener(mClusterManager)
        for (i in 0..spotArray.size - 1) {
            val offsetItem = MyItem(spotArray[i])
            mClusterManager.addItem(offsetItem)
        }

        // 描画
        mClusterManager.renderer = MyClusterRenderer(this, mGoogleMap, mClusterManager)

        // クラスタータッチ ( true: 非表示, false: 表示 )
        mClusterManager.setOnClusterClickListener{ item ->
            val items = item.items as ArrayList<MyItem>
            activeItem = null

            if (mGoogleMap.cameraPosition.zoom < mGoogleMap.maxZoomLevel - 1) {
                mGoogleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(items[0].position, mGoogleMap.cameraPosition.zoom + 2))
            }

            true
        }

        // アイテムタッチ
        mClusterManager.setOnClusterItemClickListener{ item ->
            activeItem = item
            false
        }

        mGoogleMap.setInfoWindowAdapter(MyInfoWindowAdapter(this))
        // infowindowをクリック
        mGoogleMap.setOnInfoWindowClickListener {
            if (it.tag != null) {
                val tag = it.tag as MyItem
                val item = tag.getData()
                firebase.sendSpotInfo(FirebaseHelper.screenName.Search_Near_List, item.type, item.id)
                if (item.type == 1) {
                    val intent = Intent(this, ActivitySpotInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, ActivityHospitalInfo::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("name", item.name)
                    startActivity(intent)
                }
            }
        }
    }

    // クラスター構造データ //
    class MyItem(private val data: DataSpotList) : ClusterItem {
        override fun getTitle(): String = data.name
        override fun getPosition(): LatLng = LatLng(data.latitude, data.longitude)
        override fun getSnippet(): String = data.address
        fun getData(): DataSpotList = data
    }

    // クラスター描画 //
    class MyClusterRenderer(context: Context, map: GoogleMap, clusterManager: ClusterManager<MyItem>?) :
        DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

        // 定数 //
        private val CLUSTER_SPOT: Int = 5

        // データ //
        private val mContext = context
        private val mClusterIconGenerator = IconGenerator(context)
        private val mGoogleMap = map

        override fun shouldRenderAsCluster(cluster: Cluster<MyItem>?): Boolean {
            // メインスレッドでないとcameraPositionを取得できないため
            if (Thread.currentThread() == mContext.mainLooper.thread) {
                if (mGoogleMap.cameraPosition.zoom >= mGoogleMap.maxZoomLevel - 2){
                    return false
                }
            }
            return cluster!!.size > CLUSTER_SPOT
        }

        override fun onBeforeClusterItemRendered(
            item: MyItem?,
            markerOptions: MarkerOptions
        ) {
            val icon = BitmapFactory.decodeResource(
                mContext.resources, MyImage().icon_category_pin(item!!.getData().parent_category_id))
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }
    }

    // InfoWindow //
    inner class MyInfoWindowAdapter(activity: Activity) : InfoWindowAdapter {
        private val view: View
        private val mContext = activity
        private var lastMarker: Marker? = null

        init {
            view = activity.layoutInflater.inflate(R.layout.cell_spot_neighbor_map, null)
        }

        override fun getInfoWindow(marker: Marker): View {
            if (lastMarker == null || !lastMarker!!.equals(marker)) {
                lastMarker = marker

                if (activeItem != null) {
                    val icon = BitmapFactory.decodeResource(mContext.resources, MyImage().icon_category_pin(activeItem!!.getData().parent_category_id))
                    animeMarker(icon, marker, 1000)
                    marker.tag = activeItem

                    val name = view.findViewById<TextView>(R.id.textViewName)
                    name.text = activeItem!!.title

                    val info = view.findViewById<TextView>(R.id.textViewInfo)
                    val data = activeItem!!.getData()
                    info.text = "%s %s".format(data.category, data.distance)

                    val imageSpot = view.findViewById<ImageView>(R.id.imageViewSpot)
                    if (!activeItem!!.getData().image_url.isEmpty()) {
                        // coilがうまく動作しないため
                        TaskWebMarkerImage(imageSpot, lastMarker!!).execute(activeItem!!.getData().image_url)
                        /*
                        if (Build.VERSION.SDK_INT >= 28) {
                            TaskWebMarkerImage(imageSpot, lastMarker!!).execute(activeItem!!.getData().image_url)
                        } else {
                            imageSpot.load(activeItem!!.getData().image_url) {
                                placeholder(R.drawable.ic_image_placeholder)
                            }
                        }
                        */
                    }
                }
            }

            return view
        }

        override fun getInfoContents(p0: Marker?): View {
            TODO("Not yet implemented")
        }

    }

    /**
     * マーカーの画像をダウンロード
     */
    inner class TaskWebMarkerImage internal constructor(img: ImageView, marker: Marker) :
        AsyncTask<String, Void?, Bitmap?>() {

        var img: ImageView
        var marker: Marker

        init {
            this.img = img
            this.marker = marker
        }

        override fun doInBackground(vararg strings: String): Bitmap? {
            var bmp: Bitmap? = null
            try {
                val urlStr = strings[0]
                val url = URL(urlStr)
                val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                con.requestMethod = "GET"
                con.connect()
                when (con.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val `is`: InputStream = con.inputStream
                        bmp = BitmapFactory.decodeStream(`is`)
                        `is`.close()
                    }
                    else -> {
                    }
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: ProtocolException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bmp
        }

        override fun onPostExecute(bmp: Bitmap?) {
            img.setImageBitmap(bmp)
            marker.showInfoWindow()
        }
    }

    /**
     * マーカーアニメーション
     * @param markerIcon
     * @param marker
     * @param onePulseDuration
     */
    private fun animeMarker(icon: Bitmap, marker: Marker, onePulseDuration: Long) {

        // リセット
        if (handlerMarkerAnime != null && runnableMarkerAnime != null) {
            handlerMarkerAnime!!.removeCallbacks(runnableMarkerAnime!!)
        }
        handlerMarkerAnime = Handler()
        val startTime = System.currentTimeMillis()
        val interpolator: Interpolator = CycleInterpolator(1f)
        runnableMarkerAnime = object : Runnable {
            override fun run() {
                // マーカーが消えたときにリセット
                if (!marker.isInfoWindowShown) {
                    if (handlerMarkerAnime != null && runnableMarkerAnime != null) {
                        handlerMarkerAnime!!.removeCallbacks(runnableMarkerAnime!!)
                    }
                    return
                }
                val elapsed = System.currentTimeMillis() - startTime
                val t = interpolator.getInterpolation(elapsed.toFloat() / onePulseDuration)

                marker.setIcon(
                    BitmapDescriptorFactory.fromBitmap(scaleBitmap(icon,1f + 0.05f * t)))
                handlerMarkerAnime!!.postDelayed(this, 16)
            }
        }
        handlerMarkerAnime!!.post(runnableMarkerAnime!!)
    }

    fun scaleBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap? {
        val sizeX = (bitmap.width * scaleFactor).roundToInt()
        val sizeY = (bitmap.height * scaleFactor).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false)
    }

    /*
    # メモ
    # 下記でマーカーのtitleやpostionを取得できる
    val markerCollection: MarkerManager.Collection = mClusterManager.markerCollection
    val markers: Collection<Marker> = markerCollection.markers
    for ((index, m) in markers.withIndex()) {
        val id = m.id // m1, m2, m3
        val title = m.title
        Log.i("%s".format(id), "%s".format(title))
    }

    # infowindowの表示
    val markers = mClusterManager.markerCollection.markers as ArrayList<Marker>
    markers[0].showInfoWindow()
     */

}
