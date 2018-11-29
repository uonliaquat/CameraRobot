package app.akexorcist.ioiocamerarobot.controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.utils.JoyStickView;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class JoyStickManager implements View.OnTouchListener {
    private static final int JOYSTICK_COOLDOWN = 200;
    private JoyStickEventListener listener;
    private JoyStickView joystick;

    private long time = System.currentTimeMillis();

    @SuppressWarnings("deprecation")
    public JoyStickManager(Context context, ViewGroup layoutJoyStick, int screenHeight) {
        setupJoyStick(context, layoutJoyStick, screenHeight);
        layoutJoyStick.setOnTouchListener(this);
    }

    public void setJoyStickEventListener(JoyStickEventListener listener) {
        this.listener = listener;
    }

    private void setupJoyStick(Context context, ViewGroup layoutJoyStick, int screenHeight) {
        joystick = new JoyStickView(context, layoutJoyStick, R.drawable.image_button);
        joystick.setStickSize(screenHeight / 7, screenHeight / 7);
        joystick.setLayoutSize(screenHeight / 2, screenHeight / 2);
        joystick.setLayoutAlpha(100);
        joystick.setStickAlpha(255);
        joystick.setOffset((int) ((screenHeight / 9) * 0.6));
        joystick.setMinimumDistance((int) ((screenHeight / 9) * 0.6));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        joystick.drawStick(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getJoyStickDirection();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - time > JOYSTICK_COOLDOWN) {
                getJoyStickDirection();
                time = currentTimeMillis;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null)
                listener.onJoyStickNone();
        }
        return true;
    }

    //umar
    public void getJoyStickDirection() {
        int direction = joystick.get8Direction();
        float dist = joystick.getDistance();
        int speed = (int) (joystick.getDistance() / 1.875); //+ 20;
        speed = (speed > 100) ? 100 : speed;
        speed = (speed < 0) ? 0 : speed;

        int x,y;
        x = joystick.getX();
        y = joystick.getY();

        if (listener != null) {
            if (direction == JoyStickView.STICK_UP) {
                listener.onJoyStickUp(x,y);
            } else if (direction == JoyStickView.STICK_UPRIGHT) {
                listener.onJoyStickUpRight(x,y);
            } else if (direction == JoyStickView.STICK_RIGHT) {
                listener.onJoyStickRight(x,y);
            } else if (direction == JoyStickView.STICK_DOWNRIGHT) {
                listener.onJoyStickDownRight(x,y);
            } else if (direction == JoyStickView.STICK_DOWN) {
                listener.onJoyStickDown(x,y); //speed
            } else if (direction == JoyStickView.STICK_DOWNLEFT) {
                listener.onJoyStickDownLeft(x,y);
            } else if (direction == JoyStickView.STICK_LEFT) {
                listener.onJoyStickLeft(x,y);
            } else if (direction == JoyStickView.STICK_UPLEFT) {
                listener.onJoyStickUpLeft(x,y);
            } else if (direction == JoyStickView.STICK_NONE) {
                listener.onJoyStickNone();
            }
        }
    }

    public interface JoyStickEventListener {
        public void onJoyStickUp(int x, int y);
        public void onJoyStickUpRight(int x, int y);
        public void onJoyStickUpLeft(int x, int y);
        public void onJoyStickDown(int x, int y);
        public void onJoyStickDownRight(int x, int y);
        public void onJoyStickDownLeft(int x, int y);
        public void onJoyStickRight(int x, int y);
        public void onJoyStickLeft(int x, int y);
        public void onJoyStickNone();
    }
}
