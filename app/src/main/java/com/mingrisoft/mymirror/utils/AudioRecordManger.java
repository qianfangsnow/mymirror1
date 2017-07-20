package com.mingrisoft.mymirror.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Administrator on 2017/7/19 0019.
 */

public class AudioRecordManger {
    private static final String TAG="AudioRecord";
    public static final int SAMPLE_RATE_IN_HZ=8000;
    public static final int BUFFER_SIZE= AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;
    public boolean isGetVoiceRun;
    private Handler mHandler;
    private int mWhat;
    public Object mLock;
    public AudioRecordManger(Handler handler,int what){
        mLock=new Object();
        this.mHandler=handler;
        this.mWhat=what;
    }
    public void getNoiseLevel(){
        if(isGetVoiceRun){
            Log.e(TAG,"It's recording");
            return;
        }
        mAudioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT,BUFFER_SIZE);
        if(mAudioRecord==null){
            Log.e("sound","mAudioRecord initial failed");
        }
        isGetVoiceRun=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer=new short[BUFFER_SIZE];
                while (isGetVoiceRun){
                    int r=mAudioRecord.read(buffer,0,BUFFER_SIZE);
                    long v=0;
                    for(int i=0;i<buffer.length;i++){
                        v+=buffer[i]*buffer[i];
                    }
                    double mean=v/(double)r;
                    double volume=10*Math.log10(mean);
                    synchronized (mLock){
                        try{
                            mLock.wait(500);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                    Message message=Message.obtain();
                    message.what=mWhat;
                    message.obj=volume;
                    mHandler.sendMessage(message);
                }

            }
        }).start();
    }

}
