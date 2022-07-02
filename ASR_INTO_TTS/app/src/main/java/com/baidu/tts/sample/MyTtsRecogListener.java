package com.baidu.tts.sample;

import android.os.Handler;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.tts.sample.control.MySyntherizer;


public class MyTtsRecogListener extends MessageStatusRecogListener {
    private MySyntherizer mySyntherizer;
    public MyTtsRecogListener(Handler handler,MySyntherizer mySyntherizer ){
        super(handler);
        this.mySyntherizer = mySyntherizer;
    }

    //识别成功
    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        mySyntherizer.speak(results[0]);
    }
}
