package com.mypopsy.doorsignview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.mypopsy.doorsignview.internal.InteractiveViewHelper;
import com.mypopsy.doorsignview.internal.OrientationProvider;
import com.mypopsy.doorsignview.internal.ViewUtils;

import java.util.Locale;

/**
 * Created by Cerrato Renaud <renaud.cerrato@gmail.com>
 * https://github.com/renaudcerrato
 * 9/12/16
 */
public class AnimatedDoorSignView extends DoorSignView implements InteractiveViewHelper.Callback {

    private static final boolean DEBUG = false;
    private static final int DEFAULT_TENSION = 100;
    private static final int DEFAULT_FRICTION = 3;

    private final InteractiveViewHelper mInteractiveViewHelper = new InteractiveViewHelper(this, this);
    private float mTension, mFriction;
    private RotationController mController;
    private TextPaint mTextPaint;

    public AnimatedDoorSignView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedDoorSignView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedDoorSignView, defStyleAttr, 0);
        mTension = a.getFloat(R.styleable.AnimatedDoorSignView_adsv_tension, DEFAULT_TENSION);
        mFriction = a.getFloat(R.styleable.AnimatedDoorSignView_adsv_friction, DEFAULT_FRICTION);
        a.recycle();
    }

    public void setTension(float tension) {
        mTension = tension;
        if(mController != null) mController.setTension(tension);
    }

    public void setFriction(float friction) {
        mFriction = friction;
        if(mController != null) mController.setFriction(friction);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mInteractiveViewHelper.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mInteractiveViewHelper.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mInteractiveViewHelper.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mInteractiveViewHelper.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void onInteractivityChanged(boolean isInteractive) {
        getController().register(isInteractive);
    }

    private RotationController getController() {
        if(mController == null) mController = new RotationController(this, mTension, mFriction);
        return mController;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(DEBUG) {
            if(mTextPaint == null) {
                mTextPaint = new TextPaint();
                mTextPaint.setTextSize(ViewUtils.dpTopx(this, 14));
                mTextPaint.setColor(Color.BLACK);
            }
            canvas.drawText(String.format(Locale.ENGLISH, "y=%3.1f° p=%3.1f° r=%3.1f°",
                    Math.toDegrees(getController().sensor.yaw()),
                    Math.toDegrees(getController().sensor.pitch()),
                    Math.toDegrees(getController().sensor.roll())), 0, 0, mTextPaint);
        }
    }

    static private class RotationController extends SimpleSpringListener implements OrientationProvider.OnRotationChanged {

        private final SpringSystem springSystem;
        private final View view;
        private final OrientationProvider sensor;
        private double friction;
        private Spring spring;
        private SpringConfig config;

        RotationController(View view, double tension, double friction) {
            this.view = view;
            this.friction = friction;
            sensor = new OrientationProvider(view.getContext(), ViewUtils.getDisplay(view), this);
            springSystem = SpringSystem.create();
            config = new SpringConfig(tension, friction);
        }

        @Override
        public void onOrientationChanged(OrientationProvider provider) {
            double roll = Math.toDegrees(-provider.roll());
            double pitch = Math.abs(Math.cos(provider.pitch()));
            config.friction = Math.min(2000, pitch == 0 ? Double.MAX_VALUE : friction/(pitch*pitch));
            spring.setEndValue(getNearestAngle(spring.getEndValue(), roll));
        }

        void setFriction(double friction) {
            config.friction = this.friction = friction;
        }

        void setTension(double tension) {
            config.tension = tension;
        }

        @Override
        public void onSpringUpdate(Spring spring) {
            view.setRotation((float) spring.getCurrentValue());
        }

        public void register(boolean register) {
            if(register == (spring != null)) {
                return;
            }

            if(register) {
                spring = springSystem.createSpring();
                spring.setSpringConfig(config);
                spring.addListener(this);
                spring.setCurrentValue(view.getRotation());
                sensor.start(SensorManager.SENSOR_DELAY_UI);
            }else {
                sensor.stop();
                spring.destroy();
                spring = null;
            }
        }

        static private double getNearestAngle(double start, double end) {
            double diff = ((end+360)%360) - ((start+360)%360);
            while(Math.abs(diff) > 180) {
                if(diff < 0)
                    diff = (360 + diff) % 360;
                else
                    diff = (diff - 360);
            }
            return start+diff;
        }
    }
}
