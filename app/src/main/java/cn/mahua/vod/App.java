package cn.mahua.vod;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import com.dpuntu.downloader.DownloadManager;
import com.dpuntu.downloader.Downloader;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.orhanobut.hawk.Hawk;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.commonsdk.UMConfigure;

import org.litepal.LitePal;
import org.xutils.x;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.mahua.av.play.MyIjkPlayerFactory;
import cn.mahua.vod.base.BaseApplication;
import cn.mahua.vod.bean.AppConfigBean;
import cn.mahua.vod.bean.PlayScoreBean;
import cn.mahua.vod.bean.StartBean;
import cn.mahua.vod.download.GetFileSharePreance;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import jaygoo.library.m3u8downloader.M3U8Library;

public class App extends BaseApplication {
    private static final String TAG = "App";

    public static List<String> searchHot;
    public static StartBean startBean;
    public static AppConfigBean playAd;
    public static AppConfigBean tagConfig;

    private static WeakReference<App> weakReference;
    private static App vocApp;
    public static List<Downloader> downloaders = new ArrayList<>();

    public static PlayScoreBean curPlayScoreBean;

    public static App getInstance() {
        return weakReference.get();
    }


    public static App getApplication() {
        return vocApp;
    }

    // ?????????????????? static ?????????????????????????????????
    static {
//        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
//            @NonNull
//            @Override
//            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
//                MaterialHeader materialHeader = new MaterialHeader(context);
//                materialHeader.setColorSchemeColors(0xFFFF6600);
//                return materialHeader;
//            }
//        });
        //???????????????Footer?????????
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //???????????????Footer???????????? BallPulseFooter
                return new ClassicsFooter(context);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            setRxJavaErrorHandler();
        } catch (Exception e) {
        }
        LitePal.initialize(this);
        UMConfigure.init(this, "5f7335cb80455950e49c5fd6", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        weakReference = new WeakReference<>(this);
        vocApp = this; //xUtils ?????????
        x.Ext.init(this);
        x.Ext.setDebug(true);//????????????Debug??????

        Hawk.init(this).build();
        DownloadManager.initDownloader(vocApp);

        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.i(getClass().getName().toString(), "initX5Environment onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.i(getClass().getName().toString(), "initX5Environment onViewInitFinished");
            }
        });



        //????????????????????????????????????????????????????????????
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
//                .setLogEnabled(BuildConfig.DEBUG)
                .setPlayerFactory(MyIjkPlayerFactory.create(this))
                //.setPlayerFactory(ExoMediaPlayerFactory.create(this))
                //.setAutoRotate(true)
//                .setEnableMediaCodec(true)
                //.setUsingSurfaceView(true)
                //.setEnableParallelPlay(true)
                //.setEnableAudioFocus(true)
                //.setScreenScale(VideoView.SCREEN_SCALE_MATCH_PARENT)
                .build());
//        //??????toast?????????
//        ToastUtils.setBgColor(ContextCompat.getColor(this, R.color.colorAccent));

        M3U8Library.init(this);
    }

    public static int getSrceenWidth() {
        return ((WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }


    // ?????????????????? static ?????????????????????????????????
    static {
        //???????????????Header?????????
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
//                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//????????????????????????
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("????????? %s"));//???????????????Header???????????? ???????????????Header
            }
        });
        //???????????????Footer?????????
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //???????????????Footer???????????? BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    public synchronized static GetFileSharePreance getFileSharePreance(){
        return new GetFileSharePreance(vocApp);
    }

    /**
     * RxJava2 ??????????????????(dispose())???RxJava?????????????????????????????????(?????????????????????????????????????????????IO?????????),?????????RxJavaPlugin???????????????????????????ErrorHandler
     * ?????????http://engineering.rallyhealth.com/mobile/rxjava/reactive/2017/03/15/migrating-to-rxjava-2.html#Error Handling
     */
    private void setRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.d(TAG, "RxJavaPlugins throw test");
            }
        });
    }

}
