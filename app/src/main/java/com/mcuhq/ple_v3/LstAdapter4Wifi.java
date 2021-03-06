package com.mcuhq.ple_v3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LstAdapter4Wifi extends ArrayAdapter<String> {
    int groupid;
    String[] item_list;
    ArrayList<String> desc;
    Context context;
    public LstAdapter4Wifi(Context context, int vg, int id, String[] item_list){
        super(context,vg, id, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }
    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView textDbm;
        public TextView textMac;
        public TextView textCount;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // Inflate the rowlayout.xml file if convertView is null
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textDbm= (TextView) rowView.findViewById(R.id.tvDbm);
            viewHolder.textMac= (TextView) rowView.findViewById(R.id.tvMAC);
            viewHolder.textCount = (TextView) rowView.findViewById(R.id.tvCount);
            rowView.setTag(viewHolder);

        }
        // Set text to each TextView of ListView item
        String[] items=item_list[position].split("__");
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textDbm.setText(items[0]);
        holder.textMac.setText(items[1]);
        holder.textCount.setText(items[2]);
        return rowView;
    }

}
