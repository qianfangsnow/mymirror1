package com.mingrisoft.mymirror.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.mingrisoft.mymirror.R;
import com.mingrisoft.mymirror.view.DrawView;
import com.mingrisoft.mymirror.view.FunctionView;
import com.mingrisoft.mymirror.view.PictureView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,SeekBar.OnSeekBarChangeListener
        ,View.OnClickListener,View.OnTouchListener{
    private static  final String TAG=MainActivity.class.getSimpleName();
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private PictureView pictureView;
    private FunctionView functionView;
    private SeekBar seekBar;
    private ImageView add,minus;
    private LinearLayout bottom;
    private ImageView save;
    private ProgressDialog dialog;
    private DrawView drawView;


    private boolean havecamera;
    private int mCurrentCamIndex;
    private int ROTATE;
    private int minFocus;
    private int maxFocus;
    private int everyFocus;
    private int nowFocus;
    private Camera camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setViews();

    }
    private void initViews(){
        surfaceView=(SurfaceView)findViewById(R.id.surface);
        pictureView=(PictureView)findViewById(R.id.picture);
        functionView=(FunctionView)findViewById(R.id.function);
        seekBar=(SeekBar)findViewById(R.id.seekbar);
        add=(ImageView)findViewById(R.id.add);
        minus=(ImageView)findViewById(R.id.minus);
        bottom=(LinearLayout)findViewById(R.id.bottombar);
        drawView=(DrawView)findViewById(R.id.draw_glasses);
    }

    private boolean checkCameraHardware(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    private Camera openFrontFacingCameraGingerbread(){
        int cameraCount;
        Camera mCamera=null;
        Camera.CameraInfo cameraInfo=new Camera.CameraInfo();
        cameraCount=Camera.getNumberOfCameras();
        for(int camIdx=0;camIdx<cameraCount;camIdx++){
            Camera.getCameraInfo(camIdx,cameraInfo);
            if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
                try{
                    mCamera=Camera.open(camIdx);
                    mCurrentCamIndex=camIdx;
                }catch (RuntimeException e){
                    Log.e(TAG,"相机打开失败",e.getCause());
                }
            }
        }
        return mCamera;
    }

    private  void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera){
        Camera.CameraInfo info=new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,info);
        int rotation=activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees=0;
        switch(rotation){
            case Surface.ROTATION_0:
                degrees=0+180;
                break;
            case Surface.ROTATION_90:
                degrees=90+180;
                break;
            case Surface.ROTATION_180:
                degrees=180+180;
                break;
            case Surface.ROTATION_270:
                degrees=270+180;
                break;
        }
        int result=0;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result=(info.orientation+degrees)%360;
        }else{
            result=(info.orientation-degrees+360)%360;
        }
        ROTATE=result+180;
        camera.setDisplayOrientation(result);
    }

    private void setCamera(){
        if(checkCameraHardware()){
            camera=openFrontFacingCameraGingerbread();
            setCameraDisplayOrientation(this,mCurrentCamIndex,camera);
            Camera.Parameters parameters=camera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            List<String> list=parameters.getSupportedFocusModes();
            for(String str:list){
                Log.e(TAG,"支持的对焦的模式"+str);
            }
            List<Camera.Size> pictureList=parameters.getSupportedPictureSizes();
            List<Camera.Size> previewList=parameters.getSupportedPreviewSizes();
            parameters.setPictureSize(pictureList.get(0).width,pictureList.get(0).height);
            parameters.setPreviewSize(previewList.get(0).width,previewList.get(0).height);
            minFocus=parameters.getZoom();
            maxFocus=parameters.getMaxZoom();
            everyFocus=1;
            nowFocus=minFocus;
            seekBar.setMax(maxFocus);
            Log.e(TAG,"当前镜头焦距"+minFocus+"\t\t获取最大焦距"+maxFocus);
            camera.setParameters(parameters);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e("surfaceCreated","绘制开始");
        try{
            setCamera();
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (IOException e){
            camera.release();
            camera=null;
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e("surfaceChanged","绘图改变");
        try{
            camera.stopPreview();
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e("surfaceDestroyed","绘图结束");
        toRelease();
    }

    private void toRelease() {
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera=null;
    }

    private void setViews(){
        holder=surfaceView.getHolder();
        holder.addCallback(this);
        add.setOnTouchListener(this);
        minus.setOnTouchListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private void setZoomValues(int want){
        Camera.Parameters parameters=camera.getParameters();
        seekBar.setProgress(want);
        parameters.setZoom(want);
        camera.setParameters(parameters);
    }
    private int getZoomValues(){
        Camera.Parameters parameters=camera.getParameters();
        int values=parameters.getZoom();
        return values;
    }
    private void addZoomValues(){
        if(nowFocus>maxFocus){
            Log.e(TAG,"It's imposible that now focus is bigger than max focus");
        }else if(nowFocus==maxFocus ){
        }else {
            setZoomValues(getZoomValues()+everyFocus);
        }
    }
    private void minusZoomValues() {
        if (nowFocus < minFocus) {
            Log.e(TAG, "It's imposible that now focus is lesser than min focus");
        } else if (nowFocus == minFocus) {
        } else {
            setZoomValues(getZoomValues() - everyFocus);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        Camera.Parameters parameters=camera.getParameters();
        nowFocus=progress;
        parameters.setZoom(progress);
        camera.setParameters(parameters);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){
            case R.id.add:
                addZoomValues();
                break;
            case R.id.minus:
                minusZoomValues();
                break;
            case R.id.picture:

                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
