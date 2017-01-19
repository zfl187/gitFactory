package com.siyanhui.mojif.bqss_demo.api;

import java.util.Map;

/**
 * Created by fantasy on 16/12/29.
 */

public class BaseNetworkTask {
    protected String mUrl;
    protected Map<String, String> mParams;
    protected byte[] mData;
    protected NetworkManager.ResultCallback mCallback;

    public BaseNetworkTask(BaseNetworkTask another) {
        mUrl = another.mUrl;
        mParams = another.mParams;
        mData = another.mData;
        mCallback = another.mCallback;
    }

    public BaseNetworkTask(String url, Map<String, String> params, byte[] data, NetworkManager.ResultCallback callback) {
        mUrl = url;
        mParams = params;
        mCallback = callback;
        mData = data;
    }

}
