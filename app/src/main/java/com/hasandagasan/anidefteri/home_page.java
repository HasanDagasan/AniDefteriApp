package com.hasandagasan.anidefteri;

import  androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
public class home_page extends AppCompatActivity {
    private int index = 0;
    private  TextView textView;
    private final long delay = 100;
    private final Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        textView = findViewById(R.id.textView10);
        Button basla = findViewById(R.id.baslabutton);
        mediaPlayer = MediaPlayer.create(this, R.raw.changepage);

        startTypingAnimation();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean sesAcik = prefs.getBoolean("sesAcik", true);

        basla.setOnClickListener(view -> {
            if (sesAcik && mediaPlayer != null) {
                mediaPlayer.start();
            }

            Intent intent = new Intent(home_page.this, MainActivity.class);
            intent.putExtra("openFragment", "ekle");
            startActivity(intent);

            finish();
        });
    }
    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {

            String fullText = "Hoşgeldin. Bugün hangi anını kaydetmek istersin?";
            textView.setText(fullText.subSequence(0, index++));
            if (index <= fullText.length()) {
                handler.postDelayed(this, delay);
            }
        }
    };
    private void startTypingAnimation() {
        index = 0;
        textView.setText("");
        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, delay);
    }
}