package com.mingrisoft.mymirror.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View;

import com.mingrisoft.mymirror.R;

import static com.mingrisoft.mymirror.R.id.choose;
import static com.mingrisoft.mymirror.R.id.hint;
import static com.mingrisoft.mymirror.R.id.up;

/**
 * Created by Administrator on 2017/7/14 0014.
 */

public class FunctionView extends LinearLayout implements View.OnClickListener {
    private LayoutInflater mInflater;
    private ImageView hint,choose,down,up;
    public  static final int HINT_ID=R.id.hint;
    public  static final int CHOOSE_ID=R.id.choose;
    public  static final int DOWN_ID=R.id.light_down;
    public  static final int UP_ID=R.id.light_up;
    private  onFunctionViewItemClickListener listener;
    public interface onFunctionViewItemClickListener{
        void hint();
        void choose();
        void down();
        void up();
    }
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
        hint=(ImageView)findViewById(R.id.hint);
        choose=(ImageView)findViewById(R.id.choose);
        down=(ImageView)findViewById(R.id.light_down);
        up=(ImageView)findViewById(R.id.light_up);
        setView();
    }
    private void setView(){
        hint.setOnClickListener(this);
        choose.setOnClickListener(this);
        down.setOnClickListener(this);
        up.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(listener!=null){
           switch (view.getId()){
               case HINT_ID:
                   listener.hint();
                   break;
               case CHOOSE_ID:
                   listener.choose();
                   break;
               case DOWN_ID:
                   listener.down();
                   break;
               case UP_ID:
                   listener.up();
                   break;
               default:
                   break;
           }
        }

    }
    public void setOnFunctionViewItemClickListener(onFunctionViewItemClickListener
                                                           monFunctionViewItemClickListener){
        this.listener=monFunctionViewItemClickListener;
    }
}
