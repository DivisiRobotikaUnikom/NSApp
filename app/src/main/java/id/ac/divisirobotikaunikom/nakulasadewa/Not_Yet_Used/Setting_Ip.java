package id.ac.divisirobotikaunikom.nakulasadewa.Not_Yet_Used;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import id.ac.divisirobotikaunikom.nakulasadewa.Wifi.Komunikasi;
import id.ac.divisirobotikaunikom.nakulasadewa.R;

public class Setting_Ip extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN //fulscreen
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //tetap nyala
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.firstrun);

        final EditText editText = findViewById(R.id.ip);
        Button button = findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = editText.getText().toString();
                if(ip.isEmpty()){
                    return;
                }
                Komunikasi.ipServer = ip;

                SharedPreferences settings = getSharedPreferences("prefs", 0); //0 mode private
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("run", false);
                editor.putString("ip", ip);
                editor.commit();

                Intent i = new Intent("android.intent.action.MAINACTIVITY");
                startActivity(i);
                finish();
            }
        });
    }
}
