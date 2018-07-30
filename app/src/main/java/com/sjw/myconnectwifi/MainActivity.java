package com.sjw.myconnectwifi;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.sjw.myconnectwifi.utils.RuntimeRationale;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Setting;

import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 操作，先连接一个wifi，再用这个软件登录另外一个wifi，就会改变wifi
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText ssidEditText;
    private EditText passwordEditText;
    private Button saveBtn;
    private WifiUtil wifiUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        wifiUtil = new WifiUtil(this);
        wifiUtil.startScan();
        for (WifiConfiguration configuration :
                wifiUtil.getConfigurations()) {
//            ssidEditText.setText(configuration.SSID);
            Log.e("configuration", "ssid:" + configuration.SSID + "--id:" + configuration.networkId +
                    "--priority" + configuration.priority + "--allowedAuthAlgorithms:" + configuration.allowedAuthAlgorithms +
                    "--allowedGroupCiphers:" + configuration.allowedGroupCiphers + "--allowedKeyManagement:" + configuration.allowedKeyManagement +
                    "--allowedAuthAlgorithms:" + configuration.allowedAuthAlgorithms
                    + "--allowedPairwiseCiphers:" + configuration.allowedPairwiseCiphers
                    + "--hiddenSSID:" + configuration.hiddenSSID
                    + "--wepTxKeyIndex:" + configuration.wepTxKeyIndex
                    + "--wepKeys:" + configuration.wepKeys[0]
                    + "--preSharedKey" + configuration.preSharedKey
                    + "--status:" + configuration.status);
        }
    }


    private void initView() {
        ssidEditText = (EditText) findViewById(R.id.edit_ssid);
        passwordEditText = (EditText) findViewById(R.id.eidt_password);
        saveBtn = (Button) findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission(Permission.Group.LOCATION);
            }
        });
    }


    /**
     * Request permissions.
     */
    private void requestPermission(String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        connectWifi();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
//                        toast(R.string.failure);
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            showSettingDialog(MainActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    private void connectWifi() {

        //                wifiUtil.mWifiManager.enableNetwork(20, true);
//                wifiUtil.mWifiManager.saveConfiguration();
//                wifiUtil.mWifiManager.reconnect();
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getEditableText().toString().trim();

        if (!JudgeNetwork.isWifiConnected(MainActivity.this)) {

            Toast.makeText(MainActivity.this, "请连接wifi", Toast.LENGTH_SHORT).show();
            return;
        }

        for (WifiConfiguration c : wifiUtil.getConfigurations()) {
            wifiUtil.mWifiManager.disableNetwork(c.networkId);
        }
        for (WifiConfiguration configuration :
                wifiUtil.getConfigurations()) {

            String scanSsid = configuration.SSID;

            if (configuration.SSID.startsWith("\"") && configuration.SSID.endsWith("\"")) {
                scanSsid = configuration.SSID.substring(1, configuration.SSID.length() - 1);
            }

            if (scanSsid.equals(ssid)) {
                wifiUtil.addNetWork(wifiUtil.createWifiInfo(ssid, password, password.length() == 0 ? 1 : 3));

                wifiUtil.mWifiManager.enableNetwork(configuration.networkId, true);
//                        wifiUtil.mWifiManager.
            }
//                        Boolean connectState = wifiUtil.connectConfiguration(configuration.networkId);
//                        if (connectState) {
//                            passwordEditText.setVisibility(View.GONE);
//                            Toast.makeText(MainActivity.this, "已经连接过了", Toast.LENGTH_SHORT).show();
//                            return;
//                        } else {
////                                    passwordEditText.setVisibility(View.VISIBLE);
//                            Toast.makeText(MainActivity.this, "还未连接过", Toast.LENGTH_SHORT).show();
////                                    return;
//                        }

//                                Log.e("configuration", configuration.SSID + configuration.networkId + "--" + configuration.priority);
//                                break;

        }
    }


    /**
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        Toast.makeText(MainActivity.this, R.string.message_setting_comeback, Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }
}
