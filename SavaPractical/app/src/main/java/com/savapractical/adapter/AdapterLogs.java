package com.savapractical.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.savapractical.R;

import java.util.ArrayList;

public class AdapterLogs extends RecyclerView.Adapter<AdapterLogs.MyViewHolder> {

    private Context context;
    private ArrayList<String> arrayList = new ArrayList<>();

    public AdapterLogs(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_logs, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_log.setText(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_log;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_log = itemView.findViewById(R.id.tv_log);
        }
    }
}
