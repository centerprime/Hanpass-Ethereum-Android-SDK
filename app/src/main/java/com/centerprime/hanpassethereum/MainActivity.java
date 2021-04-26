package com.centerprime.hanpassethereum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.centerprime.hanpass_ethereum_sdk.EthManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EthManager ethManager = EthManager.getInstance();
        ethManager.init("https://mainnet.infura.io/v3/a396c3461ac048a59f389c7778f06689");

        ethManager.rewardTransfer(this,"2021040116244001412955",
                "2021-04-19 12:08:52",
                "2000000",
                "KRW",
                "KR",
                "UZ",
                "TOKEN",
                new EthManager.CallbackHanpass() {
                    @Override
                    public void result(String result) {
                        System.out.println(result);
                    }
                });
    }

}