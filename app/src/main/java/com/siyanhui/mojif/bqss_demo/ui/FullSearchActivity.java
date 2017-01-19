package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.siyanhui.mojif.bqss_demo.Base64Img;
import com.siyanhui.mojif.bqss_demo.BqssConstants;
import com.siyanhui.mojif.bqss_demo.R;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssChatAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fantasy on 16/12/30.
 */

public class FullSearchActivity extends Activity {
    public static final int REQUESTCODE = 1000;
    private static final int REFRESH = 3;
    private TextView mTitleText;
    private LinearLayout backBtn;
    private ImageView mGoFullSearchBtn;
    private BqssChatAdapter mBqssChatAdapter;
    private ListView mListView;
    private List<String> mDatas;
    private MyHandler mHandler;

    private EditText mEditText;
    private Button mTextSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_search);
        mTitleText = (TextView) findViewById(R.id.title_text);
        mTitleText.setText("全屏搜索");
        mHandler = new MyHandler(this);
        backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mGoFullSearchBtn = (ImageView) findViewById(R.id.go_bqss_btn);
        mGoFullSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullSearchActivity.this, FullSearchHomePage.class);
                startActivityForResult(intent, REQUESTCODE);
            }
        });
        mListView = (ListView) findViewById(R.id.listview);
        mDatas = new ArrayList<>();
        mBqssChatAdapter = new BqssChatAdapter(mDatas);
        mListView.setAdapter(mBqssChatAdapter);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mTextSendBtn = (Button) findViewById(R.id.text_send_btn);
        mTextSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = BqssConstants.BQSS_TEXT_TAG + mEditText.getText().toString();
                if (TextUtils.isEmpty(mEditText.getText().toString().trim())) {
                    Toast.makeText(FullSearchActivity.this, "不能发送空消息", Toast.LENGTH_SHORT).show();
                } else {
                    mDatas.add(text);
                    mBqssChatAdapter.refresh(mDatas);
                    mEditText.setText("");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE && resultCode == FullSearchHomePage.RESULTCODE) {
            final String imgUrl = data.getStringExtra("stickerData");
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    String str = Base64Img.getImageStrFromUrl(imgUrl);
                    Message message = Message.obtain();
                    message.obj = str;
                    message.arg1 = REFRESH;
                    mHandler.sendMessage(message);
                }
            };
            thread.start();
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<FullSearchActivity> weakReference;

        MyHandler(FullSearchActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FullSearchActivity activity = weakReference.get();
            if (activity != null) {
                switch (msg.arg1) {
                    case REFRESH:
                        activity.mDatas.add((String) msg.obj);
                        activity.mBqssChatAdapter.refresh(activity.mDatas);
                        break;
                }
            }
        }
    }

}
