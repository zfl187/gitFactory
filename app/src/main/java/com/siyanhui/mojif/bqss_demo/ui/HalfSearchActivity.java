package com.siyanhui.mojif.bqss_demo.ui;

import android.app.Activity;
import android.content.Context;
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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.siyanhui.mojif.bqss_demo.Base64Img;
import com.siyanhui.mojif.bqss_demo.BqssConstants;
import com.siyanhui.mojif.bqss_demo.R;
import com.siyanhui.mojif.bqss_demo.api.IOpenApiCallback;
import com.siyanhui.mojif.bqss_demo.api.OpenApiResponseObject;
import com.siyanhui.mojif.bqss_demo.api.SearchSticksApi;
import com.siyanhui.mojif.bqss_demo.api.WebSticker;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssChatAdapter;
import com.siyanhui.mojif.bqss_demo.ui.widget.BqssEditView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.siyanhui.mojif.bqss_demo.BqssConstants.TRENDING_STICKER_TAG;

/**
 * Created by fantasy on 16/12/30.
 */

public class HalfSearchActivity extends Activity {
    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private static final int REFRESH = 3;
    private static GridAdapter mAdapter;
    private BqssEditView mBqssEditView;
    private static MyHandler mHandler;
    private Context mContext;
    private GridView mGridView;
    private String mKeyword;
    private int mCurrentPage = 1;
    private boolean mNeedLoadMore;
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
        setContentView(R.layout.activity_half_search);
        mContext = this;
        LinearLayout backBtn = (LinearLayout) findViewById(R.id.back_btn);
        TextView mTitleText = (TextView) findViewById(R.id.title_text);
        mTitleText.setText("键盘搜索");
        mListView = (ListView) findViewById(R.id.listview);
        mBqssEditView = (BqssEditView) findViewById(R.id.bqss_editview);
        TextView mCancelBtn = (TextView) findViewById(R.id.cancel_btn);
        mBqssBtn = (ImageView) findViewById(R.id.go_bqss_btn);
        mTopLine = findViewById(R.id.top_line);
        mEditLayout = (LinearLayout) findViewById(R.id.bqss_edit_layout);
        mBqssBtn.setImageResource(R.mipmap.icon);
        mETLayout = (LinearLayout) findViewById(R.id.edittext_layout);
        mETLayout.setVisibility(View.GONE);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mTextSendBtn = (Button) findViewById(R.id.text_send_btn);
        mBqssSearchLayout = (LinearLayout) findViewById(R.id.bqss_search_layout);
        mHandler = new MyHandler(this);

        getTrendingStickers(1);
        mKeyword = TRENDING_STICKER_TAG;
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setNumColumns(4);
        mGridView.setPadding(10, 10, 10, 10);
        mGridView.setHorizontalSpacing(10);
        mGridView.setVerticalSpacing(10);
        mAdapter = new GridAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setVisibility(View.GONE);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() >= view.getCount() - 1) {
                        if (mNeedLoadMore) {
                            mNeedLoadMore = false;
                            if (mKeyword.equals(BqssConstants.TRENDING_STICKER_TAG)) {
                                getTrendingStickers(mCurrentPage);
                            } else {
                                getStickers(mCurrentPage);
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
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
                mGridView.setVisibility(View.GONE);
                mBqssBtn.setImageResource(R.mipmap.icon_gray);
                showEdittext();
            }
        });
        mBqssEditView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGridView.setVisibility(View.GONE);
                showSoftInput(mBqssEditView);
                return false;
            }
        });
        mDatas = new ArrayList<>();
        mBqssChatAdapter = new BqssChatAdapter(mDatas);
        mListView.setAdapter(mBqssChatAdapter);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGridView.setVisibility(View.GONE);
                hideSoftInput(mListView);
                return false;
            }
        });

        mBqssBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHiddenBqss) {//打开表情搜搜
                    isHiddenBqss = false;
                    hideSoftInput(mListView);
                    mETLayout.setVisibility(View.GONE);
                    mBqssBtn.setImageResource(R.mipmap.icon);
                    mBqssSearchLayout.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.VISIBLE);
