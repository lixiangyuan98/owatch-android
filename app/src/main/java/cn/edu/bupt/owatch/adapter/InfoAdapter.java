package cn.edu.bupt.owatch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.edu.bupt.owatch.R;
import cn.edu.bupt.owatch.bean.ServerInfo;

public class InfoAdapter extends BaseAdapter {

    private List<ServerInfo> serverInfoList;
    private Context context;

    public InfoAdapter(Context context, List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
        this.context = context;
    }

    public void refresh(List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
        for (int i = 0; i < serverInfoList.size(); i++) {
            Log.i("adapter", "host=" + serverInfoList.get(i).getHost() + "status=" + serverInfoList.get(i).getStatus());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.info_list, null);

            holder.hostView = convertView.findViewById(R.id.remote_info_host);
            holder.statusView = convertView.findViewById(R.id.remote_info_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.hostView.setText(serverInfoList.get(position).getHost());
        holder.statusView.setText(serverInfoList.get(position).getStatus());
        return convertView;
    }

    @Override
    public int getCount() {
        return serverInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return serverInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

class ViewHolder {
    TextView hostView;
    TextView statusView;
}