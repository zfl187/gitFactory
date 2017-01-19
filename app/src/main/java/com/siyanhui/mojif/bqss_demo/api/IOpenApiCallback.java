package com.siyanhui.mojif.bqss_demo.api;


/**
 * Created by fantasy on 16/10/19.
 */
public interface IOpenApiCallback<T> {
    void onSuccess(OpenApiResponseObject<T> result);

    void onError(String errorInfo);
}
