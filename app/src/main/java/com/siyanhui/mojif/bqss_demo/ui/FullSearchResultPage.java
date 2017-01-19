package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.siyanhui.mojif.bqss_demo.BqssConstants;
import com.siyanhui.mojif.bqss_demo.R;
import com.siyanhui.mojif.bqss_demo.api.IOpenApiCallback;
import com.siyanhui.mojif.bqss_demo.api.OpenApiResponseObject;
import com.siyanhui.mojif.bqss_demo.api.SearchSticksApi;
import com.siyanhui.mojif.bqss_demo.api.WebSticker;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssEditView;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssFooterView;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssWordWrapView;
import com.siyanhui.mojif.bqss_demo.utils.BqssPreferenceHelper;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fantasy on 17/1/5.
 */

public class FullSearchResultPage extends Activity {
    public static int RESULTCODE = 1003;
    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private static final int PREVIEWIMG = 3;
    private static GridAdapter mAdapter;
    private BqssEditView mBqssEditView;
    private static MyHandler mHandler;
    private GridView mGridView;
    private String mKeyword;
    private int mCurrentPage = 1;
    private static boolean mNeedLoadMore;
    private RelativeLayout mPreviewImageLayout;
    private SimpleDraweeView mSimpleDraweeView;
    private RelativeLayout mSendBtn;
    private RelativeLayout mCancelBtn;
    private String mSendImageUrl;
    private BqssWordWrapView bqssWordWrapView;
    private LinearLayout searchHistoryView;
    private TextView clearSearchHistoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fs_result_page);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        TextView backText = (TextView) findViewById(R.id.back_text);
        titleText.setText("全屏搜索");
        backText.setText("返回");
        LinearLayout backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPreviewImageLayout = (RelativeLayout) findViewById(R.id.preview_bg);
        mSimpleDraweeView = (SimpleDraweeView) findViewById(R.id.preview_img);
        mBqssEditView = (BqssEditView) findViewById(R.id.bqss_editview);
        mHandler = new MyHandler(this);
        Intent intent = getIntent();
        mKeyword = intent.getStringExtra("keyword");
        mGridView = (GridView) findViewById(R.id.result_gridview);
        mGridView.setNumColumns(3);
        mGridView.setPadding(10, 10, 10, 10);
        mGridView.setHorizontalSpacing(10);
        mGridView.setVerticalSpacing(10);
        mAdapter = new GridAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() >= view.getCount() - 1) {
                        if (mNeedLoadMore) {
                            mNeedLoadMore = false;
                            if (mAdapter != null) {
                                mAdapter.setFooterViewStatus(BqssFooterView.LOADING);
                            }
                            getStickers(mCurrentPage);
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mSendBtn = (RelativeLayout) findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.putExtra("stickerData", mSendImageUrl);
                setResult(RESULTCODE, sendIntent);
                finish();
            }
        });
        mCancelBtn = (RelativeLayout) findViewById(R.id.cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreviewImageLayout.setVisibility(View.GONE);
            }
        });
        getStickers(mCurrentPage);
        searchHistoryView = (LinearLayout) findViewById(R.id.search_history_view);
        bqssWordWrapView = (BqssWordWrapView) findViewById(R.id.word_wrap_view);
        mBqssEditView.setOnTouchListener(new View.OnTouchListener() {
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
                    mCurrentPage = 1;
                    mKeyword = keyword;
                    searchHistoryView.setVisibility(View.GONE);
                    getStickers(mCurrentPage);
                }
            });
            bqssWordWrapView.addView(textview);
        }
    }

    private void getStickers(int page) {
        SearchSticksApi.getStickers(mKeyword, page, BqssConstants.fullSearchLoadSize, new IOpenApiCallback<WebSticker>() {
            @Override
            public void onSuccess(OpenApiResponseObject<WebSticker> result) {
                Message message = Message.obtain();
                message.arg1 = SUCCESS;
                message.arg2 = mCurrentPage;
                message.obj = result.getEmojis();
                if (result.getEmojis().size() == BqssConstants.fullSearchLoadSize && mCurrentPage < 5) {
                    ++mCurrentPage;
                    mNeedLoadMore = true;
                } else {
                    mNeedLoadMore = false;
                }
                mHandler.sendMessage(message);
            }

            @Override
            public void onError(String errorInfo) {
                Message message = Message.obtain();
                message.arg1 = ERROR;
                message.obj = errorInfo;
                mHandler.sendMessage(message);
            }
        });
    }

    private static class GridAdapter extends BaseAdapter {
        List<WebSticker> content;

        private BqssFooterView footerView;

        private boolean footerViewEnable = false;

        @Override
        public int getCount() {
            if (content != null)
                return content.size();
            return 0;
        }

        public boolean isFooterViewEnable() {
            return footerViewEnable;
        }


        public void setFootreViewEnable(boolean enable) {
            footerViewEnable = enable;
        }

        private int getDisplayWidth(Activity activity) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            return width;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (content == null) return null;
            if (footerViewEnable && position == content.size() - 1 && mNeedLoadMore) {
                if (footerView == null) {
                    footerView = new BqssFooterView(parent.getContext());

                    GridView.LayoutParams pl = new GridView.LayoutParams(
                            getDisplayWidth((Activity) parent.getContext()),
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    footerView.setLayoutParams(pl);
                }
                setFooterViewStatus(BqssFooterView.MORE);
                return footerView;
            }
            SimpleDraweeView simpleDraweeView;
            if (convertView == null || (convertView != null && convertView == footerView)) {
                simpleDraweeView = new SimpleDraweeView(parent.getContext());
                int width = parent.getWidth() / 3;
                simpleDraweeView.setLayoutParams(new ViewGroup.LayoutParams(width - 5, width - 5));
            } else {
                simpleDraweeView = (SimpleDraweeView) convertView;
            }

            if (content.get(position) != null) {
                final String mainImg = content.get(position).getMain();
                Uri uri = Uri.parse(mainImg);

                DraweeController draweeController = Fresco.newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build();
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(parent.getResources());
                GenericDraweeHierarchy hierarchy = builder.setPlaceholderImage(R.raw.bqss_sticker_loading).setFailureImage(R.mipmap.icon_loading_failed).setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
                simpleDraweeView.setHierarchy(hierarchy);
                simpleDraweeView.setController(draweeController);
                simpleDraweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message message = Message.obtain();
                        message.obj = mainImg;
                        message.arg1 = PREVIEWIMG;
                        mHandler.sendMessage(message);
                    }
                });
            }
            return simpleDraweeView;
        }

        public void setContent(List<WebSticker> content) {
            this.content = content;
            notifyDataSetChanged();
        }

        public BqssFooterView getFooterView() {
            return footerView;
        }

        public void setFooterViewStatus(int status) {
            if (footerView != null) {
                footerView.setStatus(status);
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<FullSearchResultPage> weakReference;

        MyHandler(FullSearchResultPage activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FullSearchResultPage activity = weakReference.get();
            if (activity != null) {
                switch (msg.arg1) {
                    case SUCCESS:
                        List<WebSticker> webStickerList = (List<WebSticker>) msg.obj;
                        if (webStickerList == null || webStickerList.size() == 0) {
                            Toast.makeText(activity, "无搜索结果", Toast.LENGTH_SHORT).show();
                        } else {
                            activity.mGridView.setVisibility(View.VISIBLE);

                            if (msg.arg2 == 1) {
                                mAdapter.content = webStickerList;
                            } else {
                                if (mAdapter.content == null)
                                    mAdapter.content = webStickerList;
                                else
                                    // 在添加数据之前删除最后的伪造item
                                    if (mAdapter.isFooterViewEnable()) {
                                        mAdapter.content.remove(mAdapter.content.get(mAdapter.content.size() - 1));
                                    }
                                mAdapter.content.addAll(webStickerList);
                            }
                            if (mNeedLoadMore) {
                                mAdapter.content.add(null);
                                mAdapter.setFootreViewEnable(true);
                            }
                            mAdapter.notifyDataSetChanged();
                            if (msg.arg2 == 1) {
                                activity.mGridView.smoothScrollToPosition(0);
                            }
                        }
                        break;
                    case ERROR:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case PREVIEWIMG:
                        activity.mPreviewImageLayout.setVisibility(View.VISIBLE);
                        activity.mSendImageUrl = (String) msg.obj;
                        Uri uri = Uri.parse(activity.mSendImageUrl);
                        DraweeController draweeController = Fresco.newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build();
                        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(activity.getResources());
                        GenericDraweeHierarchy hierarchy = builder.setPlaceholderImage(R.raw.bqss_sticker_loading).setFailureImage(R.mipmap.icon_loading_failed).setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
                        activity.mSimpleDraweeView.setHierarchy(hierarchy);
                        activity.mSimpleDraweeView.setController(draweeController);
                        break;
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            searchHistoryView.setVisibility(View.GONE);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(FullSearchResultPage.this.getCurrentFocus().getWindowToken(), 0);
            }
            mKeyword = mBqssEditView.getText().toString();
            if (!TextUtils.isEmpty(mKeyword)) {
                BqssPreferenceHelper.addSearchKeyword(mKeyword);
                mCurrentPage = 1;
                getStickers(mCurrentPage);
            } else {
                Toast.makeText(FullSearchResultPage.this, "请输入关键词", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
