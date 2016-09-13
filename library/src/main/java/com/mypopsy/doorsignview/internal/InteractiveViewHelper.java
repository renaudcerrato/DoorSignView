package com.mypopsy.doorsignview.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by Cerrato Renaud <renaud.cerrato@gmail.com>
 * https://github.com/renaudcerrato
 * 9/7/16
 */
public class InteractiveViewHelper {

    private final View mView;
    private final Callback mCallback;
    private final PowerManager mPowerManager;
    private BroadcastReceiver mReceiver;
    private IntentFilter mScreenIntentFilter;

    private boolean isVisible;
    private boolean isWindowVisible;
    private boolean isAttachedToWindow;

    private boolean isViewInteractive;

    public interface Callback {
        void onInteractivityChanged(boolean isInteractive);
    }

    public InteractiveViewHelper(@NonNull View view, @NonNull Callback callback) {
        mView = view;
        mCallback = callback;
        mPowerManager = (PowerManager) view.getContext().getSystemService(Context.POWER_SERVICE);
        isVisible = view.isShown();
        isWindowVisible = view.getWindowVisibility() == View.VISIBLE;
        isAttachedToWindow = ViewCompat.isAttachedToWindow(view);
        update(false);
    }

    public boolean isViewInteractive() {
        return isViewInteractive;
    }

    public void onAttachedToWindow() {
        isAttachedToWindow = true;
        update(true);
    }

    public void onDetachedFromWindow() {
        isAttachedToWindow = false;
        update(true);
    }

    @SuppressWarnings("UnusedParameters")
    public void onVisibilityChanged(@NonNull View changedView, int visibility) {
        isVisible = visibility == View.VISIBLE;
        update(true);
    }

    public void onWindowVisibilityChanged(int visibility) {
        isWindowVisible = visibility == View.VISIBLE;
        update(true);
    }

    private void update(boolean notify) {

        final boolean isViewVisible = isVisible && isAttachedToWindow && isWindowVisible;
        final boolean interactive = isViewVisible && isScreenInteractive();

        maybeRegisterScreenReceiver(isViewVisible);

        if(interactive != isViewInteractive) {
            isViewInteractive = interactive;

            if (notify) {
                mCallback.onInteractivityChanged(isViewInteractive);
            }
        }
    }

    private void maybeRegisterScreenReceiver(boolean register) {
        if(register) {
            if(mReceiver == null) {
                mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        update(true);
                    }
                };

                if(mScreenIntentFilter == null) {
                    mScreenIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                    mScreenIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                }

                mView.getContext().registerReceiver(mReceiver, mScreenIntentFilter);
            }
        }else if(mReceiver != null) {
            mView.getContext().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private boolean isScreenInteractive() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) return mPowerManager.isInteractive();
        return mPowerManager.isScreenOn();
    }
}
