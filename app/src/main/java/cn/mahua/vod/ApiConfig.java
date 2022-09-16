package cn.mahua.vod;

public class ApiConfig {

    public static final String BASE_URL = "http://tv.bjwmsc.com/api.php/";
    public static final String getStart = "v1.main/startup";
    public static final String getTypeList = "v1.vod/types";
    public static final String getBannerList = "v1.vod";

    //专题
    public static final String getTopicList = "v1.topic/topicList";
    //专题详情
    public static final String getTopicDetail = "v1.topic/topicDetail";
    //游戏
    public static final String getGameList = "v1.youxi/index";
    //添加视频播放记录
    public static final String addPlayLog = "v1.user/addViewLog";
    //上报观影时长
    public static final String watchTimeLong = "v1.user/viewSeconds";
    //获取视频播放记录
    public static final String getPlayLogList = "v1.user/viewLog";
    //获取视频播放进度
    public static final String getVideoProgress = "v1.vod/videoProgress";
    //删除播放记录
    public static final String dleltePlayLogList = "v1.user/delVlog";
    //直播列表
    public static final String getLiveList = "v1.zhibo";
    //直播详情
    public static final String getLiveDetail = "v1.zhibo/detail";


    public static final String getTopList = "v1.vod";
    public static final String getCardList = "v1.main/category";
    public static final String getRecommendList = "v1.vod/vodPhbAll";
    public static final String getCardListByType = "v1.vod/type";
    public static final String getVodList = "v1.vod";
    public static final String getVod = "v1.vod/detail";

    public static final String COMMENT = "v1.comment";
    public static final String USER_INFO = "v1.user/detail";
    public static final String LOGIN = "v1.auth/login";
    public static final String LOGOUT = "v1.auth/logout";
    public static final String REGISTER = "v1.auth/register";
    public static final String VERIFY_CODE = "v1.auth/registerSms";
    public static final String OPEN_REGISTER = "v1.user/phoneReg";
    public static final String SIGN = "v1.sign";
    public static final String GROUP_CHAT = "v1.groupchat";
    public static final String CARD_BUY = "v1.user/buy";
    public static final String UPGRADE_GROUP = "v1.user/group";
    public static final String SCORE_LIST = "v1.user/groups";
    public static final String CHANGE_AGENTS = "v1.user/changeAgents";
    public static final String AGENTS_SCORE = "v1.user/agentsScore";
    public static final String POINT_PURCHASE = "v1.user/order";
    public static final String CHANGE_NICKNAME = "v1.user";
    public static final String CHANGE_AVATOR = "v1.upload/user";
    public static final String GOLD_WITHDRAW = "v1.user/goldWithdrawApply";
    public static final String PAY_TIP = "v1.user/payTip";
    public static final String GOLD_TIP = "v1.user/goldTip";
    public static final String FEEDBACK = "v1.gbook";
    public static final String COLLECTION_LIST = "v1.user/favs";
    public static final String COLLECTION = "v1.user/ulog";
    public static final String SHARE_SCORE = "v1.user/shareScore";
    public static final String TASK_LIST = "v1.user/task";
    public static final String MSG_LIST = "v1.message/index";
    public static final String MSG_DETAIL = "v1.message/detail";
    public static final String EXPAND_CENTER = "v1.user/userLevelConfig";
    public static final String MY_EXPAND = "v1.user/subUsers";
    public static final String SEND_DANMU = "v1.danmu";
    public static final String SCORE = "v1.vod/score";
    public static final String CHECK_VOD_TRYSEE = "v1.user/checkVodTrySee";
    public static final String BUY_VIDEO = "v1.user/buypopedom";
    public static final String CHECK_VERSION = "v1.main/version";
    public static final String PAY = "v1.user/pay";
    public static final String ORDER = "v1.user/order";
    public static final String APP_CONFIG = "v1.user/appConfig";
    public static final String SHARE_INFO = "v1.user/shareInfo";
    public static final String video_count = "v1.vod/videoViewRecode";
    public static final String tabFourInfo = "v1.youxi/index";
    public static final String tabThreeName = "v1.zhibo/thirdUiName";
    public static final String getRankList = "v1.vod/vodphb";
}
