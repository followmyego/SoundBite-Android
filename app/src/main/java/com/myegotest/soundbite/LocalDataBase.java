package com.myegotest.soundbite;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by Lucas on 11/03/2017.
 */
public class LocalDataBase {

    //Declare the Shared Preference Variable
    public static SharedPreferences mLocalDatabase;

    //Name of this Shared Preference
    public final static String SP_NAME = "SoundBite List";



    //Call this constructor to access this local data base
    public LocalDataBase (Context context){
        mLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    //Method to store the arraylist fo soundites
    public void addSoundBiteList(){
        /*SharedPreferences.Editor spEditor = mLocalDatabase.edit();
        spEditor.putString(FACEBOOK_ID, user.facebookId);
        spEditor.commit();*/
    }

    //Method to get arraylist of soundbites
    public SoundBite getSoundBytes(){
        /*String facebookId = mLocalDatabase.getString(FACEBOOK_ID, "");
        return new User(facebookId, email, firstName, lastName, gender, birthday, user_status, using_facebook_pic, pageViews);*/
        return null;
    }

}
