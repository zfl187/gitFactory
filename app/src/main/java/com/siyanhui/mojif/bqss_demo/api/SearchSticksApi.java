package com.siyanhui.mojif.bqss_demo.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fantasy on 16/12/29.
 */

public class SearchSticksApi extends BqssApi {
    public static void getStickers(String q, int p, int size, final IOpenApiCallback<WebSticker> callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("q", q);
        params.put("p", String.valueOf(p));
        params.put("size", String.valueOf(size));
        accessGetApi("/emojis/net/search/", params, new NetworkManager.ResultCallback() {
            @Override
            public void onSuccess(String t) {
                OpenApiResponseObject responseObject = new OpenApiResponseObject();
                try {
                    JSONObject jsonObject = new JSONObject(t);
                    responseObject.setEmojis(getWebStickerListFromData(jsonObject.optJSONArray("emojis")));
                    int count = jsonObject.getInt("count");
                    responseObject.setCount(count);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onSuccess(responseObject);
            }

            @Override
            public void onFailure(String errorInfo) {
                callback.onError(errorInfo);
            }
        });
    }

    public static void getTrendingStickers(int p, int size, final IOpenApiCallback<WebSticker> callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("p", String.valueOf(p));
        params.put("size", String.valueOf(size));
        accessGetApi("/trending/", params, new NetworkManager.ResultCallback() {
            @Override
            public void onSuccess(String t) {
                OpenApiResponseObject responseObject = new OpenApiResponseObject();
                try {
                    JSONObject jsonObject = new JSONObject(t);
                    responseObject.setEmojis(getWebStickerListFromData(jsonObject.optJSONArray("emojis")));
                    int count = jsonObject.getInt("count");
                    responseObject.setCount(count);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onSuccess(responseObject);
            }

            @Override
            public void onFailure(String errorInfo) {
                callback.onError(errorInfo);
            }
        });
    }

    public static void getHomePageStickers(final IOpenApiCallback<HotTag> callback) {
        HashMap<String, String> params = new HashMap<>();
        accessGetApi("/netword/homepage/", params, new NetworkManager.ResultCallback() {
            @Override
            public void onSuccess(String t) {
                OpenApiResponseObject responseObject = new OpenApiResponseObject();
                try {
                    JSONObject jsonObject = new JSONObject(t);
                    JSONObject jsonObjectData = jsonObject.optJSONObject("data");
                    responseObject.setEmojis(getHotTagsFromData(jsonObjectData.optJSONArray("chat")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onSuccess(responseObject);
            }

            @Override
            public void onFailure(String errorInfo) {
                callback.onError(errorInfo);
            }
        });
    }

    private static List<HotTag> getHotTagsFromData(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return null;
        }
        List<HotTag> hotTagList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            HotTag hotTag = new HotTag(jsonArray.getJSONObject(i));
            hotTagList.add(hotTag);
        }
        return hotTagList;
    }

    private static List<WebSticker> getWebStickerListFromData(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return null;
        }
        List<WebSticker> webStickerList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            WebSticker webSticker = new WebSticker(jsonArray.getJSONObject(i));
            webStickerList.add(webSticker);
        }
        return webStickerList;
    }
}
