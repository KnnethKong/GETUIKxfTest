package gjcm.kxf.getuikxftest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.Tag;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * PushManager 为对外接口, 所有调用均是通过 PushManager.getInstance() 日志过滤器输入 PushManager, 在接口调用失败会有对应 Error 级别
 * log 提示.
 */
public class GetuiSdkDemoActivity extends Activity implements OnClickListener {

    private static final String TAG = "GetuiSdkDemo";
    private static final String MASTERSECRET = "hSVMd9cdrfAF2689SeiWq2";
    private Button clearBtn;
    private Button serviceBtn;
    private Button bindAliasBtn;
    private Button unbindAliasBtn;
    private Button btnAddTag;
    private Button btnVersion;
    private Button btnSilentime;
    private Button btnGetCid;
    private Button btnExit;
    private TextView appKeyView;
    private TextView appSecretView;
    private TextView masterSecretView;
    private TextView appIdView;

    public static TextView tView;
    public static TextView tLogView;

    // 透传测试.
    private Button transmissionBtn;

    // 通知测试.
    private Button notifactionBtn;

    // SDK服务是否启动.
    private boolean isServiceRunning = true;
    private Context context;

    private String appkey = "";
    private String appsecret = "";
    private String appid = "";

    private static final int REQUEST_PERMISSION = 0;

    // DemoPushService.class 自定义服务名称, 核心服务
    private Class userPushService = DemoPushService.class;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.e(TAG, "GetuiSdkDemoActivity   onCreate---------------------------->");

        context = this;
        isServiceRunning = true;

        DemoApplication.demoActivity = this;

        initView();
        parseManifests();

        appKeyView.setText(String.format("%s", getResources().getString(R.string.appkey) + appkey));
        appSecretView.setText(String.format("%s", getResources().getString(R.string.appsecret) + appsecret));
        masterSecretView.setText(String.format("%s", getResources().getString(R.string.mastersecret) + MASTERSECRET));
        appIdView.setText(String.format("%s", getResources().getString(R.string.appid) + appid));

        Log.d(TAG, "initializing sdk...");

