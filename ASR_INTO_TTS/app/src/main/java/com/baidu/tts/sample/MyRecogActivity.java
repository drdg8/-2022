package com.baidu.tts.sample;

import android.content.Intent;
import android.os.Bundle;

import com.baidu.aip.asrwakeup3.core.mini.ActivityMiniRecog;
import com.baidu.speech.asr.SpeechConstant;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

public class MyRecogActivity extends ActivityMiniRecog {
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;

        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            // 识别相关的结果都在这里
            if (params == null || params.isEmpty()) {
                return;
            }
            if (params.contains("\"nlu_result\"")) {
                // 一句话的语义解析结果
                if (length > 0 && data.length > 0) {
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            } else if (params.contains("\"partial_result\"")) {
                // 一句话的临时识别结果
                logTxt += ", 临时识别结果：" + params;
            }  else if (params.contains("\"final_result\""))  {
                // 一句话的最终识别结果

                logTxt += ", 最终识别结果：" + params;
                Intent intent = new Intent(MyRecogActivity.this, SynthActivity.class);
                JSONObject json= null;
                try {
                    json = new JSONObject(params);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String s=json.optString("results_recognition");
                intent.putExtra("word",s);
                startActivity(intent);
            }  else {
                // 一般这里不会运行
                logTxt += " ;params :" + params;
                if (data != null) {
                    logTxt += " ;data length=" + data.length;
                }
            }
        } else {
            // 识别开始，结束，音量，音频数据回调
            if (params != null && !params.isEmpty()){
                logTxt += " ;params :" + params;
            }
            if (data != null) {
                logTxt += " ;data length=" + data.length;
            }
        }


        printLog(logTxt);
    }
}