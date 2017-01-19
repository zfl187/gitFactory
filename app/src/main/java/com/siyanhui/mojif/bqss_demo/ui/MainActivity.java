package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.siyanhui.mojif.bqss_demo.R;

public class MainActivity extends Activity implements View.OnClickListener {

    private RelativeLayout mQuaterBtn;
    private RelativeLayout mHalfBtn;
    private RelativeLayout mFullBtn;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mQuaterBtn = (RelativeLayout) findViewById(R.id.quater_search_btn);
        mHalfBtn = (RelativeLayout) findViewById(R.id.half_search_btn);
        mFullBtn = (RelativeLayout) findViewById(R.id.full_search_btn);
        mQuaterBtn.setOnClickListener(this);
        mHalfBtn.setOnClickListener(this);
        mFullBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quater_search_btn://进入联想搜索页面
                Intent goQuaterIntent = new Intent(mContext, QuaterSearchActivity.class);
                startActivity(goQuaterIntent);
                break;
            case R.id.half_search_btn://进入键盘搜索页面
                Intent goHalfIntent = new Intent(mContext, HalfSearchActivity.class);
                startActivity(goHalfIntent);
                break;
            case R.id.full_search_btn://进入全屏搜索页面
                Intent goFullIntent = new Intent(mContext, FullSearchActivity.class);
                startActivity(goFullIntent);
                break;
        }
    }
}
