package com.mypopsy.doorsignview.internal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;

/**
 * Created by Cerrato Renaud <renaud.cerrato@gmail.com>
 * https://github.com/renaudcerrato
 * 17/08/15
 */
public class OrientationProvider {

    final private SensorManager mSensorManager;
    final private Display mDisplay;
    @Nullable final private Sensor mSensor;

    final private float[] R = new float[9];
    final private float[] RR = new float[9];
    final private float[] YPR = new float[3];

    private boolean isDirty;
    private boolean isStarted;

    final private OnRotationChanged mListener;

    public interface OnRotationChanged {
        void onOrientationChanged(OrientationProvider provider);
    }

    public OrientationProvider(@NonNull Context context, @NonNull Display display, @NonNull OnRotationChanged listener) {
        this((SensorManager) context.getSystemService(Context.SENSOR_SERVICE), display, listener);
    }

    public OrientationProvider(@NonNull SensorManager sensorManager, @NonNull Display display, @NonNull OnRotationChanged listener) {
        mDisplay = display;
        mSensorManager = sensorManager;
        mListener = listener;
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Start sampling.
     *
     * @param samplingPeriodUs The rate {@link android.hardware.SensorEvent sensor events} are
     *            delivered at. This is only a hint to the system. Events may be received faster or
     *            slower than the specified rate. Usually events are received faster. The value must
     *            be one of {@link android.hardware.SensorManager#SENSOR_DELAY_NORMAL},
     *            {@link android.hardware.SensorManager#SENSOR_DELAY_UI},
     *            {@link android.hardware.SensorManager#SENSOR_DELAY_GAME}, or
     *            {@link android.hardware.SensorManager#SENSOR_DELAY_FASTEST} or, the desired delay
     *            between events in microseconds. Specifying the delay in microseconds only works
     *            from Android 2.3 (API level 9) onwards. For earlier releases, you must use one of
     *            the {@code SENSOR_DELAY_*} constants.
     */
    public void start(int samplingPeriodUs) {
        if(!isStarted && mSensor != null) {
            mSensorManager.registerListener(mSensorListener, mSensor, samplingPeriodUs);
            isStarted = true;
        }
    }

    public void stop() {
        if(isStarted) {
            mSensorManager.unregisterListener(mSensorListener);
            isStarted = false;
        }
    }

    public float[] rotationMatrix() {
        return RR;
    }

    /**
     * Get the Azimuth/Yaw value.
     * @return the angle in radians
     */
    public float yaw() {
        return getYPR()[0];
    }

    /**
     * Get the pitch value.
     * @return the angle in radians
     */
    public float pitch() {
        return getYPR()[1];
    }

    /**
     * Get the roll value.
     * @return the angle in radians
     */
    public float roll() {
        return getYPR()[2];
    }

    private float[] getYPR() {
        if(isDirty) {
            SensorManager.getOrientation(RR, YPR);
            isDirty = false;
        }
        return YPR;
    }

    final private SensorEventListener mSensorListener  = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(!isStarted) {
                return;
            }

            int rotation = mDisplay.getRotation();
            int axisX, axisY;

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(R, event.values);
                    break;
                default:
                    return;
            }

            switch (rotation) {

                default:
                case Surface.ROTATION_0:
                    axisX = SensorManager.AXIS_X;
                    axisY = SensorManager.AXIS_Z;
                    break;

                case Surface.ROTATION_90:
                    axisX = SensorManager.AXIS_Z;
                    axisY = SensorManager.AXIS_MINUS_X;
                    break;

                case Surface.ROTATION_180:
                    axisX = SensorManager.AXIS_MINUS_X;
                    axisY = SensorManager.AXIS_MINUS_Z;
                    break;

                case Surface.ROTATION_270:
                    axisX = SensorManager.AXIS_MINUS_Z;
                    axisY = SensorManager.AXIS_X;
                    break;
            }

            SensorManager.remapCoordinateSystem(R, axisX, axisY, RR);
            isDirty = true;
            mListener.onOrientationChanged(OrientationProvider.this);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // nothing to do?
        }
    };
}
