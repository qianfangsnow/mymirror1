package com.mingrisoft.mymirror.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mingrisoft.mymirror.R;

public class HintActivity extends AppCompatActivity {
    private TextView know;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hint);
        know=(TextView)findViewById(R.id.i_know);
        know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
