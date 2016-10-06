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
                    getRotationMatrixFromVector(R, event.values);
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

    /**
     * SensorManager.getRotationMatrixFromVector() is broken on some Samsung devices.
     * https://groups.google.com/forum/#!topic/android-developers/U3N9eL5BcJk
     *  @param rotationVector the rotation vector to convert
     *  @param R an array of floats in which to store the rotation matrix
     */
    private static void getRotationMatrixFromVector(float[] R, float[] rotationVector) {

        float q0;
        float q1 = rotationVector[0];
        float q2 = rotationVector[1];
        float q3 = rotationVector[2];

        if (rotationVector.length >= 4) {
            q0 = rotationVector[3];
        } else {
            q0 = 1 - q1*q1 - q2*q2 - q3*q3;
            q0 = (q0 > 0) ? (float)Math.sqrt(q0) : 0;
        }

        float sq_q1 = 2 * q1 * q1;
        float sq_q2 = 2 * q2 * q2;
        float sq_q3 = 2 * q3 * q3;
        float q1_q2 = 2 * q1 * q2;
        float q3_q0 = 2 * q3 * q0;
        float q1_q3 = 2 * q1 * q3;
        float q2_q0 = 2 * q2 * q0;
        float q2_q3 = 2 * q2 * q3;
        float q1_q0 = 2 * q1 * q0;

        if(R.length == 9) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;

            R[3] = q1_q2 + q3_q0;
            R[4] = 1 - sq_q1 - sq_q3;
            R[5] = q2_q3 - q1_q0;

            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[8] = 1 - sq_q1 - sq_q2;
        } else if (R.length == 16) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = 0.0f;

            R[4] = q1_q2 + q3_q0;
            R[5] = 1 - sq_q1 - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = 0.0f;

            R[8] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = 1 - sq_q1 - sq_q2;
            R[11] = 0.0f;

            R[12] = R[13] = R[14] = 0.0f;
            R[15] = 1.0f;
        }
    }
}
