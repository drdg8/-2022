package com.baidu.tts.sample;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import com.baidu.aip.asrwakeup3.core.mini.ActivityMiniWakeUp;
import com.baidu.aip.asrwakeup3.core.util.bluetooth.AndroidAudioManager;
import com.baidu.aip.asrwakeup3.core.wakeup.WakeUpResult;
import com.baidu.speech.EventManagerFactory;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.sample.control.InitConfig;
import com.baidu.tts.sample.control.MySyntherizer;
import com.baidu.tts.sample.control.NonBlockSyntherizer;
import com.baidu.tts.sample.listener.UiMessageListener;
import com.baidu.tts.sample.util.Auth;
import com.baidu.tts.sample.util.AutoCheck;
import com.baidu.tts.sample.util.FileUtil;
import com.baidu.tts.sample.util.IOfflineResourceConst;
import com.baidu.tts.sample.util.OfflineResource;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;


public class MyWakeUpActivity extends ActivityMiniWakeUp {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioManager mAudioManager = (AudioManager)getSystemService(Context. AUDIO_SERVICE);
        //mAudioManager.startBluetooth();
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();
        mAudioManager.setMode(2);
//        BluetoothUtil.start(this,BluetoothUtil.FULL_MODE); // 蓝牙耳机开始，注意一部分手机这段代码无效
        setContentView(com.baidu.aip.asrwakeup3.core.R.layout.common_mini);
        super.initView();
        super.initPermission();
        // 基于SDK唤醒词集成1.1 初始化EventManager
        super.wakeup = EventManagerFactory.create(this, "wp");
        // 基于SDK唤醒词集成1.3 注册输出事件
        super.wakeup.registerListener(this); //  EventListener 中 onEvent方法

//        AndroidAudioManager.getInstance(context).startBluetooth();
//        AndroidAudioManager.getInstance(context).routeAudioToBluetooth();
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyWakeUpActivity.this, SynthActivity.class));
            }
        });
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
            Intent intent = new Intent(MyWakeUpActivity.this, SynthActivity.class);
            JSONObject json= null;
            try {
                json = new JSONObject(params);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String s=json.optString("word");
            intent.putExtra("word",s);
            startActivity(intent);
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        printLog(logTxt);
    }
}