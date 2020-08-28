package id.ac.divisirobotikaunikom.nakulasadewa.TTS;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TTS {

    private static TTS mInstance;
    private Context mContext;
    private TextToSpeech tts;

    public synchronized static TTS getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new TTS(c);
        }
        return mInstance;
    }

    public TTS(Context c) {
        mContext = c;
    }

    public TextToSpeech getTts() {
        if (tts == null) {
            tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(Locale.getDefault());
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(mContext, "TTS not supported", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Initilization Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return tts;
    }

    public void speak(String s) {
        if(!isSpeaking()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle map = new Bundle();
                map.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
                getTts().speak(s, TextToSpeech.QUEUE_FLUSH, map, "UniqueID");
            } else {
                getTts().speak(s, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public boolean isSpeaking(){
        return getTts().isSpeaking();
    }
}