        PackageManager pkgManager = getPackageManager();

        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        // read phone state用于获取 imei 设备信息
        boolean phoneSatePermission =
                pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
            requestPermission();
        } else {
            PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
        }

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), DemoIntentService.class);

        // 应用未启动, 个推 service已经被唤醒,显示该时间段内离线消息
        if (DemoApplication.payloadData != null) {
            tLogView.append(DemoApplication.payloadData);
        }

        // cpu 架构
        Log.d(TAG, "cpu arch = " + (Build.VERSION.SDK_INT < 21 ? Build.CPU_ABI : Build.SUPPORTED_ABIS[0]));///------

        // 检查 so 是否存在
        File file = new File(this.getApplicationInfo().nativeLibraryDir + File.separator + "libgetuiext2.so");
        Log.e(TAG, "libgetuiext2.so exist = " + file.exists());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "GetuiSdkDemoActivity   onRestart---------------------------->");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
            } else {
                Log.e(TAG, "We highly recommend that you need to grant the special permissions before initializing the SDK, otherwise some "
                        + "functions will not work");
                PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("GetuiSdkDemo", "onDestroy()");
        DemoApplication.payloadData.delete(0, DemoApplication.payloadData.length());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Log.d("GetuiSdkDemo", "onStop()");
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetuiSdkDemo Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://gjcm.kxf.getuikxftest/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    public void onClick(View v) {
        if (v == clearBtn) {
            tLogView.setText("");
            DemoApplication.payloadData.delete(0, DemoApplication.payloadData.length());
        } else if (v == serviceBtn) {
            if (isServiceRunning) {
                Log.d(TAG, "stopping sdk...");
                PushManager.getInstance().stopService(this.getApplicationContext());// 当前为运行状态，停止SDK服务
                tView.setText(getResources().getString(R.string.no_clientid));
                serviceBtn.setText(getResources().getString(R.string.start));
                isServiceRunning = false;
            } else {
                Log.d(TAG, "reinitializing sdk...");// 当前未运行状态，启动SDK服务
                PushManager.getInstance().initialize(this.getApplicationContext(), userPushService); // 重新初始化sdk
                serviceBtn.setText(getResources().getString(R.string.stop));
                isServiceRunning = true;
            }
        } else if (v == bindAliasBtn) {
            bindAlias();
            // PushManager.getInstance().turnOnPush(this.getApplicationContext());
        } else if (v == unbindAliasBtn) {
            unBindAlias();
            // PushManager.getInstance().turnOffPush(this.getApplicationContext());
        } else if (v == transmissionBtn) {
            showTransmission();
        } else if (v == notifactionBtn) {
            showNotification();
        } else if (v == btnAddTag) {
            addTag();
        } else if (v == btnGetCid) {
            getCid();
        } else if (v == btnSilentime) {
            setSilentime();
        } else if (v == btnVersion) {
            getVersion();
        } else if (v == btnExit) {
            finish();
        }
    }

    /**
     * 测试addTag接口.
     */
    private void addTag() {
        final View view = new EditText(this);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("设置Tag").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TextView tagText = (TextView) view;

                Log.d(TAG, "setTag input tags = " + tagText.getText().toString());

                String[] tags = tagText.getText().toString().split(",");
                Tag[] tagParam = new Tag[tags.length];
                for (int i = 0; i < tags.length; i++) {
                    Tag t = new Tag();
                    t.setName(tags[i]);
                    tagParam[i] = t;
                }

                int i = PushManager.getInstance().setTag(context, tagParam, "" + System.currentTimeMillis());
                String text = "设置标签失败, 未知异常";

                // 这里的返回结果仅仅是接口调用是否成功, 不是真正成功, 真正结果见{
                // com.getui.demo.DemoIntentService.setTagResult 方法}
                switch (i) {
                    case PushConsts.SETTAG_SUCCESS:
                        text = "接口调用成功";
                        break;

                    case PushConsts.SETTAG_ERROR_COUNT:
                        text = "接口调用失败, tag数量过大, 最大不能超过200个";
                        break;

                    case PushConsts.SETTAG_ERROR_FREQUENCY:
                        text = "接口调用失败, 频率过快, 两次间隔应大于1s";
                        break;

                    case PushConsts.SETTAG_ERROR_NULL:
                        text = "接口调用失败, tag 为空";
                        break;

                    default:
                        break;
                }

                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }).setView(view);
        alertBuilder.create().show();
    }

    private void getVersion() {
        String version = PushManager.getInstance().getVersion(this);
        Toast.makeText(this, "当前sdk版本 = " + version, Toast.LENGTH_SHORT).show();
    }

    private void getCid() {
        String cid = PushManager.getInstance().getClientid(this);
        Toast.makeText(this, "当前应用的cid = " + cid, Toast.LENGTH_LONG).show();
        Log.d(TAG, "当前应用的cid = " + cid);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        clearBtn = (Button) findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);
        serviceBtn = (Button) findViewById(R.id.btn_service);
        serviceBtn.setOnClickListener(this);
        bindAliasBtn = (Button) findViewById(R.id.btn_bind_alias);
        bindAliasBtn.setOnClickListener(this);
        unbindAliasBtn = (Button) findViewById(R.id.btn_unbind_alias);
        unbindAliasBtn.setOnClickListener(this);

        btnAddTag = (Button) findViewById(R.id.btnAddTag);
        btnAddTag.setOnClickListener(this);
        btnVersion = (Button) findViewById(R.id.btnVersion);
        btnVersion.setOnClickListener(this);
        btnSilentime = (Button) findViewById(R.id.btnSilentime);
        btnSilentime.setOnClickListener(this);
        btnGetCid = (Button) findViewById(R.id.btnGetCid);
        btnGetCid.setOnClickListener(this);
        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(this);

        tView = (TextView) findViewById(R.id.tvclientid);
        appKeyView = (TextView) findViewById(R.id.tvappkey);
        appSecretView = (TextView) findViewById(R.id.tvappsecret);
        masterSecretView = (TextView) findViewById(R.id.tvmastersecret);
        appIdView = (TextView) findViewById(R.id.tvappid);
        tLogView = (EditText) findViewById(R.id.tvlog);
        tLogView.setInputType(InputType.TYPE_NULL);
        tLogView.setSingleLine(false);
        tLogView.setHorizontallyScrolling(false);
        transmissionBtn = (Button) findViewById(R.id.btn_pmsg);
        transmissionBtn.setOnClickListener(this);
        notifactionBtn = (Button) findViewById(R.id.btn_psmsg);
        notifactionBtn.setOnClickListener(this);
    }

    private void parseManifests() {
        String packageName = getApplicationContext().getPackageName();
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                appid = appInfo.metaData.getString("PUSH_APPID");
                appsecret = appInfo.metaData.getString("PUSH_APPSECRET");
                appkey = appInfo.metaData.getString("PUSH_APPKEY");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断网络是否连接.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

    private void setSilentime() {
        final View view = LayoutInflater.from(this).inflate(R.layout.silent_setting, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("设置静默时间段").setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TextView beginText = (TextView) view.findViewById(R.id.beginText);
                TextView durationText = (TextView) view.findViewById(R.id.durationText);

                try {
                    int beginHour = Integer.valueOf(String.valueOf(beginText.getText()));
                    int durationHour = Integer.valueOf(String.valueOf(durationText.getText()));

                    boolean result = PushManager.getInstance().setSilentTime(context, beginHour, durationHour);

                    if (result) {
                        Toast.makeText(context, "begin = " + beginHour + ", duration = " + durationHour, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "setSilentime, begin = " + beginHour + ", duration = " + durationHour);
                    } else {
                        Toast.makeText(context, "setSilentime failed, value exceeding", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "setSilentime failed, value exceeding");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        }).setView(view);
        alertBuilder.create().show();
    }

    private void unBindAlias() {
        final EditText editText = new EditText(GetuiSdkDemoActivity.this);
        new AlertDialog.Builder(GetuiSdkDemoActivity.this).setTitle(R.string.unbind_alias).setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alias = editText.getEditableText().toString();
                        if (alias.length() > 0) {
                            PushManager.getInstance().unBindAlias(GetuiSdkDemoActivity.this, alias, false);
                            Log.d(TAG, "unbind alias = " + editText.getEditableText().toString());
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void bindAlias() {
        final EditText editText = new EditText(GetuiSdkDemoActivity.this);
        new AlertDialog.Builder(GetuiSdkDemoActivity.this).setTitle(R.string.bind_alias).setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getEditableText() != null) {
                            String alias = editText.getEditableText().toString();
                            if (alias.length() > 0) {
                                PushManager.getInstance().bindAlias(GetuiSdkDemoActivity.this, alias);
                                Log.d(TAG, "bind alias = " + editText.getEditableText().toString());
                            }
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void showNotification() {
        if (isNetworkConnected()) {
            // !!!!!!注意：以下为个推服务端API1.0接口，仅供测试。不推荐在现网系统使用1.0版服务端接口，请参考最新的个推服务端API接口文档，使用最新的2.0版接口
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("action", "pushSpecifyMessage"); // pushSpecifyMessage为接口名，注意大小写
            /*---以下代码用于设定接口相应参数---*/
            param.put("appkey", appkey);
            param.put("type", 2); // 推送类型： 2为消息
            param.put("pushTitle", "通知栏测试"); // pushTitle请填写您的应用名称

            // 推送消息类型，有TransmissionMsg、LinkMsg、NotifyMsg三种，此处以LinkMsg举例
            param.put("pushType", "LinkMsg");
            param.put("offline", true); // 是否进入离线消息
            param.put("offlineTime", 72); // 消息离线保留时间
            param.put("priority", 1); // 推送任务优先级

            List<String> cidList = new ArrayList<String>();
            cidList.add(tView.getText().toString()); // 您获取的ClientID
            param.put("tokenMD5List", cidList);
            param.put("sign", GetuiSdkHttpPost.makeSign(MASTERSECRET, param));// 生成Sign值，用于鉴权，需要MasterSecret，请务必填写

            // LinkMsg消息实体
            Map<String, Object> linkMsg = new HashMap<String, Object>();
            linkMsg.put("linkMsgIcon", "push.png"); // 消息在通知栏的图标
            linkMsg.put("linkMsgTitle", "通知栏测试"); // 推送消息的标题
            linkMsg.put("linkMsgContent", "您收到一条测试消息，点击访问www.igetui.com！"); // 推送消息的内容
            linkMsg.put("linkMsgUrl", "http://www.igetui.com/"); // 点击通知跳转的目标网页
            param.put("msg", linkMsg);
            GetuiSdkHttpPost.httpPost(param);

        } else {
            Toast.makeText(this, "对不起，当前网络不可用!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTransmission() {
        if (isNetworkConnected()) {
            // !!!!!!注意：以下为个推服务端API1.0接口，仅供测试。不推荐在现网系统使用1.0版服务端接口，请参考最新的个推服务端API接口文档，使用最新的2.0版接口
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("action", "pushmessage"); // pushmessage为接口名，注意全部小写
            /*---以下代码用于设定接口相应参数---*/
            param.put("appkey", appkey);
            param.put("appid", appid);
            // 注：透传内容后面需用来验证接口调用是否成功，假定填写为hello girl~
            param.put("data", "收到一条透传测试消息");
            // 当前请求时间，可选
            param.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(System.currentTimeMillis())));
            param.put("clientid", tView.getText().toString()); // 您获取的ClientID
            param.put("expire", 3600); // 消息超时时间，单位为秒，可选
            param.put("sign", GetuiSdkHttpPost.makeSign(MASTERSECRET, param));// 生成Sign值，用于鉴权

            GetuiSdkHttpPost.httpPost(param);
        } else {
            Toast.makeText(this, "对不起，当前网络不可用!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GetuiSdkDemo Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://gjcm.kxf.getuikxftest/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }
}
