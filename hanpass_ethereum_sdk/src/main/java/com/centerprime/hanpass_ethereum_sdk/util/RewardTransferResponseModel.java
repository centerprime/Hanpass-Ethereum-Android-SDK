package com.centerprime.hanpass_ethereum_sdk.util;

import com.google.gson.annotations.SerializedName;

public class RewardTransferResponseModel {
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public class Data {
        @SerializedName("tx_hash")
        private String tx_hash;

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }
    }
}
