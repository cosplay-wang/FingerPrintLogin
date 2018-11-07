package com.cosplay.wang.fingerprintlogin.util;

import android.annotation.TargetApi;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.Key;
import java.security.KeyStore;

import javax.crypto.KeyGenerator;

/**
 * Author:wangzhiwei on 2018/11/7.
 * Email :wangzhiwei@moyi365.com
 * Description:AndroidKeyStore帮助类
 */
public class AndroidKeyStoreUtils {
    KeyStore keyStore;
    public static final String keyName = "key";
    public static final String keyPassword = "password";
    public static final String KEYSTORE_NAME = "AndroidKeyStore";
    public static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    public AndroidKeyStoreUtils() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Key GetKey() throws Exception {
        Key secretKey;
        if (!keyStore.isKeyEntry(keyName)) {
            CreateKey();
        }
        secretKey = keyStore.getKey(keyName, keyPassword.toCharArray());
        return secretKey;
    }

    @TargetApi(23)
    public void CreateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
        KeyGenParameterSpec keyGenSpec =
                new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true)
                        .build();
        keyGen.init(keyGenSpec);
        keyGen.generateKey();
    }

}
