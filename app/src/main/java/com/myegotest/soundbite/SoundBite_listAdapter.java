package com.myegotest.soundbite;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lucas on 11/03/2017.
 */
public class SoundBite_listAdapter extends BaseAdapter {

    Context context;
    ArrayList<SoundBite> arrayList = new ArrayList<SoundBite>();
    LayoutInflater mInflater;

    public SoundBite_listAdapter(Context context, ArrayList<SoundBite> arrayList){
        this.arrayList = arrayList;
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    public void setList(ArrayList<SoundBite> list){
        arrayList = list;
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        /*Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ChaletNewYorkNineteenEighty.ttf");*/

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.soundbite_listitem, null);
            viewHolder.soundBiteName = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.soundBiteName.setText(arrayList.get(position).getSoundBite_name());




        return convertView;
    }

    public class ViewHolder {
        TextView soundBiteName;
    }
}