//                    showSoftInput(mBqssEditView);
                } else {//关闭表情搜搜
                    isHiddenBqss = true;
                    mBqssSearchLayout.setVisibility(View.GONE);
                    mGridView.setVisibility(View.GONE);
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
                    Toast.makeText(HalfSearchActivity.this, "不能发送空消息", Toast.LENGTH_SHORT).show();
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

    private void getTrendingStickers(int page) {
        SearchSticksApi.getTrendingStickers(page, BqssConstants.loadSize, new IOpenApiCallback<WebSticker>() {
            @Override
            public void onSuccess(OpenApiResponseObject<WebSticker> result) {
                Message message = Message.obtain();
                message.arg1 = SUCCESS;
                message.arg2 = mCurrentPage;
                message.obj = result.getEmojis();
                mHandler.sendMessage(message);
                if (result.getEmojis().size() == BqssConstants.loadSize) {
                    ++mCurrentPage;
                    mNeedLoadMore = true;
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

    private void getStickers(int page) {
        SearchSticksApi.getStickers(mKeyword, page, BqssConstants.loadSize, new IOpenApiCallback<WebSticker>() {
            @Override
            public void onSuccess(OpenApiResponseObject<WebSticker> result) {
                Message message = Message.obtain();
                message.arg1 = SUCCESS;
                message.arg2 = mCurrentPage;
                message.obj = result.getEmojis();
                mHandler.sendMessage(message);
                if (result.getEmojis().size() == BqssConstants.loadSize && mCurrentPage < 5) {
                    ++mCurrentPage;
                    mNeedLoadMore = true;
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

    private static class GridAdapter extends BaseAdapter {
        List<WebSticker> content;

        @Override
        public int getCount() {
            if (content != null)
                return content.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (content == null) return null;
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(parent.getContext(), R.layout.sticker_item, null);
                viewHolder.simpleDraweeView = (SimpleDraweeView) convertView.findViewById(R.id.sticker_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final String mainImg = content.get(position).getMain();
            Uri uri = Uri.parse(mainImg);

            DraweeController draweeController = Fresco.newDraweeControllerBuilder().setUri(uri).setAutoPlayAnimations(true).build();
            GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(parent.getResources());
            GenericDraweeHierarchy hierarchy = builder.setPlaceholderImage(R.raw.bqss_sticker_loading).setFailureImage(R.mipmap.icon_loading_failed).setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
            viewHolder.simpleDraweeView.setHierarchy(hierarchy);
            viewHolder.simpleDraweeView.setController(draweeController);
            viewHolder.simpleDraweeView.setOnClickListener(new View.OnClickListener() {
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
                }
            });
            return convertView;
        }

        public void setContent(List<WebSticker> content) {
            this.content = content;
            notifyDataSetChanged();
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<HalfSearchActivity> weakReference;

        MyHandler(HalfSearchActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HalfSearchActivity activity = weakReference.get();
            if (activity != null) {
                switch (msg.arg1) {
                    case SUCCESS:
                        List<WebSticker> webStickerList = (List<WebSticker>) msg.obj;
                        if (webStickerList == null || webStickerList.size() == 0) {
                            Toast.makeText(activity, "无搜索结果", Toast.LENGTH_SHORT).show();
                        } else {
                            activity.mGridView.setVisibility(View.VISIBLE);
                            activity.mTopLine.setVisibility(View.VISIBLE);
                            if (msg.arg2 == 1) {
                                mAdapter.content = webStickerList;
                            } else {
                                if (mAdapter.content == null)
                                    mAdapter.content = webStickerList;
                                else
                                    mAdapter.content.addAll(webStickerList);
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
            mKeyword = mBqssEditView.getText().toString().trim();
            if (!TextUtils.isEmpty(mKeyword)) {
                hideSoftInput(mGridView);
                mCurrentPage = 1;
                getStickers(mCurrentPage);
            } else {
                Toast.makeText(mContext, "请输入关键词", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    static class ViewHolder {
        private SimpleDraweeView simpleDraweeView;
    }
}
