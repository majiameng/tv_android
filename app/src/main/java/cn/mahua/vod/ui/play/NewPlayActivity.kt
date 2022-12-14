package cn.mahua.vod.ui.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.mahua.av.CheckVodTrySeeBean
import cn.mahua.av.play.AvVideoController
import cn.mahua.av.play.AvVideoController.RECEIVER_TYPE_REPLAY
import cn.mahua.av.play.AvVideoController.RECEIVER_TYPE_TIMER
import cn.mahua.vod.App
import cn.mahua.vod.R
import cn.mahua.vod.base.BaseActivity
import cn.mahua.vod.bean.*
import cn.mahua.vod.jiexi.BackListener
import cn.mahua.vod.jiexi.JieXiUtils2
import cn.mahua.vod.netservice.VodService
import cn.mahua.vod.ui.dlan.DlanListPop
import cn.mahua.vod.ui.login.LoginActivity
import cn.mahua.vod.ui.pay.PayActivity
import cn.mahua.vod.ui.widget.HitDialog
import cn.mahua.vod.utils.*
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.StringUtils
import com.dueeeke.videoplayer.listener.OnVideoViewStateChangeListener
import com.dueeeke.videoplayer.player.VideoView
import com.github.StormWyrm.wanandroid.base.exception.ResponseException
import com.github.StormWyrm.wanandroid.base.net.RequestManager
import com.github.StormWyrm.wanandroid.base.net.observer.BaseObserver
import com.github.StormWyrm.wanandroid.base.net.observer.PlayLoadingObserver
import com.liuwei.android.upnpcast.NLUpnpCastManager
import com.liuwei.android.upnpcast.device.CastDevice
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.activity_new_play.*
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class NewPlayActivity : BaseActivity(), OnSpeedItemClickListener {
    private val TAG = "NewPlayActivity"

    private lateinit var controller: AvVideoController
    private lateinit var mVodBean: VodBean
    private var isShowPlayProgress = false
    private var curPlayUrl: String = ""
    private var isAllowCastScreen: Boolean = false//???????????????????????????????????????????????????
    private var curParseIndex = 0//???????????????????????????????????????????????????????????????????????????????????? ?????????????????????
    private var curFailIndex = -1
    private var isPlay = false//??????????????????
    private var playSourceIndex = 0//???????????????
    private var urlIndex = 0//???????????????????????????
    private lateinit var playFormList: List<PlayFromBean>
    private lateinit var playFrom: PlayFromBean //???????????????????????????
    private var playList: List<UrlBean>? = null//??????????????????
    private var playScoreInfo: PlayScoreBean? = null
    private var isParseSuccess = false
    private var isSeekToHistory: Boolean = false
    private var curProgressHistory: Long = 0
    private var vodDuration: Long = 0
    private var videoNetProgress: Long = 0L

    private val onJiexiResultListener = object : BackListener {
        override fun onSuccess(url: String?, curParseIndex: Int) {
            println("===Jiexi onSuccess ?????????$curParseIndex url=${url}")
            println("---play----onSuccess" + url)
            if (isSuccess) {
                Log.d(TAG, "====ParseonFail  " + "\n url=" + url)
                return
            } else {
                Log.d(TAG, "====ParseonSuccess  " + "\n url=" + url)
            }
            curFailIndex = curParseIndex
            println("---play----onSuccess=false curFailIndex=" + curFailIndex + " ??????url" + url)
            if (url == null || url.isEmpty()) {
                println("===??????onSuccess")
                runOnUiThread {
                    chengeNextLine()
                }
                return
            }
            println("---play----isPlay=" + isPlay)
            url.let {
                if (!isPlay) {
                    Log.d(TAG, "====ParseonSuccess  play??????" + " url=" + it)
                    play(it)
                    curPlayUrl = it
                    isPlay = true
                }
            }


        }

        override fun onError() {
            controller.updateJiexiProgess("??????????????????,???????????????????????????????????????")
        }

        override fun onProgressUpdate(msg: String?) {
            controller.updateJiexiProgess(msg)
        }
    }
    private var videoDetailFragment: VideoDetailFragment? = null
    private var summaryFragment: SummaryFragment? = null
    private var playListFragment: PlayListFragment? = null
    private var isParsed: Boolean = false
    private var isLandscape = false//?????????????????????

    override fun getLayoutResID(): Int {
        return R.layout.activity_new_play
    }

    override fun initView() {
        super.initView()
        BarUtils.setStatusBarColor(this, ColorUtils.getColor(R.color.player_status_color))
        mVodBean = intent.getParcelableExtra(PlayActivity.KEY_VOD) as VodBean
        isShowPlayProgress = intent.getBooleanExtra(PlayActivity.KEY_SHOW_PROGRESS, false)
        controller = AvVideoController(videoView, this)

        videoView.setVideoController(controller)

        registerReceiver()


    }

    override fun initListener() {
        super.initListener()
        controller.setControllerClickListener {
            when (it.id) {
                R.id.tv_av_hd ->
                    chengeNextLine()
                R.id.iv_av_back, R.id.iv_av_back1, R.id.iv_av_back2 -> {
                    Log.i("bds", "back===========")
                    // finish();
                    App.curPlayScoreBean = null
                    playScoreInfo = null
                    savePlayRecord(true)
                    setResult(3)
                }

                R.id.iv_av_next ->
                    playNext()
                R.id.tv_av_speed ->
                    showSpeedListDialog(it.tag as Int)
                R.id.tv_av_selected ->
                    showPlayListDialog()
                R.id.tvPlaySource ->
                    showPlaySourceDialog()
                R.id.iv_av_miracast -> {

                    if (LoginUtils.checkVIP(this@NewPlayActivity, "??????????????????vip,???????????????")) {
                        showCastScreenDialog()
                    }
                }


                R.id.tvPayButton, R.id.tvEndPayButton -> {
                    payPlay()
                }
                R.id.tvUpdateButton, R.id.tvEndUpdateButton -> {
                    updateVip()
                }
                R.id.btn_pop_danmaku -> {
                    val s = it.tag as String
                    sendDanmu(s)
                }
            }
        }

        videoView.setOnVideoViewStateChangeListener(object : OnVideoViewStateChangeListener {
            override fun onPlayStateChanged(playState: Int) {
                if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
                    val percentage = getPercentage(curProgressHistory, vodDuration)
                    println("??????9???=" + controller.percentage + "  2:" + playScoreInfo?.curProgress + " 3=" + curProgressHistory + " 4=" + percentage)
                    if (percentage <= 0.01f || percentage >= 0.99f) {
                        println("??????5???==")
                        playNext()
                    } else {
                        println("??????1???==" + curProgressHistory)
                        controller.setReplayByCurProgress(true)
                    }
                } else if (playState == VideoView.STATE_PREPARED) {
                    isParseSuccess = true
                    if (isShowPlayProgress) {
                        Log.i("dsd", "iko===${App.curPlayScoreBean?.curProgress ?: 0}")
                        videoView.seekTo(playScoreInfo?.curProgress ?: 0)
                        println("??????3???==" + playScoreInfo?.curProgress)
                        //?????????????????????????????????????????????????????????????????????
//                        playScoreInfo?.let {
//                            LitePal.deleteAll(PlayScoreBean::class.java, "vodId = ?", "${it.vodId}")
//                            playScoreInfo = null
//                        }
                        isShowPlayProgress = false
                    } else {
                        if (isSeekToHistory) {
                            videoView.seekTo(curProgressHistory)
                            println("??????2???==" + curProgressHistory)
//                            isSeekToHistory = false
                        } else {
                            //??????30?????????
                            if (videoNetProgress == 0L) {
                                videoView.seekTo(30000)
                            } else {
                                videoView.seekTo(videoNetProgress)
                            }
                            println("??????4???== videoNetProgress=" + videoNetProgress)
                        }
                    }
                    vodDuration = controller.duration
                    println("??????12???==" + vodDuration)
                    when (SPUtils.getInstance().getInt(AvVideoController.KEY_SPEED_INDEX, 3)) {
                        0 -> {
                            videoView.setSpeed(2f)
                            controller.setSpeed("2.00")
                        }
                        1 -> {
                            videoView.setSpeed(1.5f)
                            controller.setSpeed("1.50")
                        }
                        2 -> {
                            videoView.setSpeed(1.25f)
                            controller.setSpeed("1.25")
                        }
                        3 -> {
                            videoView.setSpeed(1f)
                            controller.setSpeed("1.00")
                        }
                        4 -> {
                            videoView.setSpeed(0.75f)
                            controller.setSpeed("0.75")
                        }
                        5 -> {
                            videoView.setSpeed(0.5f)
                            controller.setSpeed("0.50")
                        }
                    }
                } else if (playState == VideoView.STATE_ERROR) {
                    LogUtils.d("=====?????? video OnError")
                    controller.setReplayByCurProgress(true)
                    isSeekToHistory = true
                    curParseIndex++
                    parseData()
                }
            }

            override fun onPlayerStateChanged(playerState: Int) {
                if (playerState == VideoView.PLAYER_NORMAL) {
                    isLandscape = false
                } else if (playerState == VideoView.PLAYER_FULL_SCREEN) {
                    isLandscape = true
                }
            }
        })
    }

    override fun initData() {
        super.initData()
        getVideoDetail()
    }

    override fun onStart() {
        super.onStart()
        videoView.resume()
        if (isParsed) {
            checkVodTrySee()
        }
    }

    override fun onResume() {
        super.onResume()
        checkVodTrySee()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()
        videoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        JieXiUtils2.INSTANCE.stopGet()
        controller.onDestroy()
        videoView.release()
        lbm?.unregisterReceiver(localReceiver)
        cancelTimer()
    }

    override fun onBackPressedSupport() {
        if (!videoView.onBackPressed()) {
            try {
                recordPlay()//????????????????????????
            } catch (e: Exception) {
            } finally {
                super.onBackPressedSupport()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            App.curPlayScoreBean = null
            playScoreInfo = null
            savePlayRecord(true)
            setResult(3)
            finish()
            false
        } else {
            super.onKeyDown(keyCode, event)
        }
    }


    fun showSummary() {
        if (summaryFragment == null) {
            summaryFragment = SummaryFragment.newInstance(mVodBean)
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, summaryFragment!!)
                    .commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction()
                    .show(summaryFragment!!)
                    .commitAllowingStateLoss()
        }
    }

    fun hideSummary() {
        supportFragmentManager.beginTransaction()
                .hide(summaryFragment!!)
                .commitAllowingStateLoss()
    }

    fun showVideoDetail() {
        if (videoDetailFragment == null) {
            videoDetailFragment = VideoDetailFragment.newInstance(mVodBean, urlIndex, playSourceIndex)
            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, videoDetailFragment!!)
                    .commitNowAllowingStateLoss()
        } else {
            videoDetailFragment?.changeCurIndex(urlIndex)
            supportFragmentManager.beginTransaction()
                    .show(videoDetailFragment!!)
                    .commitNowAllowingStateLoss()
        }
    }

    fun showPlayList() {
        if (playListFragment == null) {
            val spanCount = if (mVodBean.type_id == 3) {
                2
            } else {
                5
            }
            playListFragment = PlayListFragment.newInstance(spanCount).apply {
                if (playList != null) {
                    showPlayList(playList!!, urlIndex)
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.flContainer, playListFragment!!)
                    .commitAllowingStateLoss()
        } else {
            playListFragment?.run {
                if (playList != null) {
                    showPlayList(playList!!, urlIndex)
                }
            }
            supportFragmentManager.beginTransaction()
                    .show(playListFragment!!)
                    .commitAllowingStateLoss()
        }
    }

    fun hidePlayList() {
        supportFragmentManager.beginTransaction()
                .hide(playListFragment!!)
                .commitAllowingStateLoss()
    }

    fun showNewVideo(vodBean: VodBean) {
        savePlayRecord(false)
        curProgressHistory = 0
        videoNetProgress = 0
//        isSeekToHistory = false
        recordPlay()//??????????????????????????????
        App.curPlayScoreBean = null
        playScoreInfo = null
        mVodBean = vodBean
        supportFragmentManager.beginTransaction()
                .remove(videoDetailFragment!!)
                .commitNowAllowingStateLoss()
        videoDetailFragment = null

        if (summaryFragment != null) {
            supportFragmentManager.beginTransaction()
                    .remove(summaryFragment!!)
                    .commitNowAllowingStateLoss()
            summaryFragment = null

        }

        if (playListFragment != null) {
            supportFragmentManager.beginTransaction()
                    .remove(playListFragment!!)
                    .commitNowAllowingStateLoss()
            playListFragment = null
        }

        videoView.release()
        controller.setTitle(mVodBean.vodName)
        getVideoDetail()
    }

    fun changeSelection(position: Int) {
        urlIndex = position//??????????????????
        curProgressHistory = 0
        videoNetProgress = 0
        isSeekToHistory = false
        curFailIndex = -1
        this.curParseIndex = 0
        LogUtils.d("=====?????? changeSelection")
        parseData()
    }

    fun changeVideoUrlIndex(position: Int = -1) {
        videoDetailFragment?.changeCurIndex(urlIndex)
        this.curParseIndex = 0
        curFailIndex = -1
    }

    fun changePlaySource(playFromBean: PlayFromBean, playSourceIndex: Int) {
        this.playFrom = playFromBean
        this.playList = playFrom.urls
        this.playSourceIndex = playSourceIndex
        this.curParseIndex = 0
        curFailIndex = -1
        LogUtils.d("=====?????? changePlaySource")
        parseData()
        videoDetailFragment?.changePlaysource(playSourceIndex)
    }

    fun castScreen(device: CastDevice) {
        if (isParseSuccess && curPlayUrl.isNotEmpty()) {
            val vodUrl = if (curPlayUrl.startsWith("//")) {
                "https:$curPlayUrl"
            } else {
                curPlayUrl
            }
            NLUpnpCastManager.getInstance().connect(device);
            Intent(this, CastScreenActivity2::class.java).apply {
                putExtra("vod", mVodBean)
                putExtra("playSourceIndex", playSourceIndex)
                putExtra("urlIndex", urlIndex)
                putExtra("vodurl", vodUrl)
                putExtra("vodLong", controller.duration)
//                putExtra("device", device )
                println("vodurl+=${vodUrl}")
                val newPlayFromList = ArrayList<PlayFromBean>()
                playFormList.map {
                    newPlayFromList.add(it)
                }
                putParcelableArrayListExtra("playFormList", newPlayFromList)
                ActivityUtils.startActivity(this)
            }
        } else {
            runOnUiThread {
                ToastUtils.showShort("???????????????...")
            }
        }
    }

    private fun changeTitle() {
        var title = mVodBean.vod_name
        if (mVodBean.type_id == 2) {
            if (playList != null) {
                title += " ${playList!![urlIndex].name}"
            }
        }
        controller.setTitle(title)
    }

    private fun chengeNextLine() {
        curParseIndex++
        parseData()
    }

    private fun chengeNextLineFromHead() {
        curParseIndex = 0
        curFailIndex = -1
        LogUtils.d("=====?????? chengeNextLineFromHead")
        parseData()
    }

    private fun showSpeedListDialog(pos: Int) {
        SpeedListDialog(mActivity, this, pos).show()
    }

    private fun showPlayListDialog() {
        if (playList != null) {
            PlayListDialog(mActivity, urlIndex, playList!!)
                    .show()
        }
    }

    private fun showPlaySourceDialog() {
        PlaySourceDialog(mActivity, playSourceIndex, playFormList)
                .show()
    }

    private fun showCastScreenDialog() {
        if (isAllowCastScreen) {
//            val dialogHeight = if (isLandscape) {
//                -1
//            } else {
//                videoView.height
//            }
//            CastScreenDialog(mActivity, dialogHeight)
//                    .show()
//            val url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"
            val danListPop = DlanListPop(this@NewPlayActivity) {

                castScreen(it)
            }
            XPopup.Builder(this@NewPlayActivity)
                    .asCustom(danListPop)
                    .show()
        } else {
            runOnUiThread {
                ToastUtils.showShort("??????????????????????????????????????????")
            }
        }
    }

    private fun getVideoDetail() {
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {

        }
        RequestManager.execute(this, vodService.getVideoDetail(mVodBean.vod_id, 10),
                object : PlayLoadingObserver<VodBean>(mActivity) {
                    override fun onSuccess(data: VodBean) {
                        mVodBean = data
                        curParseIndex = 0
                        curFailIndex = -1
                        // playScoreInfo = LitePal.where("vodId = ?", data.vod_id.toString()).findFirst(PlayScoreBean::class.java)
                        playScoreInfo = App.curPlayScoreBean
                        urlIndex = playScoreInfo?.urlIndex ?: 0//??????????????????????????????
                        playSourceIndex = playScoreInfo?.playSourceIndex ?: 0//???????????????

                        val playInfo = data.playInfo
                        if (playInfo != null) {
                            playSourceIndex = playInfo.playSourceIndex
                            urlIndex = playInfo.urlIndex
                            videoNetProgress = playInfo.curProgress
                            curProgressHistory = videoNetProgress
                        }

//                        runOnUiThread {
//                        ToastUtils.showShort("?????????playInfo=" + playInfo + "  videoNetProgress=" + videoNetProgress)
//                        }

                        playFormList = data.vod_play_list
                        if (data.vod_play_list.isNullOrEmpty() || data.vod_play_url.isEmpty() || data.getVod_play_from().equals("no")) {
                            HitDialog(this@NewPlayActivity)
                                    .setTitle(StringUtils.getString(R.string.tip))
                                    .setMessage("????????????????????????????????????")
                                    .setOnHitDialogClickListener(object : HitDialog.OnHitDialogClickListener() {
                                        override fun onCancelClick(dialog: HitDialog) {
                                            super.onCancelClick(dialog)
                                            finish()
                                        }

                                        override fun onOkClick(dialog: HitDialog) {
                                            super.onOkClick(dialog)
                                            finish()
                                        }
                                    })
                                    .show()
                            return
                        }
                        if (data.vod_play_list != null) {
                            playFrom = data.vod_play_list!![playSourceIndex]
                        }
                        playList = playFrom.urls
                        LogUtils.d("=====?????? getVideoDetail")
                        parseData()
                        showVideoDetail()
                    }

                    override fun onError(e: ResponseException) {
                        finish()
                    }

                })

    }

    private fun checkVodTrySee() {
        if (playList == null) {
            return
        }
        val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return;
        }
        RequestManager.execute(
                mActivity,
                vodService.checkVodTrySee(mVodBean.vod_id.toString(), 1.toString(), playList!![urlIndex].nid.toString()),
                object : BaseObserver<CheckVodTrySeeBean>() {
                    override fun onSuccess(data: CheckVodTrySeeBean) {
                        var isVip = false
                        if (UserUtils.isLogin() && UserUtils.userInfo?.group_id == 3) {
                            isVip = true
                        }
                        val status = data.status
                        isAllowCastScreen = status == 0 || (isVip && status == 1)
                        controller.CheckVodTrySeeBean(data.user_video, data, isVip, mVodBean.vod_points_play)
                    }

                    override fun onError(e: ResponseException) {
                        isAllowCastScreen = false
                    }
                })
    }

    private fun parseData() {
        LogUtils.d("=====?????? parseData")
        if (isPlay) {
            videoView.release()
        }
        isParseSuccess = false
        isPlay = false
        showPlayerAd()
        showAnnouncement()
        // ??????????????????
        val parse = playFrom.player_info.parse2
        var url: String = ""
        if (playList != null) {
            url = playList!![urlIndex].url
        }
        LogUtils.d("", "====Parse start url=" + url + "  parse=" + parse)
        checkVodTrySee()
        changeTitle()

        if (url.endsWith(".mp4") || url.endsWith(".m3u8")) {


//            getSameActorData(url)
            isPlay = true
            curPlayUrl = url
            play(url)

        } else {
            isSuccess = false
            controller.showJiexi()
            LogUtils.d("", "====Parse start url=" + "??????????????????")
            JieXiUtils2.INSTANCE.getPlayUrl(parse, url, curParseIndex, onJiexiResultListener, curFailIndex)
        }
    }

    private fun showPlayerAd() {
        App.playAd?.let {
            if (it.img.isNotEmpty()) {
                controller.showAd(it.img, it.url)
            }
        }
    }

    private fun showAnnouncement() {
        runOnUiThread {
            App.startBean?.document?.roll_notice?.let {
                if (it.content.isNotEmpty() && it.status == "1") {
                    controller.showAnnouncement(it.content)
                }
            }
        }
    }

    private var isSuccess = false
    private fun play(url: String) {


//        val url="https://vip.fwwmy1.cn/api/data/youku/62ba0f501791adb51d22b699566bda7b.m3u8"
//        val url="https://vip.fwwmy1.cn/zhilian.php?url=https://vip.fwwmy1.cn/api/data/youku/62ba0f501791adb51d22b699566bda7b.m3u8"
        //??????
//        https://v5.monidai.com/20200905/ZWb6ezWP/index.m3u8
//        https://v5.szjal.cn/20200905/ZWb6ezWP/index.m3u8
        println("---play----" + url)

        isSuccess = true
        startTimer()
        controller.hideJiexi()
        LogUtils.d("", "====Parse play url=" + url)
        getSameActorData(url)
    }

    //???????????????????????????
    private fun recordPlay() {
        if (playScoreInfo == null) {
            playScoreInfo = PlayScoreBean().apply {
                vodId = mVodBean.vod_id
                typeId = mVodBean.type_id
                vodName = mVodBean.vod_name
                vodImgUrl = mVodBean.vod_pic
                percentage = controller.percentage
                curProgress = controller.curProgress
                playSourceIndex = this@NewPlayActivity.playSourceIndex
                if (playList != null) {
                    urlIndex = this@NewPlayActivity.urlIndex
                    vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
                }
                save()
            }
        } else {
            playScoreInfo?.run {
                percentage = controller.percentage
                curProgress = controller.curProgress
                playSourceIndex = this@NewPlayActivity.playSourceIndex
                if (playList != null) {
                    urlIndex = this@NewPlayActivity.urlIndex
                    vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name
                }
                saveOrUpdate("vodId = ?", mVodBean.vod_id.toString())
            }
        }
    }


    private fun savePlayRecord(isClose: Boolean) {

        // var percentage = controller.percentage
        val curProgress = controller.curProgress
        if (curProgress == 0L) {
            if (isClose) {
                finish()
            }
            return
        }
//        runOnUiThread {
//        ToastUtils.showShort("???????????????" + curProgress)
//        }
        if (curProgress != 0L) {
            curProgressHistory = curProgress
        }

        val voidid = mVodBean.vod_id.toString()
        Log.e(TAG, "======voidid===$voidid")

        var percentage = controller.percentage.toString()
        if (percentage == "NaN") {
            percentage = "0.0"
        }

        println("?????? ---savePlayRecord---  curProgress=" + curProgress)

        if (this@NewPlayActivity.playList.isNullOrEmpty() && isClose) {
            finish()
            return
        }

        val urlIndex = this@NewPlayActivity.urlIndex
        val vodSelectedWorks = this@NewPlayActivity.playList!![urlIndex].name


        var playSource = ""
        if (mVodBean.vod_play_list != null) {
            val playFromBean = mVodBean.vod_play_list!!.get(playSourceIndex)
            val playerInfo = playFromBean.player_info
            val urls = playFromBean.urls
            playSource = playerInfo.show
        }
        if (StringUtils.isEmpty(playSource)) {
            playSource = "??????"
        }


        if (UserUtils.isLogin()) {
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
            if (AgainstCheatUtil.showWarn(vodService)) {
                return;
            }
            Log.d(TAG, "voidid=${voidid}  vodSelectedWorks=${vodSelectedWorks}  playSource=${playSource}  percentage=${percentage} curProgress=${curProgress}")
            RequestManager.execute(this, vodService.addPlayLog(voidid, vodSelectedWorks, playSource.toString(), percentage, urlIndex.toString(), curProgress.toString(), playSourceIndex.toString()),
                    object : BaseObserver<UserVideo>() {
                        override fun onSuccess(data: UserVideo) {
                            Log.i("play", "addPlayLogsucess")
                            val intent = Intent("android.intent.action.AddPlayScore")
                            sendBroadcast(intent)
                            if (isClose) {
                                finish()
                            }
                        }

                        override fun onError(e: ResponseException) {
                            Log.i("play", "addPlayfaied")
                            if (isClose) {
                                finish()
                            }
                        }
                    })
            println("watchVideoLong==$watchVideoLong")
            if (watchVideoLong != 0) {
                RequestManager.execute(this, vodService.addWatchTime(watchVideoLong),
                        object : BaseObserver<GetScoreBean>() {
                            override fun onSuccess(data: GetScoreBean) {
                                if (data.score != "0") {
                                    runOnUiThread {
                                        ToastUtils.showShort("?????????30???????????????${data.score}??????")
                                    }
                                }
                            }

                            override fun onError(e: ResponseException) {
                                println("watchVideoLong==  onError")
                            }
                        })
            }
        } else {
            if (isClose) {
                finish()
            }
        }


//        RequestManager.execute(
//                mActivity,
//                vodService.shareScore(),
//                object : BaseObserver<ShareBean>() {
//                    override fun onSuccess(data: ShareBean) {
//        runOnUiThread {
//                        ToastUtils.showShort(data.info)
//        }
//                        EventBus.getDefault().post(LoginBean())
//                    }
//
//                    override fun onError(e: ResponseException) {
//                    }
//                }
//        )

    }

    //???????????????
    private fun playNext() {
        curProgressHistory = 0
        isSeekToHistory = false
        videoNetProgress = 0
        if (++urlIndex >= playFrom.urls.size) {
            urlIndex = 0
        }
        changeVideoUrlIndex()
        parseData()


    }

    //??????vip
    private fun updateVip() {
        if (!UserUtils.isLogin()) {
            LoginActivity.start()
        } else {
            if (UserUtils.userInfo?.group_id != 3) {
                val intent = Intent(mActivity, PayActivity::class.java)
                intent.putExtra("type", 1)
                ActivityUtils.startActivity(intent)
            } else {
                checkVodTrySee()
            }
        }
    }

    //????????????
    private fun payPlay() {
        if (!UserUtils.isLogin()) {
            LoginActivity.start()
        } else {
            if (playList == null) {
                return
            }
            val vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
            if (AgainstCheatUtil.showWarn(vodService)) {
                return;
            }
            RequestManager.execute(
                    mActivity,
                    vodService.buyVideo(4.toString(), mVodBean.vod_id.toString(), playFrom.sid.toString(), playList!![urlIndex].nid.toString(), 1.toString()),
                    object : BaseObserver<String>() {
                        override fun onSuccess(data: String) {
                            runOnUiThread {
                                ToastUtils.showShort("???????????????")
                            }
                            checkVodTrySee()
                        }

                        override fun onError(e: ResponseException) {
                        }

                    }
            )
        }
    }

    private var lbm: LocalBroadcastManager? = null
    private val localReceiver = LocalReceiver(this@NewPlayActivity)


    private fun registerReceiver() {
        lbm = LocalBroadcastManager.getInstance(this@NewPlayActivity)
        lbm!!.registerReceiver(localReceiver, IntentFilter("cn.whiner.av.AvVideoController"))
    }

    class LocalReceiver(act: NewPlayActivity) : BroadcastReceiver() {

        private var act = act

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "cn.whiner.av.AvVideoController") {
                when (intent.getIntExtra("type", RECEIVER_TYPE_TIMER)) {
                    RECEIVER_TYPE_REPLAY -> {
                        act.isSeekToHistory = true
                    }
                    RECEIVER_TYPE_TIMER -> {
                        val isFromHead = intent.getBooleanExtra("isFromHead", false)
                        if (isFromHead) {
                            act.chengeNextLineFromHead()
                        } else {
                            act.chengeNextLine()
                        }
                    }
                }
            }
        }
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var watchVideoLong = 0
    private var index = 0

    private fun startTimer() {
        if (timer == null && timerTask == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    if (index == 0) {
                        index++
                    } else {
                        savePlayRecord(false)
                        watchVideoLong += 60
                    }
                }
            }
            timer!!.schedule(timerTask, 0, 1000 * 30)
        }
    }

    private fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
    }

    override fun onSpeedItemClick(speed: String) {
        controller.setSpeedSelect(speed)
    }

    private fun sendDanmu(content: String) {
        if (content.isEmpty()) {
            return
        }
        var vodService = Retrofit2Utils.INSTANCE.createByGson(VodService::class.java)
        if (AgainstCheatUtil.showWarn(vodService)) {
            return;
        }
        RequestManager.execute(
                mActivity,
                vodService.sendDanmu(content, mVodBean.vod_id.toString(), System.currentTimeMillis().toString()),
                object : BaseObserver<GetScoreBean>() {
                    override fun onSuccess(data: GetScoreBean) {
                        if (data.score != "0") {
                            runOnUiThread {
                                ToastUtils.showShort("???????????????????????????${data.score}??????")
                            }
                        }
                    }

                    override fun onError(e: ResponseException) {
                        runOnUiThread {
                            ToastUtils.showShort(e.getErrorMessage())
                        }
                    }
                })
    }

    fun getPercentage(curPosition: Long, duration: Long): Float {
        val percentage: Float = curPosition / (duration * 1.0f)
        val df = DecimalFormat("#.00")
        val dfs = DecimalFormatSymbols()
        dfs.decimalSeparator = '.'
        df.decimalFormatSymbols = dfs
        return java.lang.Float.valueOf(df.format(percentage.toDouble()))
    }

    fun getSameActorData(url: String): String {
        var p1: String = ""
        OkHttpUtils.getInstance().getDataAsynFromNet(url, object : OkHttpUtils.MyNetCall {
            override fun success(call: Call?, response: Response?) {
                val playUrl: String? = response?.body?.string()
                Log.d("222ssss", playUrl + "_______")
                if (playUrl?.contains("http")!! && playUrl.contains("m3u8")) {
                    val rgex = "http(.*?)m3u8"
                    p1 = cn.mahua.vod.utils.StringUtils.getSubUtil(playUrl, rgex)[0]
                    getSameActorData(p1)
                } else {
                    videoView.post {

                        if (url.startsWith("//")) {
                            videoView.setUrl("https:$url")
                        } else {
                            videoView.setUrl(url)
                        }
                        videoView.start()
//            controller.setCurProgress(playScoreInfo?.percentage ?: 0f)
                    }
                }
            }

            override fun failed(call: Call?, e: IOException?) {
                mActivity.runOnUiThread {
                    Toast.makeText(this@NewPlayActivity, "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
                }
            }
        })
        return p1
    }

}
