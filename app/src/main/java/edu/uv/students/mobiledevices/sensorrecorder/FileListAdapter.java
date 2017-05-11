package edu.uv.students.mobiledevices.sensorrecorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Fabi on 11.05.2017.
 */

class FileListAdapter extends BaseAdapter {
    private final ArrayList<File> files;
    private final Context context;

    static class ViewHolder {
        TextView fileNameTV;
        ImageButton deleteBt;
        ImageButton shareBt;
    }

    public FileListAdapter(Context pContext, File[] pFiles) {
        context = pContext;
        files = new ArrayList<>(Arrays.asList(pFiles));
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).toString().hashCode();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            v = View.inflate(context, R.layout.table_row_file, null);
            holder = new ViewHolder();
            holder.fileNameTV = (TextView) v.findViewById(R.id.file_list_file_nameTV);
            holder.deleteBt = (ImageButton) v.findViewById(R.id.file_list_deleteBT);
            holder.shareBt = (ImageButton) v.findViewById(R.id.file_list_shareBT);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final File file = files.get(position);
        holder.fileNameTV.setText(file.getName());
        holder.deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                file.delete();
                files.remove(position);
                FileListAdapter.this.notifyDataSetChanged();
            }
        });
        holder.shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);

                if(file.exists()) {
                    intentShareFile.setType("text/plain");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
                    context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
                }
            }
        });

        return v;
    }
}
