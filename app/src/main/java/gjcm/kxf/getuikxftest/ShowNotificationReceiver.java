package gjcm.kxf.getuikxftest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by kxf on 2016/12/3.
 */
public class ShowNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//动作为启动另外一个广播
        String title = intent.getExtras().getString("title");
        String msg = intent.getExtras().getString("msg");
        String sound = intent.getExtras().getString("sound");
        Uri urisound = Uri.parse(sound);
        Log.i("kxflog", title + "code" + "      edit_login:" + msg + "sound:" + sound);
        Intent broadcastIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(msg)
                .setTicker(msg)
                .setContentIntent(pendingIntent)
                .setSound(urisound)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.applogo);

        Log.i("repeat", "showNotification");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, builder.build());

    }
}
