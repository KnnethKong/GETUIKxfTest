package gjcm.kxf.getuikxftest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by kxf on 2016/12/3.
 */
public class OtherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("kxflog", "GetuiSdkDemoActivity   onCreate---------------------------->");

        Log.i("kxflog", "the app process is dead");
        TextView textView = new TextView(this);
        textView.setText("打开后的");
        setContentView(textView);
    }
}
