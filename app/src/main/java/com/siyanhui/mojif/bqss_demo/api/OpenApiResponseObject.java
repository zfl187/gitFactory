package com.siyanhui.mojif.bqss_demo.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by fantasy on 16/10/19.
 */
public class OpenApiResponseObject<T> implements Serializable {
    private Integer errorCode;
    private Integer count;
    private List<T> emojis;

    public OpenApiResponseObject(){

    }
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<T> getEmojis() {
        return emojis;
    }

    public void setEmojis(List<T> emojis) {
        this.emojis = emojis;
    }
}
