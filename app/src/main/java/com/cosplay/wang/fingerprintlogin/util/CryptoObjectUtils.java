package com.cosplay.wang.fingerprintlogin.util;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyProperties;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * Author:wangzhiwei on 2018/11/6.
 * Email :wangzhiwei@moyi365.com
 * Description:
 */
public class CryptoObjectUtils {
    @TargetApi(23)
    public static FingerprintManager.CryptoObject getCryptoObject(int purpose, byte[] IV) {

        try {
            AndroidKeyStoreUtils androidKeyStoreUtils = new AndroidKeyStoreUtils();
            Key key = androidKeyStoreUtils.GetKey();
            if (key == null) {
                return null;
            }
            final Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC
                    + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            if (purpose == KeyProperties.PURPOSE_ENCRYPT) {
                cipher.init(purpose, key);
            } else {
                if(IV == null){
                    return null;
                }
                cipher.init(purpose, key, new IvParameterSpec(IV));
            }
            return new FingerprintManager.CryptoObject(cipher);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
