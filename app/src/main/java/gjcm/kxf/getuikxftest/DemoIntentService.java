package gjcm.kxf.getuikxftest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.FeedbackCmdMessage;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.igexin.sdk.message.SetTagCmdMessage;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class DemoIntentService extends GTIntentService {

    private static final String TAG = "GetuiSdkDemo";
    private SpeechSynthesizer mTts;
    private String path;
    private Context mycontext;

    /**
     * 为了观察透传数据变化.
     */
    private static int cnt;

    public DemoIntentService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTts = SpeechSynthesizer.createSynthesizer(getApplicationContext(), null);

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        Log.d(TAG, "onReceiveServicePid -> " + pid);
    }

    private String data;

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        Log.e("kxflog", "onReceiveMessageData:---------> " );
        mycontext = context;
        Log.d(TAG, "DemoIntentService    -> onReceiveMessageData");
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();
        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        Log.d(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));
        Log.d(TAG, "call PushManager ------------> ");

        Log.d(TAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
                + "\ncid = " + cid);

        if (payload == null) {
            Log.e(TAG, "receiver payload = null");
        } else {
            data = new String(payload);

            new Thread() {
                @Override
                public void run() {
                    mTts.setParameter(SpeechConstant.PARAMS, null);
                    mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                    mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
                    mTts.setParameter(SpeechConstant.SPEED, "50");//音速
                    mTts.setParameter(SpeechConstant.PITCH, "50");//设置合成音调
                    mTts.setParameter(SpeechConstant.VOLUME, "80");//合成音量
                    //设置播放器音频流类型
                    mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
                    // 设置播放合成音频打断音乐播放，默认为true
                    mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
                    mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
                    path = Environment.getExternalStorageDirectory() + "/woshi.wav";

                    mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, path);
                    int code = mTts.synthesizeToUri(data, path, mTtsListener);
                    Log.e("kxflog", "path: " + path+"code:"+code);
                    if (code != ErrorCode.SUCCESS) {
                        Log.e("kxflog", "语音合成失败,错误码: " + code);
                    }
                }

            }.start();
            // 测试消息为了观察数据变化
//            if (data.equals("收到一条透传测试消息")) {
//                data = data + "-" + cnt;
//                cnt++;
//            }
//            sendMessage(data, 0);
//            showNotifi();

            Log.d(TAG, "----------------------------------------------------------------------------------------------");
        }
    }

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.e("kxflog", "onSpeakBegin:-----");
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakResumed() {
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {
        }

        @Override
        public void onCompleted(SpeechError speechError) {
            Log.e("kxflog", "onCompleted:-----");
//            showNotifi();


            Intent showintent = new Intent(mycontext, ShowNotificationReceiver.class);
            showintent.putExtra("title", "新消息提醒");
            showintent.putExtra("msg", data);
            showintent.putExtra("sound", path);
            mycontext.sendBroadcast(showintent);
            mTts.stopSpeaking();
            mTts.destroy();
//            Uri urisound = Uri.parse(path);
//            Intent broadcastIntent = new Intent(mycontext, NotificationReceiver.class);
//            PendingIntent pendingIntent = PendingIntent.
//                    getBroadcast(mycontext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(mycontext);
//            builder.setContentTitle("新消息提醒")
//                    .setTicker(data)
//                    .setContentIntent(pendingIntent)
//                .setSound(urisound)
//                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                    .setSmallIcon(R.mipmap.applogo);
//
//            Log.i("kxflog", "showNotification："+data);
//            NotificationManager manager = (NotificationManager) mycontext.getSystemService(Context.NOTIFICATION_SERVICE);
//            manager.notify(2, builder.build());


        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
        }
    };

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);

        sendMessage(clientid, 1);
    }
    private void showNotifi(){
        Intent showintent = new Intent(mycontext, ShowNotificationReceiver.class);
        showintent.putExtra("title", "新消息提醒");
        showintent.putExtra("msg", data);
        showintent.putExtra("sound", path);
        mycontext.sendBroadcast(showintent);}

    /**
     * 检验是否在线
     *
     * @param context
     * @param online
     */
    @Override
    public void onReceiveOnlineState(Context context, boolean online) {

        Log.d(TAG, "onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        Log.d(TAG, "onReceiveCommandResult -> " + cmdMessage);
        int action = cmdMessage.getAction();
        if (action == PushConsts.SET_TAG_RESULT) {
            setTagResult((SetTagCmdMessage) cmdMessage);
        } else if ((action == PushConsts.THIRDPART_FEEDBACK)) {
            feedbackResult((FeedbackCmdMessage) cmdMessage);
        }
    }

//    private void showNotifi(Context context, String msg) {
//        Intent broadcastIntent = new Intent(context, NotificationReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.
//                getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//        builder.setContentTitle(msg)
//                .setTicker(msg)
//                .setContentIntent(pendingIntent)
////                .setSound(urisound)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setSmallIcon(R.mipmap.applogo);
//
//        Log.i("repeat", "showNotification");
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(2, builder.build());
//
//    }

    private void setTagResult(SetTagCmdMessage setTagCmdMsg) {
        String sn = setTagCmdMsg.getSn();
        String code = setTagCmdMsg.getCode();
        String text = "设置标签失败, 未知异常";
        switch (Integer.valueOf(code)) {
            case PushConsts.SETTAG_SUCCESS:
                text = "设置标签成功";
                break;

            case PushConsts.SETTAG_ERROR_COUNT:
                text = "设置标签失败, tag数量过大, 最大不能超过200个";
                break;

            case PushConsts.SETTAG_ERROR_FREQUENCY:
                text = "设置标签失败, 频率过快, 两次间隔应大于1s且一天只能成功调用一次";
                break;

            case PushConsts.SETTAG_ERROR_REPEAT:
                text = "设置标签失败, 标签重复";
                break;

            case PushConsts.SETTAG_ERROR_UNBIND:
                text = "设置标签失败, 服务未初始化成功";
                break;

            case PushConsts.SETTAG_ERROR_EXCEPTION:
                text = "设置标签失败, 未知异常";
                break;

            case PushConsts.SETTAG_ERROR_NULL:
                text = "设置标签失败, tag 为空";
                break;

            case PushConsts.SETTAG_NOTONLINE:
                text = "还未登陆成功";
                break;

            case PushConsts.SETTAG_IN_BLACKLIST:
                text = "该应用已经在黑名单中,请联系售后支持!";
                break;

            case PushConsts.SETTAG_NUM_EXCEED:
                text = "已存 tag 超过限制";
                break;

            default:
                break;
        }

        Log.d(TAG, "settag result sn = " + sn + ", code = " + code + ", text = " + text);
    }

    private void feedbackResult(FeedbackCmdMessage feedbackCmdMsg) {
        String appid = feedbackCmdMsg.getAppid();
        String taskid = feedbackCmdMsg.getTaskId();
        String actionid = feedbackCmdMsg.getActionId();
        String result = feedbackCmdMsg.getResult();
        long timestamp = feedbackCmdMsg.getTimeStamp();
        String cid = feedbackCmdMsg.getClientId();

        Log.d(TAG, "onReceiveCommandResult -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nactionid = " + actionid + "\nresult = " + result
                + "\ncid = " + cid + "\ntimestamp = " + timestamp);
    }

    private void sendMessage(String data, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = data;
        DemoApplication.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        Log.i("kxflog", "kxfservice-------onDestroy");
//        mTts.stopSpeaking();
//        mTts.destroy();
        super.onDestroy();
    }
}
