package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.siyanhui.mojif.bqss_demo.Base64Img;
import com.siyanhui.mojif.bqss_demo.BqssConstants;
import com.siyanhui.mojif.bqss_demo.R;
import com.siyanhui.mojif.bqss_demo.api.IOpenApiCallback;
import com.siyanhui.mojif.bqss_demo.api.OpenApiResponseObject;
import com.siyanhui.mojif.bqss_demo.api.SearchSticksApi;
import com.siyanhui.mojif.bqss_demo.api.WebSticker;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssChatAdapter;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssEditView;
import com.siyanhui.mojif.bqss_demo.utils.dip2px;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by fantasy on 16/12/29.
 */

public class QuaterSearchActivity extends Activity {
    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private static final int REFRESH = 3;
    private static MyHandler mHandler;
    private LinearLayout mStickersContainer;
    private BqssEditView mBqssEditView;
    private BqssHorizontalScrollView mBqssHScrollview;
    private static boolean mNeedLoadMore;
    private BqssChatAdapter mBqssChatAdapter;
    private ListView mListView;
    private List<String> mDatas;
    private View mTopLine;
    private LinearLayout mEditLayout;
    private ImageView mBqssBtn;
    private boolean isHiddenBqss = false;

    private EditText mEditText;
    private LinearLayout mETLayout;//文字输入键盘外的layout
    private Button mTextSendBtn;

