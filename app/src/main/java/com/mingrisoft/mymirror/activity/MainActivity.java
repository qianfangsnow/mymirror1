package com.mingrisoft.mymirror.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mingrisoft.mymirror.R;
import com.mingrisoft.mymirror.utils.AudioRecordManger;
import com.mingrisoft.mymirror.utils.SetBrightness;
import com.mingrisoft.mymirror.view.DrawView;
import com.mingrisoft.mymirror.view.FunctionView;
import com.mingrisoft.mymirror.view.PictureView;
import com.zys.brokenview.BrokenCallback;
import com.zys.brokenview.BrokenTouchListener;
import com.zys.brokenview.BrokenView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        SeekBar.OnSeekBarChangeListener
        ,View.OnClickListener,View.OnTouchListener,FunctionView.onFunctionViewItemClickListener,
        DrawView.OnCaYiCaCompletelistener{
    private static  final String TAG=MainActivity.class.getSimpleName();
    private static final int PHOTO = 1;
    private SurfaceHolder holder;
    private SurfaceView surfaceView;
    private PictureView pictureView;
    private FunctionView functionView;//onFunctionViewItemClickListener
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

    private  int frame_index;
    private int[] frame_index_ID;

    private  int brightnessValue;
    private boolean isAutoBrightness;
    private int SegmentLengh;

    private AudioRecordManger audioRecordManger;
    private static final int RECORD=2;

    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case RECORD:
                    double soundValues=(double)message.obj;
                    getSoundValues(soundValues);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private BrokenView brokenView;
    private boolean isBroken;

    private BrokenTouchListener brokenTouchListener;
    private MyBrokenCallback callback;
    private Paint brokenPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setViews();
        frame_index=0;
        frame_index_ID=new int[]{
                R.mipmap.mag_0001,R.mipmap.mag_0003,R.mipmap.mag_0005,
                R.mipmap.mag_0006,R.mipmap.mag_0007,R.mipmap.mag_0008,
                R.mipmap.mag_0009,R.mipmap.mag_0011,R.mipmap.mag_0012,
                R.mipmap.mag_0014
        };
        getBrightnessFromWindow();
        audioRecordManger=new AudioRecordManger(handler,RECORD);
        audioRecordManger.getNoiseLevel();
        mySimpleGestureListener=new MySimpleGestureListener();
        gestureDetector=new GestureDetector(this,mySimpleGestureListener);
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
        functionView.setOnFunctionViewItemClickListener(this);
        pictureView.setOnTouchListener(this);
        drawView.setOnCaYiCaCompleteListener(this);
        setToBrokenTheView();
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
                gestureDetector.onTouchEvent(motionEvent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void hint() {
        Intent intent=new Intent(this,HintActivity.class);
        startActivity(intent);
    }

    @Override
    public void choose() {
        Intent intent=new Intent(this,PhotoFrameActivity.class);
        startActivityForResult(intent,PHOTO);
        Toast.makeText(this,"选择",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void down() {
        downCurrentActivityBrightnessValues();
    }

    @Override
    public void up() {
        upCurrentActivityBrightnessValues();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG,"返回值"+resultCode+"\t\t请求值:"+requestCode);
        if(requestCode==RESULT_OK&&requestCode==PHOTO){
            int position=data.getIntExtra("position",0);
            frame_index=position;
            Log.e(TAG ,"返回的镜框类别"+position);
            pictureView.setPhotoFrame(position);
        }
    }
    private void setMyActivityBright(int brightnessValue){
        SetBrightness.setBrightness(this,brightnessValue);
        SetBrightness.saveBrightness(SetBrightness.getResolver(this),brightnessValue);
    }
    private void getAfterMySetBrightnessValues(){
        brightnessValue=SetBrightness.getScreenBrightness(this);
        Log.e(TAG,"当前手机屏幕亮度值"+brightnessValue);
    }
    public void getBrightnessFromWindow(){
        isAutoBrightness=SetBrightness.isAutoBrightness(SetBrightness.getResolver(this));
        Log.e(TAG,"this movingPhone is auto adjust screen light or not:"+isAutoBrightness);
        if(isAutoBrightness){
            SetBrightness.stopAutoBrightness(this);
            Log.e(TAG,"shut off the ableness of auto adjust");
            setMyActivityBright(255/2+1);
        }
        SegmentLengh=(255/2+1)/8;
        getAfterMySetBrightnessValues();
    }
    private void downCurrentActivityBrightnessValues(){
        if(brightnessValue>0){
            setMyActivityBright(brightnessValue-SegmentLengh);
        }
        getAfterMySetBrightnessValues();
    }
    private void upCurrentActivityBrightnessValues(){
        if(brightnessValue<255){
            if(brightnessValue+SegmentLengh>=256){
                return;
            }
            setMyActivityBright(brightnessValue+SegmentLengh);
        }
        getAfterMySetBrightnessValues();
    }

    private void hideView(){
        bottom.setVisibility(View.INVISIBLE);
        functionView.setVisibility(View.GONE);
    }
    private void showView(){
        pictureView.setImageBitmap(null);
        bottom.setVisibility(View.VISIBLE);
        functionView.setVisibility(View.VISIBLE);
    }
    private void getSoundValues(double valuses){
        if(valuses>50){
            hideView();
            drawView.setVisibility(View.VISIBLE);
            Animation animation= AnimationUtils.loadAnimation(this,R.anim.in_window);
            drawView.setAnimation(animation);
            audioRecordManger.isGetVoiceRun=false;
            Log.e( "玻璃显示","执行");
        }
    }

    @Override
    public void complete() {
        showView();
        audioRecordManger.getNoiseLevel();
        drawView.setVisibility(View.GONE);
    }

    class MyBrokenCallback extends BrokenCallback{
        @Override
        public void onStart(View v) {
            super.onStart(v);
            Log.e("Broken","onStart");
        }

        @Override
        public void onFalling(View v) {
            super.onFalling(v);
            Log.e("Broken","onFalling");
            //soundPool.play(sound.get(1),1,1,0,0,1)
        }

        @Override
        public void onFallingEnd(View v) {
            super.onFallingEnd(v);
            Log.e("Broken","onFallingEnd");
            brokenView.reset();
            pictureView.setOnTouchListener(MainActivity.this);
            pictureView.setVisibility(View.VISIBLE);
            isBroken=false;
            Log.e("isEnable","isbroken"+" ");
            brokenView.setEnable(isBroken);
            audioRecordManger.getNoiseLevel();
            showView();
        }

        @Override
        public void onCancelEnd(View v) {
            super.onCancelEnd(v);
            Log.e("Broken","onCancelEnd");
        }
    }

    private void setToBrokenTheView(){
        brokenPaint=new Paint();
        brokenPaint.setStrokeWidth(5);
        brokenPaint.setColor(Color.BLACK);
        brokenPaint.setAntiAlias(true);;
        brokenView=BrokenView.add2Window(this);
        brokenTouchListener=new BrokenTouchListener
                .Builder(brokenView)
                .setPaint(brokenPaint)
                .setBreakDuration(2000)
                .setFallDuration(5000)
                .build();
        brokenView.setEnable(true);
        callback=new MyBrokenCallback();
        brokenView.setCallback(callback);
    }

    private GestureDetector gestureDetector;
    private MySimpleGestureListener mySimpleGestureListener;

    class MySimpleGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            Log.e("手势","长按");
            isBroken=true;
            brokenView.setEnable(isBroken);
            pictureView.setOnTouchListener(brokenTouchListener);
            hideView();
            audioRecordManger.isGetVoiceRun=false;

        }
    }
}
