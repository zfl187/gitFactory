package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.siyanhui.mojif.bqss_demo.R;
import com.siyanhui.mojif.bqss_demo.api.HotTag;
import com.siyanhui.mojif.bqss_demo.api.IOpenApiCallback;
import com.siyanhui.mojif.bqss_demo.api.OpenApiResponseObject;
import com.siyanhui.mojif.bqss_demo.api.SearchSticksApi;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssWordWrapView;
import com.siyanhui.mojif.bqss_demo.utils.BqssPreferenceHelper;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fantasy on 17/1/4.
 */

public class FullSearchHomePage extends Activity {
    public static int REQUESTCODE = 1002;
    public static int RESULTCODE = 1001;
    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private GridView mGridView;
    private MyHandler myHandler;
    private EditText mEditText;
    private BqssWordWrapView bqssWordWrapView;
    private LinearLayout searchHistoryView;
    private TextView clearSearchHistoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fs_home_page);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        TextView backText = (TextView) findViewById(R.id.back_text);
        titleText.setText("全屏搜索");
        backText.setText("返回");
        searchHistoryView = (LinearLayout) findViewById(R.id.search_history_view);
        bqssWordWrapView = (BqssWordWrapView) findViewById(R.id.word_wrap_view);
        mEditText = (EditText) findViewById(R.id.bqss_editview);
        LinearLayout backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mGridView = (GridView) findViewById(R.id.home_gridview);
        mGridView.setNumColumns(3);
        myHandler = new MyHandler(this);
        SearchSticksApi.getHomePageStickers(new IOpenApiCallback<HotTag>() {
            @Override
            public void onSuccess(OpenApiResponseObject<HotTag> result) {
                List<HotTag> hotTagList = result.getEmojis();
                for (int i = 0; i < hotTagList.size(); i++) {
                    Message message = Message.obtain();
                    message.arg1 = SUCCESS;
                    message.obj = hotTagList;
                    myHandler.sendMessage(message);
                }

            }

            @Override
            public void onError(String errorInfo) {

            }
        });
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showSearchHistory();
                searchHistoryView.setVisibility(View.VISIBLE);
                return false;
            }
        });

        clearSearchHistoryBtn = (TextView) findViewById(R.id.clear_history_btn);
        clearSearchHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BqssPreferenceHelper.clearSearchHistory();
                bqssWordWrapView.removeAllViews();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchHistoryView.setVisibility(View.GONE);
        showSearchHistory();
    }

    private void showSearchHistory() {
        bqssWordWrapView.removeAllViews();
        HashSet<String> stringHashSet = (HashSet<String>) BqssPreferenceHelper.getSearchHistory();
        Iterator<String> iterator = stringHashSet.iterator();
        while (iterator.hasNext()) {
            final String keyword = iterator.next();
            TextView textview = new TextView(this);
            textview.setText(keyword);
            textview.setTextColor(getResources().getColor(R.color.gray_97));
            textview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FullSearchHomePage.this, FullSearchResultPage.class);
                    intent.putExtra("keyword", keyword);
                    startActivityForResult(intent, REQUESTCODE);
                }
            });
            bqssWordWrapView.addView(textview);
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<FullSearchHomePage> weakReference;

        public MyHandler(FullSearchHomePage activity) {
            this.weakReference = new WeakReference<FullSearchHomePage>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FullSearchHomePage activity = weakReference.get();
            if (activity != null) {
                switch (msg.arg1) {
                    case SUCCESS:
                        List<HotTag> hotTagList = (List<HotTag>) msg.obj;
                        GridAdapter gridAdapter = new GridAdapter(activity, hotTagList);
                        activity.mGridView.setAdapter(gridAdapter);
                        break;
                    case ERROR:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }

            }

        }
    }

    private static class GridAdapter extends BaseAdapter {
        private List<HotTag> hotTagList;
        private Activity activity;

        public GridAdapter(Activity activity, List<HotTag> hotTags) {
            this.activity = activity;
            this.hotTagList = hotTags;
        }

        @Override
        public int getCount() {
            if (hotTagList != null)
                return hotTagList.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.hot_tag_item, null);
                viewHolder = new ViewHolder();
                viewHolder.simpleDraweeView = (SimpleDraweeView) convertView.findViewById(R.id.hot_tag_img);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.hot_tag_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final HotTag hotTag = hotTagList.get(position);
            viewHolder.textView.setText(hotTag.getText());
            Uri uri = Uri.parse(hotTag.getMain());
            DraweeController draweeController = Fresco.newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build();
            viewHolder.simpleDraweeView.setController(draweeController);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BqssPreferenceHelper.addSearchKeyword(hotTag.getText());
                    Intent intent = new Intent(parent.getContext(), FullSearchResultPage.class);
                    intent.putExtra("keyword", hotTag.getText());
                    activity.startActivityForResult(intent, REQUESTCODE);
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        private SimpleDraweeView simpleDraweeView;
        private TextView textView;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(FullSearchHomePage.this.getCurrentFocus().getWindowToken(), 0);
            }
            String keyword = mEditText.getText().toString();
            if (!TextUtils.isEmpty(keyword)) {
                BqssPreferenceHelper.addSearchKeyword(keyword);
                Intent intent = new Intent(FullSearchHomePage.this, FullSearchResultPage.class);
                intent.putExtra("keyword", keyword);
                startActivityForResult(intent, REQUESTCODE);
            } else {
                Toast.makeText(FullSearchHomePage.this, "请输入关键词", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE && resultCode == FullSearchResultPage.RESULTCODE) {
            setResult(RESULTCODE, data);
            finish();
        }
    }
}
