package com.naganithin.accel_sgfiltered;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int on = 0;
    String data = "";
    int count = 0;
    int requestCodeP = 0;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.accerror, Toast.LENGTH_LONG).show();
            System.exit(1);
        }
        String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, requestCodeP);
        series = new LineGraphSeries<>();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestCodeP) {
            if(grantResults.length!=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) System.out.print("success");
            else ActivityCompat.requestPermissions(this, permissions, requestCodeP);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent == null) return;
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        TextView xtv = findViewById(R.id.xvl);
        TextView ytv = findViewById(R.id.yvl);
        TextView ztv = findViewById(R.id.zvl);
        TextView xnm = findViewById(R.id.xname);
        TextView ynm = findViewById(R.id.yname);
        TextView znm = findViewById(R.id.zname);
        xtv.setTextColor(Color.BLACK);
        ytv.setTextColor(Color.BLACK);
        ztv.setTextColor(Color.BLACK);
        xnm.setTextColor(Color.BLACK);
        ynm.setTextColor(Color.BLACK);
        znm.setTextColor(Color.BLACK);
        if (Math.abs(x)>Math.abs(y) && Math.abs(x)>Math.abs(y)) {
            xtv.setTextColor(Color.RED);
            xnm.setTextColor(Color.RED);
        }
        else if(Math.abs(y)>Math.abs(z)) {
            ytv.setTextColor(Color.RED);
            ynm.setTextColor(Color.RED);
        }
        else {
            ztv.setTextColor(Color.RED);
            znm.setTextColor(Color.RED);
        }
        xtv.setText(String.format(Locale.getDefault(), "%.9f", x));
        ytv.setText(String.format(Locale.getDefault(), "%.9f", y));
        ztv.setText(String.format(Locale.getDefault(), "%.9f", z));
        GraphView graph = findViewById(R.id.graph);
        series.appendData(new DataPoint(System.currentTimeMillis(), Math.sqrt(x*x + y*y + z*z)), true, 50000000);
        graph.clearAnimation();
        graph.addSeries(series);
        if (on==1) {
            data+=(String.format(Locale.getDefault(), "%.9f", x)+" "+String.format(Locale.getDefault(), "%.9f", y)+" "+String.format(Locale.getDefault(), "%.9f", z)+" "+System.currentTimeMillis()+"\n");
            count++;
            TextView saveM = findViewById(R.id.saveMessage);
            saveM.setText(String.format(Locale.getDefault(), getString(R.string.saving),count));
            if (count>=100000) start_stop(findViewById(R.id.button));
        }

    }

    public void start_stop(View v) {
        if (on==1) {
            File myDir = new File(Environment.getExternalStorageDirectory(), "acc_data/");
            String filename = "acc_data_"+System.currentTimeMillis()+".txt";
            try {
                boolean res = myDir.mkdirs();
                File file = new File(myDir, filename);
                res = res ^ file.createNewFile();
                System.out.print(res);
                PrintWriter out = new PrintWriter(file);
                out.write(data);
                out.flush();
                out.close();
                Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(),getString(R.string.fsvdat), myDir, filename), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.fail)+e, Toast.LENGTH_SHORT).show();
            }
            TextView saveM = findViewById(R.id.saveMessage);
            saveM.setText(getString(R.string.not_saving));
        }
        else {
            count = 0;
            TextView saveM = findViewById(R.id.saveMessage);
            saveM.setText(String.format(getString(R.string.saving),count));
            Toast.makeText(getApplicationContext(), R.string.started, Toast.LENGTH_SHORT).show();
            data = "";
        }
        on = 1 - on;
        System.out.print(v);
    }

}
