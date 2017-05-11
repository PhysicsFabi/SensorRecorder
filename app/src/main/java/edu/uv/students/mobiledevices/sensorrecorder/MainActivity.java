package edu.uv.students.mobiledevices.sensorrecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String LOG_TAG = "SENSOR RECORDER";

    private Button showAccelerometerBt;
    private Button showGyroscopeBt;
    private Button showMagneticFieldBt;


    private Button showRecordingsBt;
    private Button recordBt;
    private TextView infoTV;

    private boolean isRecording;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor magneticSensor;

    private File directory;
    public static final String DIRECTORY_NAME = "SensorRecorder";

    static final int MY_PERMISSIONS_REQUEST_CODE = 1;

    private static final String COLUMN_SEPARATOR = ",";
    private static final String ACCELEROMETER_DATA_TAG = "ACCELEROMETER";
    private static final String GYROSCOPE_DATA_TAG = "GYROSCOPE";
    private static final String MAGNETIC_FIELD_DATA_TAG = "MAGNETIC_FIELD";


    private static class SensorDataPoint {
        long timeNs;
        float[] values;
    }

    ArrayList<SensorDataPoint> accelerometerData;
    ArrayList<SensorDataPoint> gyroscopeData;
    ArrayList<SensorDataPoint> magneticFieldData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayout();
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CODE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accelerometer!=null)
            registersSensorListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
        if(sensorManager!=null)
            sensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean arePermissionsGranted = true;
        if(requestCode==MY_PERMISSIONS_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                arePermissionsGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }
            if(arePermissionsGranted) {
                initAfterPermissionsGranted();
            } else {
                initError(R.string.error_permissions);
            }
        }
    }

    private void initAfterPermissionsGranted() {
        if(!initSensors()) {
            initError(R.string.error_missing_sensors);
            return;
        }
        if(!initDirectory()) {
            initError(R.string.error_file_system);
            //return;
        }
    }

    private void disableAllButtons() {
        showAccelerometerBt.setEnabled(false);
        showGyroscopeBt.setEnabled(false);
        showMagneticFieldBt.setEnabled(false);
        showRecordingsBt.setEnabled(false);
        showRecordingsBt.setEnabled(false);
        recordBt.setEnabled(false);
    }

    private void initError(int errorMessageId) {
        infoTV.setText(errorMessageId);
        disableAllButtons();
    }

    private boolean initDirectory() {
        directory = new File(Environment.getExternalStorageDirectory().toString() + "/" + DIRECTORY_NAME);
        return directory.isDirectory() || directory.mkdirs();
    }

    private boolean initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(!areAllRequiredSensorsPresent())
            return false;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registersSensorListener();
        return true;
    }

    private void registersSensorListener() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initLayout() {
        showAccelerometerBt = (Button) findViewById(R.id.main_showAccelerometerBt);
        showGyroscopeBt = (Button) findViewById(R.id.main_showGyroscopeBt);
        showMagneticFieldBt = (Button) findViewById(R.id.main_showMagneticFieldBt);
        showRecordingsBt = (Button) findViewById(R.id.main_showRecordingsBt);
        recordBt = (Button) findViewById(R.id.main_recordBt);
        infoTV = (TextView) findViewById(R.id.main_infoTV);

        showAccelerometerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowSensors.class);
                intent.putExtra(ShowSensors.EXTRA_KEY_SENSOR_SELECTOR, ShowSensors.SENSOR_ACCELEROMETER);
                startActivity(intent);
            }
        });

        showGyroscopeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowSensors.class);
                intent.putExtra(ShowSensors.EXTRA_KEY_SENSOR_SELECTOR, ShowSensors.SENSOR_GYROSCOPE);
                startActivity(intent);
            }
        });

        showMagneticFieldBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowSensors.class);
                intent.putExtra(ShowSensors.EXTRA_KEY_SENSOR_SELECTOR, ShowSensors.SENSOR_MAGNETIC_FIELD);
                startActivity(intent);
            }
        });

        showRecordingsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileList.class);
                startActivity(intent);
            }
        });

        recordBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecording();
            }
        });
    }

    private void toggleRecording() {
        if(isRecording)
            stopRecording();
        else
            startRecording();
    }

    private void stopRecording() {
        if(!isRecording)
            return;
        isRecording = false;
        writeRecordingToFile();
        enableAllButtonsAfterRecording();
        recordBt.setText(R.string.main_record);
        infoTV.setText("");
    }

    private void disableAllButtonsDuringRecording() {
        showAccelerometerBt.setEnabled(false);
        showGyroscopeBt.setEnabled(false);
        showMagneticFieldBt.setEnabled(false);
        showRecordingsBt.setEnabled(false);
    }

    private void enableAllButtonsAfterRecording() {
        showAccelerometerBt.setEnabled(true);
        showGyroscopeBt.setEnabled(true);
        showMagneticFieldBt.setEnabled(true);
        showRecordingsBt.setEnabled(true);
    }

    private void startRecording() {
        if(isRecording)
            return;
        isRecording = true;
        disableAllButtonsDuringRecording();
        recordBt.setText(R.string.main_stop);
        infoTV.setText(R.string.main_recording);
        resetRecording();
    }

    private void resetRecording() {
        accelerometerData = new ArrayList<>();
        gyroscopeData = new ArrayList<>();
        magneticFieldData = new ArrayList<>();
    }

    private void writeRecordingToFile() {
        try {
            @SuppressLint("SimpleDateFormat") String fileName = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            String fileAndPathName = directory.toString()+"/"+fileName+".txt";
            FileOutputStream os = new FileOutputStream(fileAndPathName);
            PrintWriter printWriter = new PrintWriter(os);
            Iterator<SensorDataPoint> accelerometerIterator = accelerometerData.iterator();
            Iterator<SensorDataPoint> gyroscopeIterator = gyroscopeData.iterator();
            Iterator<SensorDataPoint> magneticFieldIterator = magneticFieldData.iterator();

            SensorDataPoint nextSensorDataPoint;
            String nextDataTag = null;

            SensorDataPoint accelerometerDataPoint = accelerometerIterator.hasNext() ? accelerometerIterator.next() : null;
            SensorDataPoint gyroscopeDataPoint = gyroscopeIterator.hasNext() ? gyroscopeIterator.next() : null;
            SensorDataPoint magneticFieldDataPoint = magneticFieldIterator.hasNext() ? magneticFieldIterator.next() : null;
            SensorDataPoint[] sensorDataPoints = {accelerometerDataPoint, gyroscopeDataPoint, magneticFieldDataPoint};

            long startTimeNs = -1;

            while(accelerometerDataPoint!=null || gyroscopeDataPoint!=null || magneticFieldDataPoint!=null) {
                Arrays.sort(sensorDataPoints, new Comparator<SensorDataPoint>() {
                    @Override
                    public int compare(SensorDataPoint o1, SensorDataPoint o2) {
                        if(o1==null)
                            return 1;
                        if(o2==null)
                            return -1;
                        return (int)(o1.timeNs - o2.timeNs);
                    }
                });
                nextSensorDataPoint = sensorDataPoints[0];
                if(startTimeNs==-1)
                    startTimeNs = nextSensorDataPoint.timeNs;
                if(nextSensorDataPoint==accelerometerDataPoint) {
                    accelerometerDataPoint = accelerometerIterator.hasNext() ? accelerometerIterator.next() : null;
                    nextDataTag = ACCELEROMETER_DATA_TAG;
                } else if(nextSensorDataPoint==gyroscopeDataPoint) {
                    gyroscopeDataPoint = gyroscopeIterator.hasNext() ? gyroscopeIterator.next() : null;
                    nextDataTag = GYROSCOPE_DATA_TAG;
                } else if(nextSensorDataPoint==magneticFieldDataPoint) {
                    magneticFieldDataPoint = magneticFieldIterator.hasNext() ? magneticFieldIterator.next() : null;
                    nextDataTag = MAGNETIC_FIELD_DATA_TAG;
                }
                sensorDataPoints = new SensorDataPoint[]{accelerometerDataPoint, gyroscopeDataPoint, magneticFieldDataPoint};

                if(nextSensorDataPoint!=null) {
                    printWriter.println(
                        nextDataTag+COLUMN_SEPARATOR+
                        (nextSensorDataPoint.timeNs-startTimeNs)+COLUMN_SEPARATOR+
                        nextSensorDataPoint.values[0]+COLUMN_SEPARATOR+
                        nextSensorDataPoint.values[1]+COLUMN_SEPARATOR+
                        nextSensorDataPoint.values[2]);
                }
            }
            printWriter.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while writing the sensor data.", e);
        }
    }

    private boolean areAllRequiredSensorsPresent() {
        return
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                    && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
                    && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }

    @Override
    public void onSensorChanged(SensorEvent pEvent) {
        if(!isRecording)
            return;
        SensorDataPoint sensorDataPoint = new SensorDataPoint();
        sensorDataPoint.timeNs = pEvent.timestamp;
        sensorDataPoint.values = pEvent.values.clone();
        if(pEvent.sensor==accelerometer) {
            accelerometerData.add(sensorDataPoint);
        } else if(pEvent.sensor==gyroscope) {
            gyroscopeData.add(sensorDataPoint);
        } else if(pEvent.sensor==magneticSensor) {
            magneticFieldData.add(sensorDataPoint);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
