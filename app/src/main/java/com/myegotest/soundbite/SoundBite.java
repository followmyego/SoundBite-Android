package com.myegotest.soundbite;

/**
 * Created by Lucas on 11/03/2017.
 */
public class SoundBite {

    String soundBite_name;
    String OUTPUT_FILE;

    public SoundBite(String soundBite_name, String OUTPUT_FILE){
        this.soundBite_name = soundBite_name;
        this.OUTPUT_FILE = OUTPUT_FILE;
    }

    public String getSoundBite_name(){
        return soundBite_name;
    }

    public void setSoundBite_name(String soundBite_name){
        this.soundBite_name = soundBite_name;

    }

    public String getOUTPUT_FILE(){
        return OUTPUT_FILE;
    }

    public void setOUTPUT_FILE(String OUTPUT_FILE){
        this.OUTPUT_FILE = OUTPUT_FILE;
    }
}
