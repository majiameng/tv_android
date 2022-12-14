package cn.mahua.av.play;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.dueeeke.videoplayer.controller.GestureVideoController;
import com.dueeeke.videoplayer.controller.MediaPlayerControl;
import com.dueeeke.videoplayer.player.VideoView;
import com.dueeeke.videoplayer.util.PlayerUtils;
import com.xiaweizi.marquee.MarqueeTextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cn.mahua.av.CheckVodTrySeeBean;
import cn.mahua.av.R;

public class AvVideoController extends GestureVideoController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private int isNeedVip;
    private int trySeeTime;
    protected ControllerClickListener controllerClickListener;
    private RelativeLayout rl_fullscreen;
    public static final String KEY_SPEED_INDEX = "KEY_SPEED_INDEX";
    public static final int RECEIVER_TYPE_REPLAY = 1;
    public static final int RECEIVER_TYPE_TIMER = 2;
    private Boolean mReplayByCurProgress = false;

    @SuppressWarnings("unused")
    public void setControllerClickListener(ControllerClickListener controllerClickListener) {
        this.controllerClickListener = controllerClickListener;
    }

    private static final String KEY_IS_OPEN_DANMAKU = "KEY_IS_OPEN_DANMAKU";

    private static final String TAG = "AvVideoController";
    private Animation mShowAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_av_alpha_in);
    private Animation mHideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_av_alpha_out);

    protected ImageView iv_bg;
    //??????
    protected View v_all_bg;
    protected View v_top_bg;
    protected View v_bottom_bg;
    protected ImageView iv_back;
    protected MarqueeTextView tv_title;
    protected TextClock tc_localtime;
    protected ImageView iv_miracast;
    protected ImageView iv_download;
    //??????
    protected ImageView iv_lock;
    protected ImageView iv_pip;
    protected ImageView iv_scale;
    //??????
    protected ImageView iv_play;
    protected TextView tv_curr_time, tv_total_time, tv_playtime;
    protected SeekBar progress, sb_1, sb_2;//?????????????????????
    protected ImageView iv_fullscreen;
    protected ImageView iv_next;
    protected ImageView iv_danmaku;
    protected TextView tv_danmaku;
    protected TextView tvPlaySource;
    protected TextView tv_speed;
    protected TextView tv_hd;
    protected TextView tv_selected;
    private LinearLayout llPay;
    private LinearLayout llUpdate;
    private FrameLayout rlEndUpdate;
    private FrameLayout rlEndPay;
    private TextView tvUpdateTitle;
    private TextView tvPayTitle;
    private TextView tvEndPayTitle;
    private View v_av_top_bg;

    private FrameLayout llSkip;
    private ImageView awvPlayer;
    private TextView tvSkip;
    private TextView tvAvAnnouncement;

    ///??????View
    protected ImageView iv_replay;
    protected TextView tv_replay;
    //???????????????
    protected LinearLayout clpb_loading;
    //???????????????
    protected LinearLayout clpb_jiexi;
    protected TextView tvJiexiMsg;
    //????????????????????????
    private boolean mIsDragging;
    private Animation animation;
    private VideoViewImpt videoViewImpt;
    private WeakReference<AppCompatActivity> curActivity;

    public AvVideoController(VideoViewImpt impt, @NonNull Context context) {
        this(context);
        curActivity = new WeakReference<>((AppCompatActivity) context);
        this.videoViewImpt = impt;
    }

    public AvVideoController(@NonNull Context context) {
        this(context, null);
    }

    public AvVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_av_controller;
    }


    @Override
    protected void initView() {
        super.initView();
        Log.e(TAG, "initView");

        tvAvAnnouncement = mControllerView.findViewById(R.id.tv_av_announcement);
        v_av_top_bg = mControllerView.findViewById(R.id.v_av_top_bg);
        tvUpdateTitle = mControllerView.findViewById(R.id.tvUpdateTitle);
        tvJiexiMsg = mControllerView.findViewById(R.id.tvJiexiMsg);
        tvPayTitle = mControllerView.findViewById(R.id.tvPayTitle);
        tvEndPayTitle = mControllerView.findViewById(R.id.tvEndPayTitle);
        rlEndUpdate = mControllerView.findViewById(R.id.rlEndUpdate);
        rlEndPay = mControllerView.findViewById(R.id.rlEndPay);
        llPay = mControllerView.findViewById(R.id.llPay);
        llUpdate = mControllerView.findViewById(R.id.llUpdateVip);
        iv_bg = mControllerView.findViewById(R.id.iv_av_bg);
        llSkip = mControllerView.findViewById(R.id.llSkip);
        tvSkip = mControllerView.findViewById(R.id.tvSkip);
        awvPlayer = mControllerView.findViewById(R.id.awvPlayer);

        mControllerView.findViewById(R.id.tvPayButton).setOnClickListener(this);
        mControllerView.findViewById(R.id.tvUpdateButton).setOnClickListener(this);
        mControllerView.findViewById(R.id.tvEndPayButton).setOnClickListener(this);
        mControllerView.findViewById(R.id.tvEndUpdateButton).setOnClickListener(this);
        //????????????
        v_all_bg = mControllerView.findViewById(R.id.v_av_all_bg);
        //????????????
        v_top_bg = mControllerView.findViewById(R.id.v_av_top_bg);
        //????????????
        v_bottom_bg = mControllerView.findViewById(R.id.v_av_bottom_bg);
        //-------------??????
        //?????????
        iv_back = mControllerView.findViewById(R.id.iv_av_back);
        iv_back.setOnClickListener(this);
        mControllerView.findViewById(R.id.iv_av_back1).setOnClickListener(this);
        mControllerView.findViewById(R.id.iv_av_back2).setOnClickListener(this);
        //??????
        tv_title = mControllerView.findViewById(R.id.tv_av_title);
        //????????????
        tc_localtime = mControllerView.findViewById(R.id.tc_av_localtime);
        //??????
        iv_miracast = mControllerView.findViewById(R.id.iv_av_miracast);
        iv_miracast.setOnClickListener(this);
        //??????
        iv_download = mControllerView.findViewById(R.id.iv_av_download);
        iv_download.setOnClickListener(this);
        //--------------??????
        //??????
        iv_lock = mControllerView.findViewById(R.id.iv_av_lock);
        iv_lock.setOnClickListener(this);
        //?????????
        iv_pip = mControllerView.findViewById(R.id.iv_av_pip);
        iv_pip.setOnClickListener(this);
        //????????????
        iv_scale = mControllerView.findViewById(R.id.iv_av_scale);
        iv_scale.setOnClickListener(this);
        //---------------??????
        //??????
        iv_play = mControllerView.findViewById(R.id.iv_av_play);
        iv_play.setOnClickListener(this);
        //??????????????????
        tv_curr_time = mControllerView.findViewById(R.id.tv_av_curr_time);
        //???????????????
        tv_total_time = mControllerView.findViewById(R.id.tv_av_total_time);
        //??????????????????
        tv_playtime = mControllerView.findViewById(R.id.tv_av_playtime);
        //???????????????
        sb_1 = mControllerView.findViewById(R.id.sb_av_1);
        sb_1.setOnSeekBarChangeListener(null);
        sb_2 = mControllerView.findViewById(R.id.sb_av_2);
        sb_2.setOnSeekBarChangeListener(null);
        progress = sb_1;
        progress.setOnSeekBarChangeListener(this);
        //??????
        iv_fullscreen = mControllerView.findViewById(R.id.iv_av_fullscreen);
        rl_fullscreen = mControllerView.findViewById(R.id.rl_av_fullscreen);
        rl_fullscreen.setOnClickListener(this);
        //?????????
        iv_next = mControllerView.findViewById(R.id.iv_av_next);
        iv_next.setOnClickListener(this);
        //????????????
        iv_danmaku = mControllerView.findViewById(R.id.iv_av_danmaku);
        iv_danmaku.setOnClickListener(this);
        //??????????????????
        tvPlaySource = mControllerView.findViewById(R.id.tvPlaySource);
        tvPlaySource.setOnClickListener(this);

        //??????????????????
        tv_danmaku = mControllerView.findViewById(R.id.tv_av_danmaku);
        tv_danmaku.setOnClickListener(this);

        //?????????????????????
        boolean b = SPUtils.getInstance().getBoolean(KEY_IS_OPEN_DANMAKU, true);
        iv_danmaku.setSelected(b);
        //????????????
        tv_speed = mControllerView.findViewById(R.id.tv_av_speed);
        tv_speed.setOnClickListener(this);
        //?????????
        tv_hd = mControllerView.findViewById(R.id.tv_av_hd);
        tv_hd.setOnClickListener(this);
        //??????
        tv_selected = mControllerView.findViewById(R.id.tv_av_selected);
        tv_selected.setOnClickListener(this);
        //???????????????
        clpb_loading = mControllerView.findViewById(R.id.clpb_av_loading);
        clpb_loading.setVisibility(View.GONE);
        //???????????????
        clpb_jiexi = mControllerView.findViewById(R.id.clpb_av_jiexi);
        //????????????
        iv_replay = mControllerView.findViewById(R.id.iv_av_replay);
        iv_replay.setOnClickListener(this);
        tv_replay = mControllerView.findViewById(R.id.tv_av_replay);
    }

    int bufferTime = 0;
    TimerTask bufferTask;

    private void startBufferTimer() {
        if (bufferTimer != null) {
            bufferTimer.cancel();
        }
        if (bufferTask != null) {
            bufferTask.cancel();
        }
        bufferTimer = new Timer();
        bufferTask = new TimerTask() {
            @Override
            public void run() {
                if (bufferTime >= 8) {
                    bufferTime = 0;
                    bufferTimer.cancel();
                    bufferTimer = null;
                    bufferTask = null;
                    Intent intent = new Intent("cn.whiner.av.AvVideoController");
                    intent.putExtra("type", RECEIVER_TYPE_TIMER);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    System.out.println("????????????====");
                } else {
                    bufferTime++;
                }
            }
        };
        bufferTimer.schedule(bufferTask, 0, 1000);
    }

    //????????????
    @Override
    public void setPlayerState(int playerState) {
        if (mIsLocked) return;
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                Log.e(TAG, "????????????");
                if (animation != null && tvAvAnnouncement.getVisibility() == VISIBLE) {
                    animation.reset();
                    tvAvAnnouncement.startAnimation(animation);
                }
                mIsGestureEnabled = true;
                iv_back.setVisibility(VISIBLE);
                //????????????
                tc_localtime.setVisibility(GONE);
                //??????
                iv_download.setVisibility(GONE);
                //??????
                iv_lock.setVisibility(GONE);
                //?????????
                iv_pip.setVisibility(GONE);
                //????????????
                iv_scale.setVisibility(GONE);
                //??????????????????
                tv_curr_time.setVisibility(VISIBLE);
                //?????????
                tv_total_time.setVisibility(VISIBLE);
                //??????
                iv_fullscreen.setVisibility(VISIBLE);
                rl_fullscreen.setVisibility(VISIBLE);
                //seekBar
                sb_1.setVisibility(VISIBLE);
                sb_1.setOnSeekBarChangeListener(this);
                sb_2.setVisibility(GONE);
                sb_2.setOnSeekBarChangeListener(null);
                progress = sb_1;
                ///?????????
                iv_next.setVisibility(GONE);
                //????????????
                tv_playtime.setVisibility(GONE);
                //??????
                iv_danmaku.setVisibility(GONE);
                tv_danmaku.setVisibility(GONE);
                tvPlaySource.setVisibility(GONE);
                //????????????
                tv_speed.setVisibility(GONE);
                //???????????????
                tv_hd.setVisibility(GONE);
                //??????
                tv_selected.setVisibility(GONE);
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                Log.e(TAG, "????????????");
                if (animation != null && tvAvAnnouncement.getVisibility() == VISIBLE) {
                    animation.reset();
                    tvAvAnnouncement.startAnimation(animation);
                }
                mIsGestureEnabled = true;
                //????????????
                tc_localtime.setVisibility(VISIBLE);
                //??????
                iv_download.setVisibility(VISIBLE);
                //??????
                iv_lock.setVisibility(VISIBLE);
                //?????????
                iv_pip.setVisibility(VISIBLE);
                //????????????
                iv_scale.setVisibility(VISIBLE);
                //??????????????????
                tv_curr_time.setVisibility(GONE);
                //?????????
                tv_total_time.setVisibility(GONE);
                //??????
                iv_fullscreen.setVisibility(GONE);
                rl_fullscreen.setVisibility(GONE);
                //seekBar
                sb_1.setVisibility(GONE);
                sb_1.setOnSeekBarChangeListener(null);
                sb_2.setVisibility(VISIBLE);
                sb_2.setOnSeekBarChangeListener(this);
                progress = sb_2;
                ///?????????
                iv_next.setVisibility(VISIBLE);
                //????????????
                tv_playtime.setVisibility(VISIBLE);
                //??????
                iv_danmaku.setVisibility(VISIBLE);
                if (iv_danmaku.isSelected()) {
                    tv_danmaku.setVisibility(VISIBLE);
                    tvPlaySource.setVisibility(VISIBLE);
                } else {
                    tv_danmaku.setVisibility(GONE);
                    tvPlaySource.setVisibility(GONE);
                }
                //????????????
                tv_speed.setVisibility(VISIBLE);
                //???????????????
                tv_hd.setVisibility(VISIBLE);
                //??????
                tv_selected.setVisibility(VISIBLE);
                break;
        }
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
        if (curActivity.get() != null && playerState == VideoView.PLAYER_NORMAL) {
            layoutParams.topMargin = BarUtils.getStatusBarHeight();
            layoutParams.height = ConvertUtils.dp2px(45);
            BarUtils.setStatusBarVisibility(curActivity.get(), true);
        } else {
            layoutParams.height = ConvertUtils.dp2px(45);
            layoutParams.topMargin = ConvertUtils.dp2px(0);
            BarUtils.setStatusBarVisibility(curActivity.get(), false);
        }
        v_av_top_bg.setLayoutParams(layoutParams);
    }

    //????????????
    @Override
    public void setPlayState(int playState) {
        mCurrentPlayState = playState;
        hideStatusView();
        switch (playState) {
            case VideoView.STATE_IDLE:
                Log.e(TAG, "????????????");
                isHdChange = true;
                tv_hd.setText(R.string.av_hd);
                iv_bg.setVisibility(VISIBLE);
                clpb_loading.setVisibility(View.GONE);
                sb_1.setProgress(0);
                sb_1.setSecondaryProgress(0);
                sb_2.setProgress(0);
                sb_2.setSecondaryProgress(0);
                mIsLocked = false;
                mMediaPlayer.setLock(false);
                iv_lock.setSelected(false);
                iv_play.setSelected(false);
                //????????????
                v_all_bg.setVisibility(GONE);
                iv_replay.setVisibility(GONE);
                tv_replay.setVisibility(GONE);
                show();
                break;
            case VideoView.STATE_PLAYING:
                Log.e(TAG, "?????????");
                iv_bg.setVisibility(GONE);
                iv_play.setSelected(true);
                clpb_loading.setVisibility(View.GONE);
                post(mShowProgress);
                break;
            case VideoView.STATE_PAUSED:
                Log.e(TAG, "??????");
                iv_bg.setVisibility(GONE);
                iv_play.setSelected(false);
                break;
            case VideoView.STATE_PREPARING:
                Log.e(TAG, "?????????");
                startBufferTimer();
                onDanmakuChanged(true);
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.VISIBLE);
                //????????????
                v_all_bg.setVisibility(GONE);
                iv_replay.setVisibility(GONE);
                tv_replay.setVisibility(GONE);
                show();
                break;
            case VideoView.STATE_PREPARED:
                stopBufferTime();
//                hide();
                postDelayed(() -> {
                            mShowing = true;
                            hide();
                        },
                        8000L
                );

                Log.e(TAG, "????????????");
                iv_bg.setVisibility(GONE);
                break;
            case VideoView.STATE_ERROR:
                Log.e(TAG, "??????");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.GONE);
                //????????????
                v_all_bg.setVisibility(VISIBLE);
                iv_replay.setVisibility(VISIBLE);
                tv_replay.setVisibility(VISIBLE);
                tv_replay.setText(R.string.av_error);
                show();
                break;
            case VideoView.STATE_BUFFERING:
                Log.e(TAG, "?????????");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.VISIBLE);
                iv_play.setSelected(mMediaPlayer.isPlaying());
                break;
            case VideoView.STATE_BUFFERED:
                Log.e(TAG, "????????????");
                iv_bg.setVisibility(GONE);
                clpb_loading.setVisibility(View.GONE);
                iv_play.setSelected(mMediaPlayer.isPlaying());
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                Log.e(TAG, "????????????");
                iv_bg.setVisibility(VISIBLE);
                clpb_loading.setVisibility(View.GONE);
                mIsLocked = false;
                mMediaPlayer.setLock(false);
                iv_lock.setSelected(false);
                iv_play.setSelected(false);
                //????????????
                v_all_bg.setVisibility(VISIBLE);
                iv_replay.setVisibility(VISIBLE);
                tv_replay.setVisibility(VISIBLE);
                tv_replay.setText(R.string.av_replay);
                hide();
                break;
            default:
                Log.e(TAG, "????????????---" + playState);
                break;
        }
    }

    @Override
    public void show() {
        show(mDefaultTimeout);
    }

    @Override
    public void hide() {
        if (mShowing) {
            iv_lock.setVisibility(GONE);
            if (!mIsLocked) {
                setAllView(false);
            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
            if (curActivity.get() != null && !mMediaPlayer.isFullScreen()) {
                layoutParams.topMargin = BarUtils.getStatusBarHeight();
                layoutParams.height = ConvertUtils.dp2px(45);
                BarUtils.setStatusBarVisibility(curActivity.get(), false);
            } else {
                layoutParams.height = ConvertUtils.dp2px(45);
                layoutParams.topMargin = ConvertUtils.dp2px(0);
            }
            v_av_top_bg.setLayoutParams(layoutParams);

            //??????????????????
            if (speedPop != null && speedPop.isShowing()) {
                speedPop.dismiss();
            }
            //???????????????
            if (hdPop != null && hdPop.isShowing()) {
                hdPop.dismiss();
            }
            mShowing = false;
        }
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public float getPercentage() {
//        if (mMediaPlayer.getDuration() == 0) {
//            return 100f;
//        }
        float percentage = mMediaPlayer.getCurrentPosition() / (mMediaPlayer.getDuration() * 1.0f);
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        System.out.println("??????11???curr=" + mMediaPlayer.getCurrentPosition() + " duration=" + mMediaPlayer.getDuration() + " percentage=" + percentage + "   >" + df.format(percentage));
        return Float.valueOf(df.format(percentage));
    }

    public long getCurProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void CheckVodTrySeeBean(int freeCount, CheckVodTrySeeBean bean, boolean isVip, int vod_point_pay) {
        rlEndUpdate.setVisibility(View.GONE);
        rlEndPay.setVisibility(View.GONE);
        int status = bean.getStatus();
        if (status == 0) {
            isNeedVip = 0;
            trySeeTime = bean.getTrysee();
            llPay.setVisibility(View.GONE);
            llUpdate.setVisibility(View.GONE);
            return;
        }
        if (freeCount > 0) {
            isNeedVip = 0;
            trySeeTime = bean.getTrysee();
            llPay.setVisibility(View.GONE);
            llUpdate.setVisibility(View.GONE);
            return;
        }

        isNeedVip = status;
        trySeeTime = bean.getTrysee();

        if (status == 1) {
            if (isVip) {
                isNeedVip = 0;
                llPay.setVisibility(View.GONE);
                llUpdate.setVisibility(View.GONE);
            } else {
                llPay.setVisibility(View.GONE);
                llUpdate.setVisibility(View.VISIBLE);
                tvUpdateTitle.setText("?????????" + trySeeTime + "??????????????????????????? ");
            }
        } else if (status == 2) {
            llPay.setVisibility(View.VISIBLE);
            llUpdate.setVisibility(View.GONE);
            tvPayTitle.setText("?????????" + trySeeTime + "?????? ???????????????????????????" + vod_point_pay + "?????? ");
            tvEndPayTitle.setText("?????????" + trySeeTime + "?????? ???????????????????????????\n" + vod_point_pay + "??????");
        }
    }

    Timer timer = null;
    Timer bufferTimer = null;

    public void showAd(String gifUrl, String url) {
        llSkip.setVisibility(View.VISIBLE);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (!TextUtils.isEmpty(url))
            llSkip.setOnClickListener(v -> ActivityUtils.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        if (gifUrl.endsWith(".gif"))
            Glide.with(this).asGif().load(gifUrl).centerCrop().into(awvPlayer);
        else
            Glide.with(this).asBitmap().load(gifUrl).centerCrop().into(awvPlayer);

        final int[] time = {6};
        tvSkip.setText(time[0] + "s");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                post(() -> {
                    if (--time[0] <= 0) {
                        llSkip.setVisibility(View.GONE);
                        timer.cancel();
                        timer = null;
                    } else {
                        tvSkip.setText(time[0] + "s");
                    }
                });
            }
        }, 1000, 1000);
    }

    public void showAnnouncement(String announcement) {
        tvAvAnnouncement.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvAvAnnouncement.getLayoutParams();
        layoutParams.width = ConvertUtils.dp2px(1100);
        tvAvAnnouncement.setLayoutParams(layoutParams);
        tvAvAnnouncement.setText(announcement);
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.recomment_in_left);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tvAvAnnouncement.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        tvAvAnnouncement.setAnimation(animation);
    }

    private void show(int timeout) {
        if (!mShowing) {
            if (mMediaPlayer.isFullScreen()) {
                iv_lock.setVisibility(VISIBLE);
            }
            if (!mIsLocked) {
                setAllView(true);
            }

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v_av_top_bg.getLayoutParams();
            if (curActivity.get() != null && !mMediaPlayer.isFullScreen()) {
                layoutParams.topMargin = BarUtils.getStatusBarHeight();
                layoutParams.height = ConvertUtils.dp2px(45);
                BarUtils.setStatusBarVisibility(curActivity.get(), true);
            } else {
                layoutParams.height = ConvertUtils.dp2px(45);
                layoutParams.topMargin = ConvertUtils.dp2px(0);
            }
            v_av_top_bg.setLayoutParams(layoutParams);
            mShowing = true;
        }
        removeCallbacks(mFadeOut);
        if (timeout != 0) {
            postDelayed(mFadeOut, timeout);
        }
    }

    //????????????view
    private void setAllView(boolean isShow) {
        autoSetVisibility(v_top_bg, isShow);
        autoSetVisibility(v_bottom_bg, isShow);
        autoSetVisibility(iv_back, isShow);
        autoSetVisibility(tv_title, isShow);
        autoSetVisibility(tc_localtime, isShow);
        autoSetVisibility(iv_miracast, isShow);
        autoSetVisibility(iv_download, isShow);
        autoSetVisibility(iv_lock, isShow);
        autoSetVisibility(iv_pip, isShow);
        autoSetVisibility(iv_scale, isShow);
        autoSetVisibility(iv_play, isShow);
        autoSetVisibility(tv_curr_time, isShow);
        autoSetVisibility(tv_total_time, isShow);
        autoSetVisibility(tv_playtime, isShow);
        autoSetVisibility(sb_1, isShow);
        autoSetVisibility(sb_2, isShow);
        autoSetVisibility(iv_fullscreen, isShow);
        autoSetVisibility(rl_fullscreen, isShow);
        autoSetVisibility(iv_next, isShow);
        autoSetVisibility(iv_danmaku, isShow);
        autoSetVisibility(tv_danmaku, isShow);
        autoSetVisibility(tvPlaySource, isShow);
        autoSetVisibility(tv_speed, isShow);
        autoSetVisibility(tv_hd, isShow);
        autoSetVisibility(tv_selected, isShow);
    }

    //??????view?????????GONE?????????????????????View
    private void autoSetVisibility(View view, boolean isShow) {
        if (view.getVisibility() == GONE) return;
        if (isShow) {
            view.setVisibility(VISIBLE);
            view.startAnimation(mShowAnim);
        } else {
            view.setVisibility(INVISIBLE);
            view.startAnimation(mHideAnim);
        }
    }

    @Override
    protected int setProgress() {
        if (mMediaPlayer == null || mIsDragging) {
            return 0;
        }

        //   clpb_loading.setVisibility(View.GONE);
        long position = mMediaPlayer.getCurrentPosition();
        long duration = mMediaPlayer.getDuration();
        if (isNeedVip != 0) {
            if (position / 1000f > trySeeTime * 60) {
                if (isNeedVip == 1) {
                    rlEndUpdate.setVisibility(View.VISIBLE);
                    rlEndPay.setVisibility(View.GONE);
                } else {
                    rlEndPay.setVisibility(View.VISIBLE);
                    rlEndUpdate.setVisibility(View.GONE);
                }
                mMediaPlayer.pause();
            }
        }
        if (progress != null) {
            if (duration > 0) {
                progress.setEnabled(true);
                int pos = (int) (position * 1.0 / duration * progress.getMax());
                progress.setProgress(pos);
            } else {
                progress.setEnabled(false);
            }
            int percent = mMediaPlayer.getBufferedPercentage();
            if (percent >= 95) { //????????????????????????100%??????
                progress.setSecondaryProgress(progress.getMax());
            } else {
                progress.setSecondaryProgress(percent * 10);
            }
        }

        if (tv_total_time != null)
            tv_total_time.setText(stringForTime((int) duration));
        if (tv_curr_time != null)
            tv_curr_time.setText(stringForTime((int) position));
        if (tv_playtime != null) {
            String text = stringForTime((int) position) + "/" + stringForTime((int) duration);
            tv_playtime.setText(text);
        }

        return (int) position;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (!b) {
            return;
        }
        long duration = mMediaPlayer.getDuration();
        long newPosition = (duration * i) / progress.getMax();
        if (tv_curr_time != null)
            tv_curr_time.setText(stringForTime((int) newPosition));
        if (tv_playtime != null) {
            String text = stringForTime((int) newPosition) + "/" + stringForTime((int) duration);
            tv_playtime.setText(text);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsDragging = true;
        removeCallbacks(mShowProgress);
        removeCallbacks(mFadeOut);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = mMediaPlayer.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / progress.getMax();
        mMediaPlayer.seekTo((int) newPosition);
        mIsDragging = false;
        post(mShowProgress);
        show();
    }


    @Override
    public void setMediaPlayer(MediaPlayerControl mediaPlayer) {
        super.setMediaPlayer(mediaPlayer);
    }

    //?????????
    @Override
    public boolean onBackPressed() {
        if (mIsLocked) {
            show();
            ToastUtils.showShort(R.string.av_lock_tip);
            return true;
        }

        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity == null) return super.onBackPressed();
        if (mMediaPlayer.isFullScreen()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mMediaPlayer.stopFullScreen();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        show();
        int i = view.getId();
        if (view.getId() == R.id.tvEndUpdateButton || view.getId() == R.id.tvUpdateButton) {
            if (mMediaPlayer.isFullScreen()) {

                doStartStopFullScreen();
            }
            if (controllerClickListener != null) controllerClickListener.onClick(view);

        } else if (view.getId() == R.id.tvEndPayButton || view.getId() == R.id.tvPayButton) {
            if (mMediaPlayer.isFullScreen()) {
                doStartStopFullScreen();
            }
            if (controllerClickListener != null) controllerClickListener.onClick(view);

        } else if (view.getId() == R.id.iv_av_lock) {//???????????????
            doLockUnlock();
        } else if (i == R.id.iv_av_back) {//?????????
            if (mMediaPlayer.isFullScreen()) {
                doStartStopFullScreen();
            } else {
                //???????????????
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_back1) {//?????????
            if (mMediaPlayer.isFullScreen()) {
                doStartStopFullScreen();
            } else {
                //???????????????
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_back2) {//?????????
            if (mMediaPlayer.isFullScreen()) {
                doStartStopFullScreen();
            } else {
                //???????????????
                if (controllerClickListener != null) controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_miracast) {//??????
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_download) {//??????
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_pip) {//?????????
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_scale) {//????????????
            doScale();
        } else if (i == R.id.iv_av_play) {//??????
            doPauseResume();
        } else if (i == R.id.rl_av_fullscreen) {//??????
            doStartStopFullScreen();
        } else if (i == R.id.iv_av_next) {//??????
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.iv_av_danmaku) {//????????????
            onDanmakuChanged(false);
        } else if (i == R.id.tv_av_danmaku) {//?????????????????????
            showDanmaku();
        } else if (i == R.id.btn_pop_danmaku) {//????????????
            sendDanmaku();
            if (controllerClickListener != null) {
                view.setTag(et_danmaku.getText().toString());
                controllerClickListener.onClick(view);
            }
            et_danmaku.setText("");
        } else if (i == R.id.tv_av_speed) {//??????????????????
            if (controllerClickListener != null) {
                hide();
                view.setTag(curSpeedSelect);
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.tv_av_hd) {//???????????????
            if (controllerClickListener != null) controllerClickListener.onClick(view);
        } else if (i == R.id.tv_av_selected) {//??????
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else if (i == R.id.iv_av_replay) {//????????????
            if (mReplayByCurProgress) {
                mMediaPlayer.replay(true);
                Intent intent = new Intent("cn.whiner.av.AvVideoController");
                intent.putExtra("type", RECEIVER_TYPE_REPLAY);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                mReplayByCurProgress = false;
                System.out.println("??????6???==");
            } else {
                mMediaPlayer.replay(true);
                System.out.println("??????7???==");
            }
        } else if (i == R.id.tvPlaySource) {//???????????????
            if (controllerClickListener != null) {
                hide();
                controllerClickListener.onClick(view);
            }
        } else {
            ToastUtils.showShort("??????View" + view.getId());
        }
    }

    //????????????
    protected void doLockUnlock() {
        if (mIsLocked) {
            mIsLocked = false;
            mIsGestureEnabled = true;
            mShowing = false;
            show();
            ToastUtils.showShort(R.string.av_unlocked);
        } else {
            hide();
            mIsLocked = true;
            mIsGestureEnabled = false;
            ToastUtils.showShort(R.string.av_locked);
        }
        iv_lock.setSelected(mIsLocked);
        mMediaPlayer.setLock(mIsLocked);
    }

    //??????????????????
    private int scale_val = 0;

    protected void doScale() {
        scale_val++;
        if (scale_val >= 6) scale_val = 0;
        switch (scale_val) {
            case 0:
                ToastUtils.showShort("??????");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_DEFAULT);
                break;
            case 1:
                ToastUtils.showShort("16:9");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_16_9);
                break;
            case 2:
                ToastUtils.showShort("4:3");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_4_3);
                break;
            case 3:
                ToastUtils.showShort("??????");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_MATCH_PARENT);
                break;
            case 4:
                ToastUtils.showShort("????????????");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_ORIGINAL);
                break;
            case 5:
                ToastUtils.showShort("????????????");
                mMediaPlayer.setScreenScale(VideoView.SCREEN_SCALE_CENTER_CROP);
                break;
            default:
                break;
        }
    }

    //????????????
    public void onDanmakuChanged(boolean isFirst) {
        if (videoViewImpt == null) return;
        if (iv_danmaku == null || tv_danmaku == null) return;
        if (isFirst) {
            boolean b = SPUtils.getInstance().getBoolean(KEY_IS_OPEN_DANMAKU, true);
            Log.e(TAG, b + "2");
            //?????????????????????
            iv_danmaku.setSelected(b);
            if (b) {
                videoViewImpt.showDanmaku();
            } else {
                videoViewImpt.hideDanmaku();
            }
        } else {
            if (!iv_danmaku.isSelected()) {
                //ToastUtils.showShort("?????????");
                iv_danmaku.setSelected(true);
                videoViewImpt.showDanmaku();
                SPUtils.getInstance().put(KEY_IS_OPEN_DANMAKU, true);
                if (iv_danmaku.getVisibility() == VISIBLE) {
                    tv_danmaku.setVisibility(VISIBLE);
                    tvPlaySource.setVisibility(VISIBLE);
                }
            } else {
                //ToastUtils.showShort("?????????");
                iv_danmaku.setSelected(false);
                videoViewImpt.hideDanmaku();
                SPUtils.getInstance().put(KEY_IS_OPEN_DANMAKU, false);
                tv_danmaku.setVisibility(GONE);
                tvPlaySource.setVisibility(GONE);
            }
        }
    }

    //?????????????????????
    private PopupWindow popupWindow;
    private EditText et_danmaku;

    protected void showDanmaku() {
        if (popupWindow == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_danmaku, this, false);
            et_danmaku = view.findViewById(R.id.et_pop_danmaku);
            Button btn_danmaku = view.findViewById(R.id.btn_pop_danmaku);
            btn_danmaku.setOnClickListener(this);
            popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popupWindow.setBackgroundDrawable(new ColorDrawable(0xffdadada));
            popupWindow.setOutsideTouchable(true);
        }
        popupWindow.showAtLocation(this, Gravity.BOTTOM, 0, 0);
    }

    //????????????---???????????????????????????
    private void sendDanmaku() {
        if (videoViewImpt == null) return;
        if (et_danmaku != null) {
            String str = et_danmaku.getText().toString();
            if (!StringUtils.isEmpty(str)) {
                videoViewImpt.addDanmaku(str, true);
            } else {
                ToastUtils.showShort("??????????????????");
            }
        }
    }

    public void setReplayByCurProgress(Boolean replayByCurProgress) {
        mReplayByCurProgress = replayByCurProgress;
    }

    public void setSpeed(String speed) {
        tv_speed.setText(speed);
    }

    private int curSpeedSelect = SPUtils.getInstance().getInt(KEY_SPEED_INDEX, 3);

    public void setSpeedSelect(String speed) {
        switch (speed) {
            case "0.5X":
                videoViewImpt.setVideoSpeed(0.50f);
                tv_speed.setText(getResources().getText(R.string.av_speed_1));
                curSpeedSelect = 5;
                break;
            case "0.75X":
                videoViewImpt.setVideoSpeed(0.75f);
                tv_speed.setText(getResources().getText(R.string.av_speed_2));
                curSpeedSelect = 4;
                break;
            case "1.0X":
                videoViewImpt.setVideoSpeed(1.00f);
                tv_speed.setText(getResources().getText(R.string.av_speed_3));
                curSpeedSelect = 3;
                break;
            case "1.25X":
                videoViewImpt.setVideoSpeed(1.25f);
                tv_speed.setText(getResources().getText(R.string.av_speed_4));
                curSpeedSelect = 2;
                break;
            case "1.5X":
                videoViewImpt.setVideoSpeed(1.50f);
                tv_speed.setText(getResources().getText(R.string.av_speed_5));
                curSpeedSelect = 1;
                break;
            case "2.0X":
                videoViewImpt.setVideoSpeed(2.00f);
                tv_speed.setText(getResources().getText(R.string.av_speed_6));
                curSpeedSelect = 0;
                break;
            default:
                break;
        }
        SPUtils.getInstance().put(KEY_SPEED_INDEX, curSpeedSelect);
    }

    //speed????????????
    private PopupWindow speedPop;
    private LinearLayout speedPopLayout;
    private TextView tv_speed_1, tv_speed_2, tv_speed_3, tv_speed_4, tv_speed_5, tv_speed_6;
    private TextView oldSpeedTv;
    private OnClickListener speedOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoViewImpt == null) return;
            int i = v.getId();
            if (i == R.id.tv_pop_speed_1) {
                videoViewImpt.setVideoSpeed(0.50f);
                tv_speed_1.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_1));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_1;
            } else if (i == R.id.tv_pop_speed_2) {
                videoViewImpt.setVideoSpeed(0.75f);
                tv_speed_2.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_2));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_2;
            } else if (i == R.id.tv_pop_speed_3) {
                videoViewImpt.setVideoSpeed(1.00f);
                tv_speed_3.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_3));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_3;
            } else if (i == R.id.tv_pop_speed_4) {
                videoViewImpt.setVideoSpeed(1.25f);
                tv_speed_4.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_4));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_4;
            } else if (i == R.id.tv_pop_speed_5) {
                videoViewImpt.setVideoSpeed(1.50f);
                tv_speed_5.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_5));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_5;
            } else if (i == R.id.tv_pop_speed_6) {
                videoViewImpt.setVideoSpeed(2.00f);
                tv_speed_6.setTextColor(getResources().getColor(R.color.player_theme_color));
                tv_speed.setText(getResources().getText(R.string.av_speed_6));
                if (oldSpeedTv != null) oldSpeedTv.setTextColor(Color.WHITE);
                oldSpeedTv = tv_speed_6;
            }
            speedPop.dismiss();
        }
    };

    private void selectSpeeed() {
        if (speedPop == null || speedPopLayout == null) {
            speedPopLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_speed, this, false);
            tv_speed_1 = speedPopLayout.findViewById(R.id.tv_pop_speed_1);
            tv_speed_1.setOnClickListener(speedOnClickListener);
            tv_speed_2 = speedPopLayout.findViewById(R.id.tv_pop_speed_2);
            tv_speed_2.setOnClickListener(speedOnClickListener);
            tv_speed_3 = speedPopLayout.findViewById(R.id.tv_pop_speed_3);
            tv_speed_3.setTextColor(getResources().getColor(R.color.player_theme_color));
            oldSpeedTv = tv_speed_3;
            tv_speed_3.setOnClickListener(speedOnClickListener);
            tv_speed_4 = speedPopLayout.findViewById(R.id.tv_pop_speed_4);
            tv_speed_4.setOnClickListener(speedOnClickListener);
            tv_speed_5 = speedPopLayout.findViewById(R.id.tv_pop_speed_5);
            tv_speed_5.setOnClickListener(speedOnClickListener);
            tv_speed_6 = speedPopLayout.findViewById(R.id.tv_pop_speed_6);
            tv_speed_6.setOnClickListener(speedOnClickListener);
            speedPop = new PopupWindow(speedPopLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            speedPop.setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
            speedPop.setOutsideTouchable(true);
            speedPop.setClippingEnabled(false);
        }
        speedPopLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        speedPop.showAsDropDown(tv_speed, -((speedPopLayout.getMeasuredWidth() - tv_speed.getMeasuredWidth()) / 2),
                -(speedPopLayout.getMeasuredHeight() + tv_speed.getMeasuredHeight()));
    }


    //?????????
    private PopupWindow hdPop;
    private LinearLayout hdPopLayout;
    private boolean isHdChange = true;
    private int currentIndex = 0;
    private List<String> hdList;
    private OnClickListener rateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoViewImpt == null) return;
            int index = (int) v.getTag();
            if (currentIndex == index) return;
            ((TextView) hdPopLayout.getChildAt(currentIndex)).setTextColor(Color.BLACK);
            ((TextView) hdPopLayout.getChildAt(index)).setTextColor(getResources().getColor(R.color.player_theme_color));
            tv_hd.setText(hdList.get(index));
            videoViewImpt.switchHd(hdList.get(index));
            hdPop.dismiss();
            currentIndex = index;
        }
    };

    private void selectHd() {
        if (videoViewImpt == null) return;
        if (hdPop == null || hdPopLayout == null || isHdChange) {
            isHdChange = false;
            hdPopLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pop_layout_hd, this, false);
            hdPop = new PopupWindow(hdPopLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            hdPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
            hdPop.setOutsideTouchable(true);
            hdPop.setClippingEnabled(false);
            hdList = new ArrayList<>();
            LinkedHashMap<String, String> definitionData = videoViewImpt.getHdData();
            if (definitionData == null) return;
            int i = 0;
            for (Map.Entry<String, String> entry : definitionData.entrySet()) {
                LogUtils.d("key:" + entry.getKey() + "   value:" + entry.getValue());
                hdList.add(entry.getKey());
                TextView rateItem = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.pop_item_layout_hd, hdPopLayout, false);
                rateItem.setText(entry.getKey());
                rateItem.setTag(i);
                if (i == currentIndex) {
                    rateItem.setTextColor(getResources().getColor(R.color.player_theme_color));
                }
                rateItem.setOnClickListener(rateOnClickListener);
                hdPopLayout.addView(rateItem);
                i++;
            }
        }
        hdPopLayout.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        hdPop.showAsDropDown(tv_hd, -((hdPopLayout.getMeasuredWidth() - tv_hd.getMeasuredWidth()) / 2),
                -(hdPopLayout.getMeasuredHeight() + tv_hd.getMeasuredHeight()));
    }

    /**
     * ????????????
     */
    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        tv_title.post(() -> {
            tv_title.setText(title);
            if (title.length() > 10)
                tv_title.startScroll();
        });
    }


    public void showJiexi() {
        tvJiexiMsg.setText("?????????????????????????????????");
        clpb_jiexi.post(new Runnable() {
            @Override
            public void run() {
                clpb_jiexi.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateJiexiProgess(String progress) {
        post(() -> tvJiexiMsg.setText(progress));

    }

    public void hideJiexi() {
        clpb_jiexi.post(new Runnable() {
            @Override
            public void run() {
                clpb_jiexi.setVisibility(View.GONE);
            }
        });
    }


    /**
     * ??????
     */
    public void onDestroy() {
        videoViewImpt = null;
        stopBufferTime();
    }

    private void stopBufferTime() {
        if (bufferTimer != null) {
            bufferTimer.cancel();
            bufferTimer = null;
        }
        if (bufferTask != null) {
            bufferTask.cancel();
            bufferTask = null;
        }
    }
}
