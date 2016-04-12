package com.tougee.silence.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tougee.silence.model.WifiInfo;

import java.util.List;

public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.BindViewHolder> implements View.OnClickListener {

    private OnItemClickListener mListener;
    private List<WifiInfo> mBindList;

    public SimpleRecyclerAdapter(List<WifiInfo> bindList, OnItemClickListener listener) {
        mListener = listener;
        mBindList = bindList;
    }

    @Override
    public BindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        rootView.setOnClickListener(this);
        return new BindViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(BindViewHolder holder, int position) {
        String item = mBindList.get(position).mName;
        holder.textView.setText(item);
    }

    @Override
    public int getItemCount() {
        return mBindList.size();
    }

    public void addItem(WifiInfo bindInfo, int pos) {
        mBindList.add(bindInfo);
        notifyItemInserted(pos);
    }

    public void removeItem(int pos) {
        mBindList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void setBindList(List<WifiInfo> list) {
        mBindList = list;
    }

    public List<WifiInfo> getWifiList() {
        return mBindList;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(v);
        }
    }

    static class BindViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public BindViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View v);
    }
}
