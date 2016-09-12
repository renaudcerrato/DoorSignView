package com.mypopsy.sample.doorsignview;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;

/**
 * Created by renaud on 17/08/15.
 */
public class OrientationProvider {

    final private SensorManager sensorManager;
    final private Sensor sensor;
    final private Display display;

    final private float[] R = new float[9];
    final private float[] RR = new float[9];
    final private float[] YPR = new float[3];

    @Nullable
    private OnRotationChanged mListener;
    private boolean isDirty;

    public interface OnRotationChanged {
        void onOrientationChanged(OrientationProvider provider);
    }

    public OrientationProvider(Context context, Display display) {
        this((SensorManager) context.getSystemService(Context.SENSOR_SERVICE), display);
    }

    public OrientationProvider(SensorManager sensorManager, Display display) {
        this.display = display;
        this.sensorManager = sensorManager;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void register(@Nullable OnRotationChanged listener) {
        if((mListener != null) == (listener != null) || sensor == null)
            return;

        if(listener != null) {
            mListener = listener;
            sensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
        }else {
            sensorManager.unregisterListener(mSensorListener);
            mListener = null;
        }
    }

    public float[] rotationMatrix() {
        return RR;
    }

    public float yaw() {
        return getYPR()[0];
    }

    public float pitch() {
        return getYPR()[1];
    }

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

        @SuppressWarnings("SuspiciousNameCombination")
        @Override
        public void onSensorChanged(SensorEvent event) {

            int rotation = display.getRotation();
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

            if(mListener != null) mListener.onOrientationChanged(OrientationProvider.this);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // nothing to do?
        }
    };
}
