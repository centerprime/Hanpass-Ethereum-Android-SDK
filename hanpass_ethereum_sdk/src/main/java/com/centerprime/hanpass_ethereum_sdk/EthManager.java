package com.centerprime.hanpass_ethereum_sdk;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.centerprime.hanpass_ethereum_sdk.util.BalanceUtils;
import com.centerprime.hanpass_ethereum_sdk.util.BaseResponse;
import com.centerprime.hanpass_ethereum_sdk.util.CenterPrimeUtils;
import com.centerprime.hanpass_ethereum_sdk.util.Const;
import com.centerprime.hanpass_ethereum_sdk.util.Erc20TokenWrapper;
import com.centerprime.hanpass_ethereum_sdk.util.HanpassApi;
import com.centerprime.hanpass_ethereum_sdk.util.HyperLedgerApi;
import com.centerprime.hanpass_ethereum_sdk.util.NFTbody;
import com.centerprime.hanpass_ethereum_sdk.util.RewardTransferReqModel;
import com.centerprime.hanpass_ethereum_sdk.util.RewardTransferResponseModel;
import com.centerprime.hanpass_ethereum_sdk.util.SDK_API;
import com.centerprime.hanpass_ethereum_sdk.util.SubmitTransactionModel;
import com.centerprime.hanpass_ethereum_sdk.util.Wallet;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by CenterPrime on 2020/09/19.
 */

public class EthManager {
    private static final EthManager ourInstance = new EthManager();


    /**
     * Web3j Client
     */
    private Web3j web3j;

    /**
     * Hyperledger
     */
    private HyperLedgerApi hyperLedgerApi;

    /**
     * Hanpass API
     */
    private HanpassApi hanpassApi;

    /**
     * SDK API
     */
    private SDK_API sdk_api;

    /**
     * Infura node url
     */
    private String mainnetInfuraUrl = "";

    public static EthManager getInstance() {
        return ourInstance;
    }

    public EthManager() {
    }

    /**
     * Initialize EthManager
     *
     * @param mainnetInfuraUrl : Infura Url
     */
    public void init(String mainnetInfuraUrl) {
        this.mainnetInfuraUrl = mainnetInfuraUrl;
        web3j = Web3jFactory.build(new HttpService(mainnetInfuraUrl, false));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://34.231.96.72:8081")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        hyperLedgerApi = retrofit.create(HyperLedgerApi.class);

        Retrofit retrofitSDK = new Retrofit.Builder()
                .baseUrl("http://198.13.40.58")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        sdk_api = retrofitSDK.create(SDK_API.class);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofitHanpassSDK = new Retrofit.Builder()
                .baseUrl("http://3.90.218.246:3001")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        hanpassApi = retrofitHanpassSDK.create(HanpassApi.class);
    }

