package cn.edu.bupt.owatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.bupt.owatch.R;

public class FileAdapter extends BaseAdapter {

    private List<String> files;
    private Context context;

    class ViewHolder {
        TextView filenameView;
    }

    public FileAdapter(Context context, List<String> files) {
        this.files = files;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.files.size();
    }

    @Override
    public Object getItem(int position) {
        return this.files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.file_list, null);
            holder = new ViewHolder();

            holder.filenameView = convertView.findViewById(R.id.filename);
            convertView.setTag(convertView);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.filenameView.setText(files.get(position));
        return convertView;
    }
}