    private LinearLayout mBqssSearchLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quater_search);
        TextView mTitleText = (TextView) findViewById(R.id.title_text);
        mBqssEditView = (BqssEditView) findViewById(R.id.bqss_editview);
        TextView mCancelBtn = (TextView) findViewById(R.id.cancel_btn);
        mBqssHScrollview = (BqssHorizontalScrollView) findViewById(R.id.bqss_hscrollview);
        mETLayout = (LinearLayout) findViewById(R.id.edittext_layout);
        mETLayout.setVisibility(View.GONE);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mTextSendBtn = (Button) findViewById(R.id.text_send_btn);
        mBqssSearchLayout = (LinearLayout) findViewById(R.id.bqss_search_layout);
        mTitleText.setText("联想搜索");
        mTopLine = findViewById(R.id.top_line);
        mListView = (ListView) findViewById(R.id.listview);
        LinearLayout backBtn = (LinearLayout) findViewById(R.id.back_btn);
        mStickersContainer = (LinearLayout) findViewById(R.id.stickers_container);
        mEditLayout = (LinearLayout) findViewById(R.id.bqss_edit_layout);
        mBqssBtn = (ImageView) findViewById(R.id.go_bqss_btn);
        mHandler = new MyHandler(this);
        mBqssBtn.setImageResource(R.mipmap.icon);
        mBqssHScrollview.keyword = BqssConstants.TRENDING_STICKER_TAG;
        mBqssHScrollview.currentPage = 1;
        mBqssHScrollview.getTrendingStickers();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHiddenBqss = true;
                mBqssSearchLayout.setVisibility(View.GONE);
                mBqssBtn.setImageResource(R.mipmap.icon_gray);
                showEdittext();
            }
        });
        mDatas = new ArrayList<>();
        mBqssChatAdapter = new BqssChatAdapter(mDatas);
        mListView.setAdapter(mBqssChatAdapter);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftInput(mListView);
                mBqssHScrollview.setVisibility(View.GONE);
                mTopLine.setVisibility(View.GONE);
                return false;
            }
        });
        mBqssBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHiddenBqss) {//打开表情搜搜
                    mETLayout.setVisibility(View.GONE);

                    mBqssSearchLayout.setVisibility(View.VISIBLE);
                    isHiddenBqss = false;
                    mBqssEditView.setFocusable(true);
                    mBqssEditView.setFocusableInTouchMode(true);
                    mBqssEditView.requestFocus();
                    mBqssBtn.setImageResource(R.mipmap.icon);
                    showSoftInput(mBqssEditView);
                } else {//关闭表情搜搜
                    isHiddenBqss = true;
                    mBqssSearchLayout.setVisibility(View.GONE);
                    mBqssBtn.setImageResource(R.mipmap.icon_gray);
                    showEdittext();
                }
            }
        });
        mTextSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = BqssConstants.BQSS_TEXT_TAG + mEditText.getText().toString();
                if (TextUtils.isEmpty(mEditText.getText().toString().trim())) {
                    Toast.makeText(QuaterSearchActivity.this, "不能发送空消息", Toast.LENGTH_SHORT).show();
                } else {
                    mDatas.add(text);
                    mBqssChatAdapter.refresh(mDatas);
                    mEditText.setText("");
                }
            }
        });
    }

    private void showEdittext() {
        mETLayout.setVisibility(View.VISIBLE);
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        showSoftInput(mEditText);
    }

    private static class BqssHorizontalScrollView extends HorizontalScrollView {
        private String keyword = "";
        private int currentPage = 1;


        public BqssHorizontalScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BqssHorizontalScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            int maxX = getChildAt(0).getMeasuredWidth() - getMeasuredWidth();
            if (maxX == getScrollX()) {
                loadMore();
            }
        }

        public void loadMore() {
            if (mNeedLoadMore) {
                mNeedLoadMore = false;
                if (keyword.equals(BqssConstants.TRENDING_STICKER_TAG)) {
                    getTrendingStickers();
                } else {
                    getStickers();
                }
            }
        }

        private void getStickers() {
            SearchSticksApi.getStickers(keyword, currentPage, BqssConstants.loadSize, new IOpenApiCallback<WebSticker>() {
                @Override
                public void onSuccess(OpenApiResponseObject<WebSticker> result) {
                    Message message = Message.obtain();
                    message.arg1 = SUCCESS;
                    message.arg2 = currentPage;
                    message.obj = result.getEmojis();
                    mHandler.sendMessage(message);
                    if (result.getEmojis().size() == BqssConstants.loadSize && currentPage < 5) {
                        mNeedLoadMore = true;
                        currentPage++;
                    } else {
                        mNeedLoadMore = false;
                    }
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

        private void getTrendingStickers() {
            SearchSticksApi.getTrendingStickers(currentPage, BqssConstants.loadSize, new IOpenApiCallback<WebSticker>() {
                @Override
                public void onSuccess(OpenApiResponseObject<WebSticker> result) {
                    Message message = Message.obtain();
                    message.arg1 = SUCCESS;
                    message.arg2 = currentPage;
                    message.obj = result.getEmojis();
                    mHandler.sendMessage(message);
                    if (result.getEmojis().size() == BqssConstants.loadSize) {
                        mNeedLoadMore = true;
                        currentPage++;
                    } else {
                        mNeedLoadMore = false;
                    }
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
    }

    private  class MyHandler extends Handler {
        WeakReference<QuaterSearchActivity> weakReference;

        MyHandler(QuaterSearchActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final QuaterSearchActivity activity = weakReference.get();
            if (activity != null) {
                if (msg.arg2 == 1) {
                    activity.mStickersContainer.removeAllViews();
                    activity.mBqssHScrollview.scrollTo(0, 0);
                }
                switch (msg.arg1) {
                    case SUCCESS:
                        List<WebSticker> webStickerList = (List<WebSticker>) msg.obj;
                        if (webStickerList == null || webStickerList.size() == 0) {
                            if (msg.arg2 == 1)
                                Toast.makeText(activity, "无搜索结果", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < webStickerList.size(); i++) {
                                final String mainImg = webStickerList.get(i).getMain();
                                Uri uri = Uri.parse(mainImg);
//                                View gifRootView = View.inflate(QuaterSearchActivity.this,R.layout.gif_ll,null);
//                                SimpleDraweeView simpleDraweeView = (SimpleDraweeView)gifRootView.findViewById(R.id.gif_view);
                                SimpleDraweeView simpleDraweeView = new SimpleDraweeView(activity);
                                DraweeController draweeController = Fresco.newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build();
                                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(activity.getResources());
//                                builder.setPressedStateOverlay(QuaterSearchActivity.this.getResources().getDrawable(R.color.black));
//                                builder.setOverlay(QuaterSearchActivity.this.getResources().getDrawable(R.color.transparent));
//                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(7f);
//                                roundingParams.setRoundingMethod(RoundingParams.RoundingMethod.OVERLAY_COLOR).setOverlayColor(R.color.white);
//                                roundingParams.setRoundAsCircle(false);
//                                roundingParams.setCornersRadius(15f);
//                                roundingParams.setBorderColor(R.color.transparent);
//                                roundingParams.setBorderWidth(2f);
                                RoundingParams overlayColor = RoundingParams.fromCornersRadius(25)
                                        .setRoundingMethod(RoundingParams.RoundingMethod.OVERLAY_COLOR)
                                        .setOverlayColor(Color.WHITE);
//                                        .setBorderColor(Color.GRAY)
//                                        .setBorderWidth(2f);
//                                builder.setRoundingParams(roundingParams);
                                GenericDraweeHierarchy hierarchy = builder.setPlaceholderImage(R.raw.bqss_sticker_loading).setFailureImage(R.mipmap.icon_loading_failed).setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
                                simpleDraweeView.setHierarchy(hierarchy);
                                simpleDraweeView.setController(draweeController);
//                                simpleDraweeView.getHierarchy().setRoundingParams(overlayColor);//dip2px.dip2px(QuaterSearchActivity.this,130)
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT);
                                int gifMarginValue = 2;//dip2px.dip2px(QuaterSearchActivity.this,3);
                                layoutParams.setMargins(gifMarginValue, gifMarginValue, gifMarginValue, gifMarginValue);
                                activity.mBqssHScrollview.setVisibility(View.VISIBLE);
                                activity.mTopLine.setVisibility(View.VISIBLE);

//                                ((View)simpleDraweeView).setBackgroundResource(R.drawable.shape_qutu_search_bg);
//                                ((View)simpleDraweeView).setBackgroundColor(R.color.white);
                                activity.mStickersContainer.addView(simpleDraweeView, layoutParams);
                                simpleDraweeView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Thread thread = new Thread() {
                                            @Override
                                            public void run() {
                                                super.run();
                                                String str = Base64Img.getImageStrFromUrl(mainImg);
                                                Message message = Message.obtain();
                                                message.obj = str;
                                                message.arg1 = REFRESH;
                                                mHandler.sendMessage(message);
                                            }
                                        };
                                        thread.start();
                                        activity.mBqssHScrollview.setVisibility(View.GONE);
                                        activity.mTopLine.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }

                        break;
                    case ERROR:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case REFRESH:
                        activity.mDatas.add((String) msg.obj);
                        activity.mBqssChatAdapter.refresh(activity.mDatas);
                        break;
                }

            }
        }

    }

    private void hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN && mBqssEditView.hasFocus()) {
            String content = mBqssEditView.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                mBqssHScrollview.keyword = content;
                mBqssHScrollview.currentPage = 1;
                mBqssHScrollview.getStickers();
            } else {
                Toast.makeText(QuaterSearchActivity.this, "请输入关键词", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
