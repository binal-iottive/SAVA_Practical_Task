package com.savapractical.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.savapractical.activity.GlobalApplication;
import com.savapractical.R;
import com.savapractical.listener.OnConnectClickListener;
import com.savapractical.model.DeviceModel;

import java.util.ArrayList;

public class AdapterDeviceList extends RecyclerView.Adapter<AdapterDeviceList.MyViewHolder> {

    private Context context;
    private ArrayList<DeviceModel> arrayList = new ArrayList<>();
    private OnConnectClickListener onConnectClickListener = null;

    public AdapterDeviceList(Context context, ArrayList<DeviceModel> arrayList, OnConnectClickListener onConnectClickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.onConnectClickListener = onConnectClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_device_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DeviceModel model = arrayList.get(position);
        holder.tv_macAddress.setText(model.deviceMacAddress);

        holder.tv_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConnectClickListener.onConnectClick(position);
                GlobalApplication.connectedDeviceMacAddress = arrayList.get(position).deviceMacAddress;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_macAddress;
        private TextView tv_connect;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_macAddress = itemView.findViewById(R.id.tv_macAddress);
            tv_connect = itemView.findViewById(R.id.tv_connect);
        }
    }
}
