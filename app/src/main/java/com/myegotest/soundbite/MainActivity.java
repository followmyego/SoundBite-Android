package com.myegotest.soundbite;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Bitmap;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Track;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.example.audio_trimmer.CheapSoundFile;
import com.example.audio_trimmer.Util;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.MP3TrackImpl;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.myegotest.soundbite.SlidingMenu.AudioCompiler;
import com.myegotest.soundbite.SlidingMenu.SlidingMenuClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity  {

    ImageButton soundBiteButton;
    ImageButton doneRecordingButton;
    ImageButton stopRecordingButton;
    Button menuToggleButton;
    MediaPlayer player;
    ExtAudioRecorder recorder;
    String OUTPUT_FILE;
    ArrayList<SoundBite> soundBites = new ArrayList<SoundBite>();
    private SlidingMenu slidingMenu;
    SlidingMenuClass slidingMenuClass;
    private ListView listView;
    SoundBite_listAdapter listAdapter;


    long startTime;
    ArrayList<Long> timeStamps;
    boolean isRecording;
    RandomAccessFile raf;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /** Initialize view items **/
        initializeViewItems();

        /** Start recording **/
        startRecording();

        /** Set status bar color **/
        setStatusBarColor();

        /** Setup Toolbar and Sliding Menu */
        setupToolbarAndSlidingMenu();

        /** Setup listview and listadapter **/
        setupListViewAndAdapter();

        /** Setup menu toggle button **/
        setupMenuToggleButton();



    }

    /** add sound bite time stamp to arraylist */
    public void getBiteTimeStamp(){
        if(isRecording = true){
            //Get start time of recording
            long secs = (new Date().getTime())/1000;
            long timeStamp = secs - startTime;

            Toast.makeText(MainActivity.this, "" + timeStamp, Toast.LENGTH_SHORT).show();
            ArrayList<String> stamps = new ArrayList<String>();
            timeStamps.add(timeStamp);
            Toast.makeText(getApplicationContext(), "getBiteTimeStamp", Toast.LENGTH_SHORT).show();
        } else {
            startRecording();
        }

    }

    /** start listening **/
    private void startRecording() {

        soundBiteButton.setEnabled(false);



        Toast.makeText(getApplicationContext(), "recording", Toast.LENGTH_LONG).show();


        timeStamps = new ArrayList<>();

        //Get start time of recording
        startTime =(new Date().getTime())/1000;



        String fileName = (soundBites.size() + 1) + "";
        OUTPUT_FILE = Environment.getExternalStorageDirectory()+"/soundbite_" + fileName + ".3gpp";
        /*OUTPUT_FILE = Environment.getExternalStorageDirectory()+"/soundbite.3gpp";*/

        //start recording
        ditchMediaRecorder();
        File outFile = new File(OUTPUT_FILE);

        if(outFile.exists()){
            outFile.delete();
        }

        recorder = ExtAudioRecorder.getInstanse(true);
        recorder.setOutputFile(OUTPUT_FILE);
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        recorder.setOutputFile(OUTPUT_FILE);


        recorder.prepare();
        recorder.start();
        isRecording = true;

        /** wait 10 seconds of recording before alowing soundbites */
        new CountDownTimer(10000, 1000) {
            TextView readytext = (TextView) findViewById(R.id.ready_text);
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(readytext,
                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));


            public void onTick(long millisUntilFinished) {
                readytext.setText("Waiting: " + millisUntilFinished/1000);

            }

            public void onFinish() {


                soundBiteButton.setEnabled(true);
                scaleDown.end();
                readytext.setText("Ready");
                scaleDown.setDuration(310);

                scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
                scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

                scaleDown.start();
            }
        }.start();
    }

    /** stop recording and compile timestamps into a soundBite **/
    public void doneRecording() throws IOException {

        Toast.makeText(getApplicationContext(), "done recording", Toast.LENGTH_SHORT).show();
        //stop recording thats playing
        if(recorder != null){
            recorder.stop();
            isRecording = false;
            Toast.makeText(getApplicationContext(), "getting sound bites", Toast.LENGTH_SHORT).show();

            ArrayList<String> files = new ArrayList<String>();



            //Get 10 second clips from recording
            for(int i = 0; i < timeStamps.size(); i++){
                CheapSoundFile cheapSoundFile = CheapSoundFile.create(OUTPUT_FILE, new CheapSoundFile.ProgressListener() {
                    @Override
                    public boolean reportProgress(double fractionComplete) {
                        return true;
                    }
                });

                int mSampleRate = cheapSoundFile.getSampleRate();
                int mSamplesPerFrame = cheapSoundFile.getSamplesPerFrame();
                int startFrame = Util.secondsToFrames(timeStamps.get(i) - 10, mSampleRate, mSamplesPerFrame);
                int frame = Util.secondsToFrames(timeStamps.get(i), mSampleRate, mSamplesPerFrame);
                int endFrame = frame - startFrame;

                String tempOutputFile = Environment.getExternalStorageDirectory()+"/SSoftAudioFiles_" + i + ".wav";
                //Add file string to group of files strings
                File file = new File(tempOutputFile);
                cheapSoundFile.WriteFile(file, startFrame, endFrame);
//                ditchMediaPlayer();
                files.add(tempOutputFile);
                SoundBite soundBite = new SoundBite("SoundBite - " + soundBites.size(), tempOutputFile);
                soundBites.add(soundBite);
                Toast.makeText(getApplicationContext(), "done getting sound bites", Toast.LENGTH_SHORT).show();
            }

            /** This Section combines the soundbites together **/
            /*********************************************************************************/
            //compile soundbites together
//            for(int i=0; i < soundBites.size(); i++){
//                String file1 = soundBites.get(i).getOUTPUT_FILE();
//                String file2 = soundBites.get(i + 1).getOUTPUT_FILE();
//                SSoftAudCombine combiner = new SSoftAudCombine(file1, file2);
//                String newFile = combiner.getCombinedFile();
//                SoundBite soundBite = new SoundBite("SoundBite - " + soundBites.size(), newFile);
//                soundBites.add(soundBite);
//            }

            String file1 = soundBites.get(0).getOUTPUT_FILE();
            String file2 = soundBites.get(1).getOUTPUT_FILE();

            SSoftAudCombine combiner = new SSoftAudCombine(file1, file2, getApplicationContext());
            String newFile = combiner.getCombinedFile();
            raf = combiner.getRaf();

            Toast.makeText(MainActivity.this, file1, Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, file2, Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, newFile, Toast.LENGTH_SHORT).show();
            SoundBite soundBite = new SoundBite("SoundBite - " + soundBites.size(), newFile);
            soundBites.add(soundBite);

            /*********************************************************************************/

        
            // add compiled and completed soundbite to the list of soundbites **/
            listAdapter.setList(soundBites);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void CombineWaveFile(String file1, String file2) {
        FileInputStream in1 = null, in2 = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in1 = new FileInputStream(file1);
            in2 = new FileInputStream(file2);

            out = new FileOutputStream(getFilename3());

            totalAudioLen = in1.getChannel().size() + in2.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in1.read(data) != -1) {

                out.write(data);

            }
            while (in2.read(data) != -1) {

                out.write(data);
            }

            out.close();
            in1.close();
            in2.close();

            Toast.makeText(this, "Done!!", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte)(totalDataLen & 0xff);
        header[5] = (byte)((totalDataLen >> 8) & 0xff);
        header[6] = (byte)((totalDataLen >> 16) & 0xff);
        header[7] = (byte)((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte)(longSampleRate & 0xff);
        header[25] = (byte)((longSampleRate >> 8) & 0xff);
        header[26] = (byte)((longSampleRate >> 16) & 0xff);
        header[27] = (byte)((longSampleRate >> 24) & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)((byteRate >> 8) & 0xff);
        header[30] = (byte)((byteRate >> 16) & 0xff);
        header[31] = (byte)((byteRate >> 24) & 0xff);
        header[32] = (byte)(2 * 16 / 8);
        header[33] = 0;
        header[34] = RECORDER_BPP;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(totalAudioLen & 0xff);
        header[41] = (byte)((totalAudioLen >> 8) & 0xff);
        header[42] = (byte)((totalAudioLen >> 16) & 0xff);
        header[43] = (byte)((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }



    /** Stop recording, dont compile anything **/
    public void stopRecording(){
        Toast.makeText(getApplicationContext(), "stop recording", Toast.LENGTH_SHORT).show();
        if(recorder != null) {
            recorder.stop();
        }
        //stop the playback
        /*if(player != null){
            player.stop();
        }*/
    }

    /** play a saved soundBite **/
    public void playRecording() throws Exception{
        Toast.makeText(getApplicationContext(), "play recording", Toast.LENGTH_SHORT).show();
        //play the recording
        ditchMediaPlayer();
        player = new MediaPlayer();
        player.setDataSource(OUTPUT_FILE);
        player.prepare();
        player.start();
    }









    private void initializeViewItems() {
        soundBiteButton = (ImageButton) findViewById(R.id.soundBite);
        doneRecordingButton = (ImageButton) findViewById(R.id.done_recording);
        stopRecordingButton = (ImageButton) findViewById(R.id.stop_recording);

        //Sliding menu toggle button
        menuToggleButton = (Button) findViewById(R.id.menu_toggle_button);
    }

    private void setupMenuToggleButton() {
        // Read drawable from one of my stored images.
        Drawable dra = getResources().getDrawable(R.drawable.menu_icon_2x);
        Bitmap bitmapa = ((BitmapDrawable) dra).getBitmap();
        // Create a scaled drawable from bitmap
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmapa, 75, 75, true));
        // Set menu icon to new scaled down drawable
        getSupportActionBar().setHomeAsUpIndicator(d);

        setTitle(null);
    }

    private void setupListViewAndAdapter() {
        listView = (ListView) findViewById(R.id.soundBite_listView);
        listAdapter = new SoundBite_listAdapter(getApplicationContext(), soundBites);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ditchMediaPlayer();
                Toast.makeText(getApplicationContext(), soundBites.get(i).getOUTPUT_FILE(), Toast.LENGTH_LONG).show();
                player = new MediaPlayer();
                try {
                    player.setDataSource(soundBites.get(i).getOUTPUT_FILE());
//                    System.out.println("DEBUG: " + player.getPlaybackParams());
                    player.prepare();
                    Toast.makeText(getApplicationContext(), raf.readLine(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), raf.readUTF(), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                player.start();

            }
        });
    }

    private void setupToolbarAndSlidingMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Create Sliding menu
        slidingMenuClass = new SlidingMenuClass(MainActivity.this);
        slidingMenu = slidingMenuClass.slidingMenu;
        slidingMenu.attachToActivity(MainActivity.this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setFadeDegree(1f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setBehindOffsetRes(R.dimen.behindOffSetRes);
        slidingMenu.setMenu(R.layout.sliding_menu_frame);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor() {

        Window window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
    }

    public void buttonTapped(View view){
        switch(view.getId()){
            case R.id.soundBite:
                try{
                    getBiteTimeStamp();
                } catch(Exception E){
                    E.printStackTrace();
                }

                break;
            case R.id.done_recording:
                try{
                    doneRecording();
                } catch(Exception E){
                    E.printStackTrace();
                }
                break;
            case R.id.stop_recording:
                try{
                    stopRecording();
                } catch(Exception E){
                    E.printStackTrace();
                }
                break;



            case R.id.menu_toggle_button:
                slidingMenu.toggle();
                break;
        }
    }

    /** method to get date/time for use with unique file name**/
    private String getDate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    public void ditchMediaRecorder(){
        if(recorder != null){
            //ditch recorder
            recorder.release();
        }
    }

    public void ditchMediaPlayer(){
        if(player != null){
            //ditch recorder
            player.release();
        }
    }

    // ffmpeg command sample: "-y -i inputPath -ss 00:00:02 -codec copy -t 00:00:06 outputPath"
    public String getSplitCommand(String inputFileUrl, String outputFileUrl,
                                  String start, String end) {
        if ((TextUtils.isEmpty(inputFileUrl) && (TextUtils.isEmpty(outputFileUrl)))) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-y ")
                .append("-i ")
                .append(inputFileUrl).append(" ")
                .append("-ss ")
                .append(start).append(" ")
                .append("-codec ")
                .append("copy ")
                .append("-t ")
                .append(end).append(" ")
                .append(outputFileUrl);
        return stringBuilder.toString();
    }

    /*public void executeBinaryCommand(FFmpeg ffmpeg, ProgressDialog progressDialog, final String command) {
        if (!TextUtils.isEmpty(command)) {
            try {
                ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onProgress(String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStart() {
                        Toast.makeText(MainActivity.this, "start", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish() {
                        Toast.makeText(MainActivity.this, "finish", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException exception) {
                exception.printStackTrace();
            }
        }
    }*/













    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
