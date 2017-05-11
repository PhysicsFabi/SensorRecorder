package edu.uv.students.mobiledevices.sensorrecorder;

import android.app.FragmentManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowSensors extends AppCompatActivity {

    private ProcessingVisualization processingVisualization;


    public static final String EXTRA_KEY_SENSOR_SELECTOR = "Sensor";
    public static final int SENSOR_ACCELEROMETER = 0;
    public static final int SENSOR_GYROSCOPE = 1;
    public static final int SENSOR_MAGNETIC_FIELD = 2;

    private int selectedSensor;

    private TextView heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sensors);
        heading = (TextView) findViewById(R.id.show_senors_headingTV);
        selectedSensor = getIntent().getIntExtra(EXTRA_KEY_SENSOR_SELECTOR, 0);
        switch (selectedSensor) {
            case SENSOR_ACCELEROMETER:
                heading.setText(R.string.accelerometer);
                break;
            case SENSOR_GYROSCOPE:
                heading.setText(R.string.gyroscope);
                break;
            case SENSOR_MAGNETIC_FIELD:
                heading.setText(R.string.magnetic_field);
                break;
        }
        initProcessing();
    }

    private void initProcessing() {
        FragmentManager fragmentManager = getFragmentManager();
        processingVisualization = new ProcessingVisualization();
        processingVisualization.setSensor(selectedSensor);
        fragmentManager.beginTransaction()
                .replace(R.id.ProcessingContainer, processingVisualization)
                .commit();
    }
}
