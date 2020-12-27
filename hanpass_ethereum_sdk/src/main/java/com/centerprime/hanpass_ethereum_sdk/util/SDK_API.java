package com.centerprime.hanpass_ethereum_sdk.util;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by CenterPrime on 2020/12/26.
 */
public interface SDK_API {
    @POST("/api/v1/sendNFT")
    Single<BaseResponse<Object>> sendNFT(@Body NFTbody nfTbody);
}