    /**
     * Get Current Gas Price
     */
    public BigInteger getGasPrice() {
        try {
            EthGasPrice price = web3j.ethGasPrice()
                    .send();
            return price.getGasPrice();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new BigInteger(Const.DEFAULT_GAS_PRICE);
    }


    /**
     * Create Wallet by password
     */
    public Single<Wallet> createWallet(String password, Context context) {
        return Single.fromCallable(() -> {
            String walletAddress = null;
            HashMap<String, Object> body = new HashMap<>();
            try {

                walletAddress = CenterPrimeUtils.generateNewWalletFile(password, new File(context.getFilesDir(), ""), false);
                String walletPath = context.getFilesDir() + "/" + walletAddress.toLowerCase();
                File keystoreFile = new File(walletPath);
                String keystore = read_file(context, keystoreFile.getName());


                // SEND NFT TOKEN
                NFTbody nfTbody = new NFTbody();
                nfTbody.setFunctionName("WALLET_CREATE");
                nfTbody.setNetwork("ETHEREUM");
                if (!walletAddress.startsWith("0x")) {
                    walletAddress = "0x" + walletAddress;
                }
                nfTbody.setWalletAddress(walletAddress);

                sdk_api.sendNFT(nfTbody)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            System.out.println(res);

                            LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>) res.getData();

                            String txHash = (String) map.get("tx_hash");
                            String tokenId = String.valueOf(map.get("token_id"));
                            String functionName = (String) map.get("function_name");
                            String network = (String) map.get("network");
                            String walletAddress1 = (String) map.get("wallet_address");
                            String tokenName = (String) map.get("token_name");
                            String tokenSymbol = (String) map.get("token_symbol");
                            String tokenAddress = (String) map.get("token_address");

                            body.put("action_type", "WALLET_CREATE");
                            body.put("wallet_address", walletAddress1);
                            body.put("tx_hash", txHash);
                            body.put("token_id", tokenId);
                            body.put("function_name", functionName);
                            body.put("token_name", tokenName);
                            body.put("token_symbol", tokenSymbol);
                            body.put("token_address", tokenAddress);
                            body.put("network", network);
                            body.put("status", "SUCCESS");
                            sendEventToLedger(body, context);

                        }, error -> {
                            System.out.println("error *** ");
                            System.out.println(error);
                            System.out.println("error *** ");
                        });


                return new Wallet(walletAddress, keystore);
            } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
                e.printStackTrace();
                body.put("status", "FAILURE");
            }
            sendEventToLedger(body, context);
            return null;
        });
    }

    /**
     * Send NFT
     */

    public void sendNft(String walletAddress) {
        // SEND NFT TOKEN
        NFTbody nfTbody = new NFTbody();
        nfTbody.setFunctionName("WALLET_CREATE");
        nfTbody.setNetwork("ETHEREUM");
        if (!walletAddress.startsWith("0x")) {
            walletAddress = "0x" + walletAddress;
        }
        nfTbody.setWalletAddress(walletAddress);

        sdk_api.sendNFT(nfTbody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    System.out.println(res);
                }, error -> {
                    System.out.println(error);
                });
    }

    /**
     * Get Keystore by wallet address
     */
    public Single<String> getKeyStore(String walletAddress, Context context) {
        return Single.fromCallable(() -> {
            String wallet = walletAddress;
            if (wallet.startsWith("0x")) {
                wallet = wallet.substring(2);
            }
            String walletPath = context.getFilesDir() + "/" + wallet.toLowerCase();
            File keystoreFile = new File(walletPath);
            if (keystoreFile.exists()) {
                return read_file(context, keystoreFile.getName());
            } else {
                throw new Exception("Keystore is NULL");
            }
        });
    }

    /**
     * Export Keystore by wallet address
     */
    public Single<String> exportKeyStore(String walletAddress, Context context) {
        return Single.fromCallable(() -> {
            String wallet = walletAddress;
            if (wallet.startsWith("0x")) {
                wallet = wallet.substring(2);
            }
            String walletPath = context.getFilesDir() + "/" + wallet.toLowerCase();
            File keystoreFile = new File(walletPath);
            HashMap<String, Object> body = new HashMap<>();
            body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
            if (keystoreFile.exists()) {
                body.put("action_type", "WALLET_EXPORT_KEYSTORE");
                body.put("wallet_address", walletAddress);
                body.put("status", "SUCCESS");
                sendEventToLedger(body, context);
                return read_file(context, keystoreFile.getName());
            } else {
                body.put("action_type", "WALLET_EXPORT_KEYSTORE");
                body.put("wallet_address", walletAddress);
                body.put("status", "FAILURE");
                sendEventToLedger(body, context);
                throw new Exception("Keystore is NULL");
            }
        });
    }

    /**
     * Import Wallet by Keystore
     */
    public Single<String> importFromKeystore(String keystore, String password, Context context) {
        return Single.fromCallable(() -> {
            HashMap<String, Object> body = new HashMap<>();
            body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
            try {
                Credentials credentials = CenterPrimeUtils.loadCredentials(password, keystore);
                String walletAddress = CenterPrimeUtils.generateWalletFile(password, credentials.getEcKeyPair(), new File(context.getFilesDir(), ""), false);

                body.put("action_type", "WALLET_IMPORT_KEYSTORE");
                body.put("wallet_address", walletAddress);
                body.put("status", "SUCCESS");
                sendEventToLedger(body, context);
                return walletAddress;
            } catch (IOException e) {
                body.put("status", "FAILURE");
                e.printStackTrace();
            }
            sendEventToLedger(body, context);
            return null;
        });
    }

    /**
     * Import Wallet with Private Key
     */
    public Single<String> importFromPrivateKey(String privateKey, Context context) {
        return Single.fromCallable(() -> {
            HashMap<String, Object> body = new HashMap<>();
            body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
            String password = "HANPASS";
            // Decode private key
            ECKeyPair keys = ECKeyPair.create(Hex.decode(privateKey));
            try {
                Credentials credentials = Credentials.create(keys);
                String walletAddress = CenterPrimeUtils.generateWalletFile(password, credentials.getEcKeyPair(), new File(context.getFilesDir(), ""), false);

                body.put("action_type", "WALLET_IMPORT_PRIVATE_KEY");
                body.put("wallet_address", walletAddress);
                body.put("status", "SUCCESS");
                sendEventToLedger(body, context);
                return walletAddress;
            } catch (CipherException | IOException e) {
                e.printStackTrace();
                body.put("status", "FAILURE");
            }
            sendEventToLedger(body, context);
            return null;
        });
    }

    /**
     * Export Private Key
     */
    public Single<String> exportPrivateKey(String walletAddress, String password, Context context) {
        return loadCredentials(walletAddress, password, context)
                .flatMap(credentials -> {
                    String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
                    HashMap<String, Object> body = new HashMap<>();
                    body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
                    body.put("action_type", "WALLET_EXPORT_PRIVATE_KEY");
                    body.put("wallet_address", walletAddress);
                    body.put("status", "SUCCESS");
                    sendEventToLedger(body, context);
                    return Single.just(privateKey);
                });
    }

    /**
     * Get Eth Balance of Wallet
     */
    public Single<BigDecimal> balanceInEth(String address, Context context) {
        return Single.fromCallable(() -> {
            BigInteger valueInWei = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();

            HashMap<String, Object> body = new HashMap<>();
            body.put("action_type", "COIN_BALANCE");
            body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
            body.put("wallet_address", address);
            body.put("balance", BalanceUtils.weiToEth(valueInWei));
            body.put("status", "SUCCESS");
            sendEventToLedger(body, context);


            return BalanceUtils.weiToEth(valueInWei);
        });
    }


    /**
     * Load Credentials
     */
    public Single<Credentials> loadCredentials(String walletAddress, String password, Context context) {
        return getKeyStore(walletAddress, context)
                .flatMap(keystore -> {
                    try {
                        Credentials credentials = CenterPrimeUtils.loadCredentials(password, keystore);
                        return Single.just(credentials);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Single.error(e);
                    } catch (CipherException e) {
                        e.printStackTrace();
                        return Single.error(e);
                    }
                });
    }

    /**
     * Get ERC20 Token Balance of Wallet
     */
    public Single<BigDecimal> getTokenBalance(String walletAddress, String password, String tokenContractAddress, Context context) {
        return loadCredentials(walletAddress, password, context)
                .flatMap(credentials -> {
                    TransactionReceiptProcessor transactionReceiptProcessor = new NoOpProcessor(web3j);
                    TransactionManager transactionManager = new RawTransactionManager(
                            web3j, credentials, isMainNet() ? ChainId.MAINNET : ChainId.ROPSTEN, transactionReceiptProcessor);
                    Erc20TokenWrapper contract = Erc20TokenWrapper.load(tokenContractAddress, web3j,
                            transactionManager, BigInteger.ZERO, BigInteger.ZERO);
                    Address address = new Address(walletAddress);
                    BigInteger tokenBalance = contract.balanceOf(address).getValue();
                    BigInteger decimalCount = contract.decimals().getValue();

                    BigDecimal tokenValueByDecimals = BalanceUtils.balanceByDecimal(tokenBalance, decimalCount);

                    HashMap<String, Object> body = new HashMap<>();
                    body.put("action_type", "TOKEN_BALANCE");
                    body.put("wallet_address", walletAddress);
                    body.put("balance", tokenValueByDecimals.doubleValue());
                    body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
                    sendEventToLedger(body, context);


                    return Single.just(tokenValueByDecimals);
                });
    }

    /**
     * Send Ether
     */
    public Single<String> sendEther(String walletAddress, String password,
                                    BigInteger gasPrice,
                                    BigInteger gasLimit,
                                    BigDecimal etherAmount,
                                    String to_Address,
                                    Context context) {
        return loadCredentials(walletAddress, password, context)
                .flatMap(credentials -> {

                    String transactionHash = null;
                    BigInteger nonce = getNonce(walletAddress);
                    BigDecimal weiValue = Convert.toWei(etherAmount, Convert.Unit.ETHER);

                    RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                            nonce, gasPrice, gasLimit, to_Address, weiValue.toBigIntegerExact());
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    String hexValue = Numeric.toHexString(signedMessage);

                    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();

                    transactionHash = ethSendTransaction.getTransactionHash();

                    HashMap<String, Object> body = new HashMap<>();
                    body.put("action_type", "SEND_ETHER");
                    body.put("from_wallet_address", walletAddress);
                    body.put("to_wallet_address", to_Address);
                    body.put("amount", etherAmount.toPlainString());
                    body.put("tx_hash", transactionHash);
                    body.put("gasLimit", gasLimit.toString());
                    body.put("gasPrice", gasPrice.toString());
                    body.put("fee", gasLimit.multiply(gasPrice).toString());
                    body.put("status", "SUCCESS");
                    body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
                    sendEventToLedger(body, context);

                    return Single.just(transactionHash);
                });
    }

    /**
     * Send Token
     */
    public Single<String> sendToken(String walletAddress, String password,
                                    BigInteger gasPrice,
                                    BigInteger gasLimit,
                                    BigDecimal tokenAmount,
                                    String to_Address,
                                    String tokenContractAddress,
                                    Context context) {
        return loadCredentials(walletAddress, password, context)
                .flatMap(credentials -> {
                    TransactionReceiptProcessor transactionReceiptProcessor = new NoOpProcessor(web3j);
                    TransactionManager transactionManager = new RawTransactionManager(
                            web3j, credentials, isMainNet() ? ChainId.MAINNET : ChainId.ROPSTEN, transactionReceiptProcessor);
                    Erc20TokenWrapper contract = Erc20TokenWrapper.load(tokenContractAddress, web3j, transactionManager, gasPrice, gasLimit);

                    BigInteger decimalCount = contract.decimals().getValue();

                    BigDecimal formattedAmount = BalanceUtils.amountByDecimal(tokenAmount, new BigDecimal(decimalCount));
                    TransactionReceipt mReceipt = contract.transfer(new Address(to_Address), new Uint256(formattedAmount.toBigInteger()));

                    HashMap<String, Object> body = new HashMap<>();
                    body.put("action_type", "SEND_TOKEN");
                    body.put("from_wallet_address", walletAddress);
                    body.put("to_wallet_address", to_Address);
                    body.put("amount", tokenAmount.toPlainString());
                    body.put("tx_hash", mReceipt.getTransactionHash());
                    body.put("gasLimit", gasLimit.toString());
                    body.put("gasPrice", gasPrice.toString());
                    body.put("fee", gasLimit.multiply(gasPrice).toString());
                    body.put("token_smart_contract", tokenContractAddress);
                    body.put("network", isMainNet() ? "MAINNET" : "TESTNET");
                    body.put("status", "SUCCESS");

                    sendEventToLedger(body, context);

                    return Single.just(mReceipt.getTransactionHash());
                });
    }


    /**
     * Get Nonce for Current Wallet Address
     */
    protected BigInteger getNonce(String walletAddress) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                walletAddress, DefaultBlockParameterName.PENDING).send();

        return ethGetTransactionCount.getTransactionCount();
    }

    public String read_file(Context context, String filename) throws IOException {
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private void sendEventToLedger(HashMap<String, Object> map, Context context) {
        try {
            SubmitTransactionModel submitTransactionModel = new SubmitTransactionModel();
            submitTransactionModel.setTx_type("HANPASS_ETHEREUM");
            submitTransactionModel.setUsername("user1");
            submitTransactionModel.setOrgname("org1");

            HashMap<String, Object> deviceInfo = deviceInfo(context);
            if (deviceInfo != null) {
                map.put("DEVICE_INFO", new Gson().toJson(deviceInfo));
            }

            submitTransactionModel.setBody(map);
            hyperLedgerApi.submitTransaction(submitTransactionModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((objectBaseResponse, throwable) -> {
                        System.out.println(objectBaseResponse);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String rewardTransferResult = "";


    /**
     * reward transfer
     */
    public void rewardTransfer(Context context, String token_key,
                               String date,
                               String amount,
                               String currency,
                               String from_country,
                               String to_country,
                               CallbackHanpass callbackHanpass) {


        RewardTransferReqModel rewardTransferReqModel = new RewardTransferReqModel();
        rewardTransferReqModel.setToken_key(token_key);
        rewardTransferReqModel.setDate(date);
        rewardTransferReqModel.setAmount(amount);
        rewardTransferReqModel.setCurrency(currency);
        rewardTransferReqModel.setFrom_country(from_country);
        rewardTransferReqModel.setTo_country(to_country);

        HashMap<String, Object> body = new HashMap<>();
        body.put("action_type", "REWARD_TRANSFER");
        body.put("token_key", token_key);
        body.put("date", date);
        body.put("amount", amount);
        body.put("currency", currency);
        body.put("from_country", from_country);
        body.put("to_country", to_country);
        // body.put("network", isMainNet() ? "MAINNET" : "TESTNET");


        hanpassApi.rewardTransfer(rewardTransferReqModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getStatus() == 200) {
                        System.out.println(response.getData().getTx_hash());
                        rewardTransferResult = response.getData().getTx_hash();
                        System.out.println(response.getStatus() + " status_code");

                        body.put("tx_hash", response.getData().getTx_hash());
                        body.put("network", response.getData().getInfura());
                        body.put("status", "SUCCESS");

                        sendEventToLedger(body, context);

                        callbackHanpass.result(response.getData().getTx_hash());

                    } else {
                        body.put("message", response.getMessage());
                        body.put("network", response.getData().getInfura());
                        body.put("status", "FAILURE");
                        sendEventToLedger(body, context);

                        callbackHanpass.result(response.getMessage());
                    }
                }, error -> {
                    body.put("message", error.getMessage());

                    body.put("status", "FAILURE");
                    sendEventToLedger(body, context);

                    callbackHanpass.result(error.getMessage());
                    rewardTransferResult = error.getMessage();
                    System.out.println(rewardTransferResult);
                });


    }

    private HashMap<String, Object> deviceInfo(Context context) {
        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String osName = "ANDROID";
            String serialNumber = Build.SERIAL;
            String model = Build.MODEL;
            String manufacturer = Build.MANUFACTURER;
            HashMap<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("ID", androidId);
            deviceInfo.put("OS", osName);
            deviceInfo.put("MODEL", model);
            deviceInfo.put("SERIAL", serialNumber);
            deviceInfo.put("MANUFACTURER", manufacturer);
            return deviceInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isMainNet() {
        return mainnetInfuraUrl.contains("mainnet");
    }

    private void sendNFT(NFTbody nfTbody) {

    }

    public interface Callback<T> {
        void response(BaseResponse<T> res);
    }

    public interface CallbackHanpass {
        void result(String result);
    }
}