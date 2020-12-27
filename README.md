# Hanpass Ethereum Client SDK 


## Table of Contents

- [Getting Started](#getting-started)
  - [Adding Ethereum SDK as a Maven or Gradle Dependency](#adding-ethereum-sdk-as-a-maven-or-gradle-dependency)
  - [Basic Usage](#basic-usage)
- [Features at a Glance](#features-at-a-glance)
  - [Create Wallet](#create-wallet)


## Getting Started

### Add jitpack to your root gradle file at the end of repositories:
```xml
allprojects {
    repositories {
	...
        maven { url 'https://jitpack.io'}
    }
}
```
### Adding Ethereum SDK as a Maven or Gradle Dependency
For “Maven” add this dependency to your module:

```xml
<dependency>
	    <groupId>com.github.centerprime</groupId>
	    <artifactId>Hanpass-Ethereum-SDK</artifactId>
	    <version>1.0.0</version>
</dependency>
```

For “Gradle” add this dependency to your module:

```groovy
dependencies {
    implementation 'com.github.centerprime:Hanpass-Ethereum-SDK:1.0.0'
}
```

### Basic Usage

Once you have the dependencies set up you can start using *CenterPrime* by creating a `Ethereum Wallet`:

```java
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.centerprime.ethereum_sdk.EthManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EthManager ethManager = EthManager.getInstance();

        String password = "xxxx12345";

        ethManager.createWallet(password, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallet -> {
                    String walletAddress = wallet.getAddress();
                    String keystore = wallet.getKeystore();
                }, error -> {

                });
    }
}
```

*Congratulations!* Now you are a *CenterPrime* user.

## Features at a Glance

### Create Wallet

> You can create Ethereum Wallet.
```java
EthManager ethManager = EthManager.getInstance();
String password = "xxxx12345";
ethManager.createWallet(password, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallet -> {
                    String walletAddress = wallet.getAddress();
                    String keystore = wallet.getKeystore();
                }, error -> {

                });
```

