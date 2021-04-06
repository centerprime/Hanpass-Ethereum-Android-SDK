package com.centerprime.hanpass_ethereum_sdk.util;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HanpassApi {
    @POST("/hanpass_transfer_reward")
    Single<RewardTransferResponseModel> rewardTransfer(@Body RewardTransferReqModel rewardTransferReqModel);
}
