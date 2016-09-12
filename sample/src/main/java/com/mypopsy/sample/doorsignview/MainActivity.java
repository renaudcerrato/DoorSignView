package com.mypopsy.sample.doorsignview;

import android.os.Debug;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private SeekBar mSeekBar;
    private View mDoorSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mDoorSign = findViewById(R.id.image);
        mSeekBar.setProgress(180 + (int) mDoorSign.getRotation());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isUser) {
                if(isUser) {
                    mDoorSign.setRotation(value - 180);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            if (Debug.isDebuggerConnected()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }
}
