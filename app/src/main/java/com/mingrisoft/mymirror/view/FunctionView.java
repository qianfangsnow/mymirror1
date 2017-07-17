package com.mingrisoft.mymirror.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.view.View;

import com.mingrisoft.mymirror.R;

/**
 * Created by Administrator on 2017/7/14 0014.
 */

public class FunctionView extends LinearLayout implements View.OnClickListener {
    private LayoutInflater mInflater;


    public FunctionView(Context context) {
        super(context);
    }

    public FunctionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mInflater=LayoutInflater.from(context);
        LayoutInflater mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=mInflater.inflate(R.layout.view_function, this,true);


    }

    public FunctionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
    private void init(){

    }

    @Override
    public void onClick(View view) {

    }
}
