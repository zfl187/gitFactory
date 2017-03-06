package com.siyanhui.mojif.bqss_demo.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fantasy on 16/12/29.
 */

public class NetworkManager {
    private static ExecutorService requestExecutor = Executors.newCachedThreadPool();//删除了全局用户名
    public static void get(BaseNetworkTask task) {
        requestExecutor.execute(new GetRequest(task));
    }

    public static void get(String url, Map<String, String> params, ResultCallback callback) {
        requestExecutor.execute(new GetRequest(url, params, callback));
    }

    public static void post(BaseNetworkTask task) {
        requestExecutor.execute(new PostRequest(task));
    }

    public static void post(String url, Map<String, String> params, byte[] data, ResultCallback callback) {
        requestExecutor.execute(new PostRequest(url, params, data, callback));
    }

    public interface ResultCallback {
        void onSuccess(String result);

        void onFailure(String errorInfo);
    }

    private static class PostRequest extends BaseRequest {
        public PostRequest(BaseNetworkTask task) {
            super(task);
        }

        public PostRequest(String url, Map<String, String> params, byte[] data, ResultCallback callback) {
            super(url, params, data, callback);
        }

        @Override
        protected void initConnection(HttpURLConnection connection) throws IOException {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        }
    }

    private static class GetRequest extends BaseRequest {

        public GetRequest(BaseNetworkTask task) {
            super(task);
        }

        public GetRequest(String url, Map<String, String> params, ResultCallback callback) {
            super(url, params, null, callback);
        }

        @Override
        protected void initConnection(HttpURLConnection connection) throws IOException {
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        }
    }
}
