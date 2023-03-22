package com.example.defineddatagt;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SensorService extends Service implements SensorEventListener {

    public static String TAG = MainActivity.TAG + "SensorService";
    private SensorManager sensorManagers;
    private SensorManager sensorManagerG;
    private SensorManager sensorManagerM;
    private SensorManager sensorManagerGy;
    private Sensor senAccelerometor;
    private Sensor senGyroscope;
    private Sensor senMagnetometer;
    private Sensor senGravity;
    public List<String> values;
    public Long startTime;
    public String mode;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        startTime = System.currentTimeMillis();
        Log.d(TAG, "onStartCommand at " + startTime);

        mode = intent.getStringExtra("mode");
        values = new ArrayList<>();

        sensorManagers = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensorManagerG = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensorManagerM = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        sensorManagerGy = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        senAccelerometor = sensorManagers.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = sensorManagerG.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senMagnetometer = sensorManagerM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senGravity = sensorManagerGy.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorManagers.registerListener(this, senAccelerometor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManagerG.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManagerM.registerListener(this, senMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManagerGy.registerListener(this, senGravity, SensorManager.SENSOR_DELAY_FASTEST);

        assert sensorManagers != null;

        return START_STICKY;
    }

    /*
      Called when there is a new sensor event. Note that "on changed" is somewhat of a misnomer,
      as this will also be called if we have a new reading from a sensor with the exact same
      sensor values (but a newer timestamp)
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        Sensor GSensor = sensorEvent.sensor;
        Sensor MSensor = sensorEvent.sensor;
        Sensor GySensor = sensorEvent.sensor;
        //Log.d(TAG, "inside onSensorChanged");

        Float gx, gy, gz, x, y, z, mx, my, mz, gyx, gyy, gyz;
        StringBuilder s = new StringBuilder();

        if (GSensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx = sensorEvent.values[0];
            gy = sensorEvent.values[1];
            gz = sensorEvent.values[2];
            s.append("gyroscope;");
            s.append(Float.toString(gx));
            s.append(";");
            s.append(Float.toString(gy));
            s.append(";");
            s.append(Float.toString(gz));
            s.append(";");
        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            s.append("accelerometer;");
            s.append(Float.toString(x));
            s.append(";");
            s.append(Float.toString(y));
            s.append(";");
            s.append(Float.toString(z));
            s.append(";");
        }

        if(MSensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mx = sensorEvent.values[0];
            my = sensorEvent.values[1];
            mz = sensorEvent.values[2];
            s.append("MagneticField;");
            s.append(Float.toString(mx));
            s.append(";");
            s.append(Float.toString(my));
            s.append(";");
            s.append(Float.toString(mz));
            s.append(";");
        }

        if(GySensor.getType() == Sensor.TYPE_GRAVITY) {
            gyx = sensorEvent.values[0];
            gyy = sensorEvent.values[1];
            gyz = sensorEvent.values[2];
            s.append("Gravity;");
            s.append(Float.toString(gyx));
            s.append(";");
            s.append(Float.toString(gyy));
            s.append(";");
            s.append(Float.toString(gyz));
            s.append(";");
        }

        long currentTime = System.currentTimeMillis();
        s.append(Long.toString(currentTime));
        //Log.d(TAG, String.valueOf(s));

        //store in list
        values.add(String.valueOf(s));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG + "values size", Integer.toString(values.size()));
        try {

            File file = new File(this.getFilesDir(), mode+"_"+Long.toString(startTime) + ".csv");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            for (int i = 0; i < values.size(); i++) {
                bw.write(String.valueOf(values.get(i)));
                bw.write("\n");
            }
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        sensorManagers.unregisterListener(this);
        sensorManagerG.unregisterListener(this);
        Toast.makeText(this, "Sensor Service destroyed by user.", Toast.LENGTH_LONG).show();
    }

}
