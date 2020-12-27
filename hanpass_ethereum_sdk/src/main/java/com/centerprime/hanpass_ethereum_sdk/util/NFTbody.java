package com.centerprime.hanpass_ethereum_sdk.util;

import com.google.gson.annotations.SerializedName;

/**
 * Created by CenterPrime on 2020/12/26.
 */
public class NFTbody {
    @SerializedName("wallet_address")
    private String walletAddress;
    @SerializedName("network")
    private String network;
    @SerializedName("function_name")
    private String functionName;

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
