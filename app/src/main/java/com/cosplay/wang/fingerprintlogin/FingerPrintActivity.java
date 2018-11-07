package com.cosplay.wang.fingerprintlogin;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

public class FingerPrintActivity extends Activity implements View.OnClickListener, FingerPrintHelper.FingerCallBack {
    private TextView tvCheckInput;
    private TextView tvCheck;
    private RelativeLayout rlFingerCheck;
    private TextView tvTitle;
    private ImageView imFinger;
    private TextView tvHint;
    private View viewLine;
    private TextView tvCancel;
    private int purpose;
    FingerPrintHelper fingerHelper;
    public static String token = "123456987654321";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        findViews();
        tvCheckInput.setOnClickListener(this);
        tvCheck.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

    }

    private void findViews() {
        tvCheckInput = (TextView) findViewById(R.id.tv_check_input);
        tvCheck = (TextView) findViewById(R.id.tv_check);
        rlFingerCheck = (RelativeLayout) findViewById(R.id.rl_finger_check);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        imFinger = (ImageView) findViewById(R.id.im_finger);
        tvHint = (TextView) findViewById(R.id.tv_hint);
        viewLine = (View) findViewById(R.id.view_line);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
    }

    /**
     * Request permissions.
     */
    private void requestPermission(String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        rlFingerCheck.setVisibility(View.VISIBLE);
                        initFinger();
                        //Toast.makeText(FingerPrintActivity.this, "获取权限成功" + permissions, Toast.LENGTH_SHORT).show();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                    }
                })
                .start();
    }

    void initFinger() {
        tvHint.setText("按压指纹传感器");
        fingerHelper = new FingerPrintHelper(this);
        fingerHelper.setFingerCallBack(this);
        fingerHelper.startAuthFinger(purpose);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_check_input: {
                purpose = KeyProperties.PURPOSE_ENCRYPT;
                requestPermission(new String[]{Manifest.permission.USE_FINGERPRINT});
                break;
            }
            case R.id.tv_check: {
                purpose = KeyProperties.PURPOSE_DECRYPT;
                requestPermission(new String[]{Manifest.permission.USE_FINGERPRINT});
                break;
            }
            case R.id.tv_cancel: {
                rlFingerCheck.setVisibility(View.GONE);
                if (fingerHelper != null) {
                    fingerHelper.cancelAuthFinger();
                }

                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (rlFingerCheck.getVisibility() == View.VISIBLE) {
            rlFingerCheck.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void success(String token) {
        if(token.equals(FingerPrintActivity.token)){
            Log.e("success","确认是该用户");
        }else{
            Log.e("success","确认不是开启指纹登陆的用户"); //正常走不到这里，前边就不会进指纹验证
        }
        tvHint.setText("验证通过:" + token);
    }

    @Override
    public void error(int errorCode, CharSequence errString) {
        tvHint.setText(errString);
        rlFingerCheck.setVisibility(View.GONE);
        Toast.makeText(FingerPrintActivity.this,errString,Toast.LENGTH_LONG).show();


    }

    @Override
    public void failed() {
        tvHint.setText("验证失败，请重试");
    }

    @Override
    public void help(int helpCode, CharSequence helpString) {

    }
}
