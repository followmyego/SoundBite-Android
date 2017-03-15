package com.myegotest.soundbite.SlidingMenu;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.myegotest.soundbite.R;
import com.myegotest.soundbite.SoundBite;
import com.myegotest.soundbite.SoundBite_listAdapter;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Lucas on 11/03/2017.
 */
public class SlidingMenuClass extends SlidingFragmentActivity {


    //View item variables
    public TextView version;
    ArrayList<SoundBite> arrayList = new ArrayList<SoundBite>();
    ListView listView;
    SoundBite_listAdapter listAdapter;


    //Other variables
    public SlidingMenu slidingMenu;


    public View view;

    public SlidingMenuClass(Context context) {
        slidingMenu = new SlidingMenu(context);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the Behind View
        setContentView(R.layout.sliding_menu_frame);


    }

    /*public void setSoundBiteList(ArrayList<SoundBite> list){
        arrayList = list;
        listAdapter.setList(arrayList);
        listAdapter.notifyDataSetChanged();
        listView.setAdapter(listAdapter);
    }*/

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        view = parent;
        TextView textView = (TextView) view.findViewById(R.id.yourBites);
        textView.setText("This worked.");





//        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_profile_page, container, false);
        return view;
    }
}
