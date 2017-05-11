package edu.uv.students.mobiledevices.sensorrecorder;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.io.File;

public class FileList extends AppCompatActivity {

    FileListAdapter fileListAdapter;
    ListView fileListLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        fileListLV = (ListView) findViewById(R.id.file_list_file_listLV);
        File[] files = new File(Environment.getExternalStorageDirectory().toString()+"/"+MainActivity.DIRECTORY_NAME).listFiles();
        fileListAdapter = new FileListAdapter(this, files);
        fileListLV.setAdapter(fileListAdapter);
    }
}
