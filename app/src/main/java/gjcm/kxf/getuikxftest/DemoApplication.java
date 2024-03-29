package gjcm.kxf.getuikxftest;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class DemoApplication extends Application {

    private static final String TAG = "GetuiSdkDemo";

    private static DemoHandler handler;
    public static GetuiSdkDemoActivity demoActivity;

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    public static StringBuilder payloadData = new StringBuilder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DemoApplication onCreate");

        SpeechUtility.createUtility(this,  SpeechConstant.APPID +"=58426087," + SpeechConstant.FORCE_LOGIN +"=true");
        if (handler == null) {
            handler = new DemoHandler();
        }
    }

    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public static class DemoHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.i("kxflog","msg------->:"+msg.toString());

            switch (msg.what) {
                case 0:
                    if (demoActivity != null) {
                        payloadData.append((String) msg.obj);
                        payloadData.append("\n");
                        if (GetuiSdkDemoActivity.tLogView != null) {
                            GetuiSdkDemoActivity.tLogView.append(msg.obj + "\n");
                        }
                    }
                    break;

                case 1:
                    if (demoActivity != null) {
                        if (GetuiSdkDemoActivity.tLogView != null) {
                            GetuiSdkDemoActivity.tView.setText((String) msg.obj);
                        }
                    }
                    break;
            }
        }
    }
}
