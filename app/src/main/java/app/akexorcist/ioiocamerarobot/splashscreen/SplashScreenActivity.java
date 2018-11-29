package app.akexorcist.ioiocamerarobot.splashscreen;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.os.Handler;


import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.menu.MenuActivity;

public class SplashScreenActivity extends Activity  {
  int time = 2000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        }, time);

    }

}
