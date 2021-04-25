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
        @SerializedName("infura")
        private String infura;
        @SerializedName("token_id")
        private String token_id;
        @SerializedName("token_uri")
        private String token_uri;
        @SerializedName("token_name")
        private String token_name;
        @SerializedName("token_symbol")
        private String token_symbol;
        @SerializedName("token_address")
        private String token_address;
        @SerializedName("from_address")
        private String from_address;
        @SerializedName("to_address")
        private String to_address;

        public String getFrom_address() {
            return from_address;
        }

        public void setFrom_address(String from_address) {
            this.from_address = from_address;
        }

        public String getTo_address() {
            return to_address;
        }

        public void setTo_address(String to_address) {
            this.to_address = to_address;
        }

        public String getToken_id() {
            return token_id;
        }

        public void setToken_id(String token_id) {
            this.token_id = token_id;
        }

        public String getToken_uri() {
            return token_uri;
        }

        public void setToken_uri(String token_uri) {
            this.token_uri = token_uri;
        }

        public String getToken_name() {
            return token_name;
        }

        public void setToken_name(String token_name) {
            this.token_name = token_name;
        }

        public String getToken_symbol() {
            return token_symbol;
        }

        public void setToken_symbol(String token_symbol) {
            this.token_symbol = token_symbol;
        }

        public String getToken_address() {
            return token_address;
        }

        public void setToken_address(String token_address) {
            this.token_address = token_address;
        }

        public String getInfura() {
            return infura;
        }

        public void setInfura(String infura) {
            this.infura = infura;
        }

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }
    }
}
