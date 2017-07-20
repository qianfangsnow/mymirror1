package com.mingrisoft.mymirror.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.mingrisoft.mymirror.R;

/**
 * Created by Administrator on 2017/7/13 0013.
 */

public class PictureView extends ImageView {
    private int[] bitmap_id_Array;
    private Canvas mCanvas;
    private int draw_Width;
    private int draw_Heihgt;
    private Bitmap mBitmap;
    private int bitmap_index;
    private void initBitmaps(){
        bitmap_id_Array=new int[]{
                R.mipmap.mag_0001,R.mipmap.mag_0003,R.mipmap.mag_0005,
                R.mipmap.mag_0006,R.mipmap.mag_0007,R.mipmap.mag_0008,
                R.mipmap.mag_0009,R.mipmap.mag_0011,R.mipmap.mag_0012,
                R.mipmap.mag_0014
        };
    }
    private void init(){
        initBitmaps();
        bitmap_index=0;
        mBitmap=Bitmap.createBitmap(draw_Width,draw_Heihgt,Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
    }
    public void setPhotoFrame(int index){
        bitmap_index=index;
        invalidate();
    }
    public int getPhotoFrame(){
        return bitmap_index;
    }
    private void getThewindowSize(Activity activity){
        DisplayMetrics dm=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        draw_Heihgt=dm.heightPixels;
        draw_Width=dm.widthPixels;
        Log.e("1、屏幕宽度",draw_Width+"\t\t屏幕高度:"+draw_Heihgt);

    }
    private Bitmap getNewBitmap(){
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),bitmap_id_Array[bitmap_index])
                .copy(Bitmap.Config.ARGB_8888,true);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBitmap,0,0,null);
        Rect rect1=new Rect(0,0,this.getWidth(),this.getHeight());
        canvas.drawBitmap(getNewBitmap(),null,rect1,null);
    }

    public PictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PictureView(Context context) {
        super(context);
    }

    public PictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getThewindowSize((Activity)context);
    }
}
