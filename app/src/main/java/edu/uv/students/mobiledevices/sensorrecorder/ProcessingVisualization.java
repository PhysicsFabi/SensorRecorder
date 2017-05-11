package edu.uv.students.mobiledevices.sensorrecorder;

import java.util.ArrayList;

import processing.core.PApplet;
import ketai.sensors.KetaiSensor;

/**
 * Created by Fabi on 09.05.2017.
 */


public class ProcessingVisualization extends PApplet {

    private int sensor = ShowSensors.SENSOR_ACCELEROMETER;

    private MovingGraph movingGraphX, movingGraphY, movingGraphZ;
    private int graphHeight;
    private KetaiSensor ketaiSensor;

    private static float[] maxAbsoluteYValue;
    static {
        maxAbsoluteYValue = new float[3];
        maxAbsoluteYValue[ShowSensors.SENSOR_ACCELEROMETER] = 20.0f;
        maxAbsoluteYValue[ShowSensors.SENSOR_GYROSCOPE] = 20.0f;
        maxAbsoluteYValue[ShowSensors.SENSOR_MAGNETIC_FIELD] = 60.0f;
    }

    public void setSensor(int sensor) {
        this.sensor = sensor;
    }

    public void settings() {
        fullScreen();
    }

    public void setup() {
        graphHeight = floor(height/3.0f);
        movingGraphX = new MovingGraph(0,0,width,graphHeight,5.0f*1000.0f,maxAbsoluteYValue[sensor]);
        movingGraphY = new MovingGraph(0,graphHeight,width,graphHeight,5.0f*1000.0f,maxAbsoluteYValue[sensor]);
        movingGraphZ = new MovingGraph(0,2*graphHeight,width,graphHeight,5.0f*1000.0f,maxAbsoluteYValue[sensor]);
        ketaiSensor = new KetaiSensor(this);
        ketaiSensor.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(ketaiSensor!=null)
            ketaiSensor.stop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(ketaiSensor!=null)
            ketaiSensor.start();
    }

    public void draw() {
        movingGraphX.draw();
        movingGraphY.draw();
        movingGraphZ.draw();
        textSize(floor(0.15f*graphHeight));
        fill(0xFF000000);
        textAlign(LEFT,TOP);
        text("x",0,0,width,graphHeight);
        text("y",0,graphHeight,width,graphHeight);
        text("z",0,2*graphHeight,width,graphHeight);
    }

    public void onAccelerometerEvent(float x, float y, float z) {
        if(sensor!=ShowSensors.SENSOR_ACCELEROMETER)
            return;
        movingGraphX.addValuePair(millis(),x);
        movingGraphY.addValuePair(millis(),y);
        movingGraphZ.addValuePair(millis(),z);
    }

    public void onGyroscopeEvent(float x, float y, float z) {
        if(sensor!=ShowSensors.SENSOR_GYROSCOPE)
            return;
        movingGraphX.addValuePair(millis(),x);
        movingGraphY.addValuePair(millis(),y);
        movingGraphZ.addValuePair(millis(),z);
    }

    public void onMagneticFieldEvent(float x, float y, float z) {
        if(sensor!=ShowSensors.SENSOR_MAGNETIC_FIELD)
            return;
        movingGraphX.addValuePair(millis(),x);
        movingGraphY.addValuePair(millis(),y);
        movingGraphZ.addValuePair(millis(),z);
    }

    class MovingGraph {
        private int x;
        private int y;
        private int w;
        private int h;
        private ArrayList<Float> xs;
        private ArrayList<Float> ys;
        private float scaleFactorX;
        private float scaleFactorY;
        private float h2;
        private float w2;

        MovingGraph(int pX, int pY, int pWidth, int pHeight, float pXAxisWidth, float pYAxisHeight) {
            x=pX;
            y=pY;
            w=pWidth;
            h=pHeight;
            h2=(float)h/2.0f;
            w2=(float)w/2.0f;
            xs = new ArrayList<>();
            ys = new ArrayList<>();
            scaleFactorX=(float)pWidth/pXAxisWidth;
            scaleFactorY=h2/pYAxisHeight;
        }

        void addValuePair(float pX, float pY) {
            xs.add(pX);
            ys.add(pY);
        }

        public void draw() {
            ArrayList<Float> xs_ = new ArrayList<>();
            ArrayList<Float> ys_ = new ArrayList<>();
            fill(0xFFFFFFFF);
            strokeWeight(0);
            rect(x,y,w,h);
            strokeWeight(floor(0.005f*height));
            float x0=x;
            float y0=h2+y;
            float x1,y1;
            for(int i=0;i<xs.size();++i) {
                x1=(xs.get(i)-millis())*scaleFactorX+w2+x;
                y1=ys.get(i)*scaleFactorY+h2+y;
                if(x1>0) {
                    line(x0, y0, x1, y1);
                    xs_.add(xs.get(i));
                    ys_.add(ys.get(i));
                }
                x0=x1;
                y0=y1;
            }
            xs=xs_;
            ys=ys_;
        }
    }
}