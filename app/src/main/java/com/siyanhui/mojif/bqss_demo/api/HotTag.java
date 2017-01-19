package com.siyanhui.mojif.bqss_demo.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fantasy on 17/1/5.
 */

public class HotTag {
    public HotTag(JSONObject jsonObject) {
        this.setGuid(jsonObject.optString("guid"));
        this.setText(jsonObject.optString("text"));
        try {
            JSONObject stickerCoverJsonObject = (JSONObject) jsonObject.optJSONArray("emoticions").get(0);
            this.setMain(stickerCoverJsonObject.optString("main"));
            this.setThumb(stickerCoverJsonObject.optString("thumb"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String guid;
    private String text;
    private String main;
    private String thumb;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
