package com.cosplay.wang.fingerprintlogin;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.cosplay.wang.fingerprintlogin.util.CryptoObjectUtils;
import com.cosplay.wang.fingerprintlogin.util.SPUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Author:wangzhiwei on 2018/11/6.
 * Email :wangzhiwei@moyi365.com
 * Description:指纹识别帮助类
 */
@TargetApi(23)
public class FingerPrintHelper extends FingerprintManager.AuthenticationCallback {
    private FingerprintManager fingerManager;
    private CancellationSignal cancellationSignal;
    private KeyguardManager keyguardManager;
    Context context;
    int purpose;

    public interface FingerCallBack {
        void success(String token);

        void error(int errorCode, CharSequence errString);

        void failed();

        void help(int helpCode, CharSequence helpString);
    }

    FingerCallBack fingerCallBack;

    public void setFingerCallBack(FingerCallBack fingerCallBack) {
        this.fingerCallBack = fingerCallBack;
    }

    public FingerPrintHelper(Context context) {
        this.context = context;
        fingerManager = getFingerprintManager(context);
        keyguardManager = getKeyguardManager(context);
    }

    /**
     * 开始验证
     */
    public void startAuthFinger(int purpose) {
        this.purpose = purpose;
        if (isDeviceSupportFinger() == 4) {
            cancellationSignal = new CancellationSignal();
            byte[] IV = null;
            if (purpose != KeyProperties.PURPOSE_ENCRYPT) {
                IV = Base64.decode(SPUtils.getString(context, "iv"), Base64.URL_SAFE);
            }
            FingerprintManager.CryptoObject cryptoObject = CryptoObjectUtils.getCryptoObject(purpose, IV);
            if (cryptoObject != null) {
                fingerManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
            }else{
                onAuthenticationError(1,"还没有录入指纹");
            }

        }
    }

    /**
     * 取消验证
     */
    public void cancelAuthFinger() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
    }

    /**
     * 获取设备指纹manager对象
     *
     * @param context
     * @return
     */
    @TargetApi(23)
    public FingerprintManager getFingerprintManager(Context context) {
        FingerprintManager fingerprintManager = null;
        try {
            fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Throwable e) {
            Log.e("finger", "have not class FingerprintManager");
        }
        return fingerprintManager;
    }

    /**
     * 获取设备锁
     *
     * @param context
     * @return
     */
    public KeyguardManager getKeyguardManager(Context context) {
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager;
    }

    /**
     * 设备是否支持指纹
     *
     * @return
     */
    private int isDeviceSupportFinger() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerManager.isHardwareDetected()) {
                if (keyguardManager.isKeyguardSecure()) {
                    if (fingerManager.hasEnrolledFingerprints()) {
                        return 4;
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (fingerCallBack != null) {
            fingerCallBack.error(errorCode, errString);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        if (fingerCallBack != null) {
            fingerCallBack.help(helpCode, helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (fingerCallBack != null) {
            Cipher cipher = result.getCryptoObject().getCipher();
            if (cipher != null) {
                if (purpose != KeyProperties.PURPOSE_ENCRYPT) {//验证 解密信息
                    byte[] IV = Base64.decode(SPUtils.getString(context, "iv"), Base64.URL_SAFE);
                    String pingerToken = SPUtils.getString(context, "fingertoken");
                    if (TextUtils.isEmpty(pingerToken) || IV == null) {
                        onAuthenticationFailed();
                        return;
                    }
                    try {
                        byte[] original = cipher.doFinal(Base64.decode(pingerToken, Base64.URL_SAFE));
                        String originalString = new String(original, "utf-8");
                        fingerCallBack.success(originalString);
                    } catch (Exception e) {
                        e.printStackTrace();
                        onAuthenticationFailed();
                    }
                } else {
                    try {//把需要加密的信息 通过cipher加密
                        byte[] encrypted = cipher.doFinal(FingerPrintActivity.token.getBytes());
                        byte[] IV = cipher.getIV();
                        String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                        String siv = Base64.encodeToString(IV, Base64.URL_SAFE);
                        SPUtils.setString(context, "fingertoken", se);
                        SPUtils.setString(context, "iv", siv);
                        fingerCallBack.success(se);
                    } catch (BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                        onAuthenticationFailed();
                    }

                }
            }

        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (fingerCallBack != null) {
            fingerCallBack.failed();
        }
    }
}
