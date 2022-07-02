package com.baidu.tts.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.baidu.tts.sample.util.CalendarUtil;
import com.baidu.tts.sample.util.FileUtil;
import com.baidu.tts.sample.util.IOfflineResourceConst;
import com.baidu.tts.sample.util.OfflineResource;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 合成demo。
 * 离在线合成SDK含在线和离线模型，纯离线合成SDK额外含有纯离线功能。
 * <p>
 * <p>
 * TtsMode.MIX 离在线合成根据网络状况优先走在线，在线时访问服务器失败后转为离线。
 * TtsMode.ONLINE 一直在线合成
 * TtsMode.OFFLINE 纯离线合成，一直离线。需要纯离线SDK
 */
public class SynthActivity extends BaseActivity implements View.OnClickListener {

    // ================== 完整版初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected String sn; // 纯离线合成SDK授权码；离在线合成SDK免费，没有此参数

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； TtsMode.OFFLINE 纯离线合成，需要纯离线SDK
    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_OFFLINE_TTS_MODE;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_vXXXXXXX.dat为离线男声模型文件；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_vXXXXX.dat为离线女声模型文件;
    // assets目录下bd_etts_common_speech_yyjw_mand_eng_high_am-mix_vXXXXX.dat 为度逍遥模型文件;
    // assets目录下bd_etts_common_speech_as_mand_eng_high_am_vXXXX.dat 为度丫丫模型文件;
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    protected MyRecognizer myRecognizer;


    protected int descTextId = R.raw.sync_activity_description;

    private static final String TAG = "SynthActivity";

    final Handler handler = new Handler();
    private CalendarUtil CalendarReminderUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Auth.getInstance(this);
        } catch (Auth.AuthCheckException e) {
            mShowText.setText(e.getMessage());
            return;
        }
        mShowText.setText(FileUtil.getResourceText(this, descTextId));

        appId = Auth.getInstance(this).getAppId();
        appKey = Auth.getInstance(this).getAppKey();
        secretKey = Auth.getInstance(this).getSecretKey();
        sn = Auth.getInstance(this).getSn(); // 纯离线合成必须有此参数；离在线合成SDK免费，没有此参数
        initialButtons(); // 配置onclick
        initialTts(); // 初始化TTS引擎
        Log.i("SynthActivity", "so version:" + SynthesizerTool.getEngineInfo());
        //IRecogListener listener = new MessageStatusRecogListener(mainHandler);
        IRecogListener listener = new MyTtsRecogListener(mainHandler,synthesizer);
        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(this, listener);
        handler.postDelayed(new  Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                speak();
            }
        }, 500);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
//        start();
//        speak();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible
        // it is now "resumed"

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        //initialTts();
        //speak();
        //onRestart();
        //this.finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
// Another activity is taking focus
// this activity is about to be "paused"
        //speak();
    }

    protected void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new HashMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new com.baidu.aip.asrwakeup3.core.mini.AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    com.baidu.aip.asrwakeup3.core.mini.AutoCheck autoCheck = (com.baidu.aip.asrwakeup3.core.mini.AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        //txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    /**
     * 界面上的按钮对应方法
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.speak:

                speak(); // 合成并播放
                break;
            case R.id.synthesize:
                synthesize(); // 只合成不播放
                break;
            case R.id.batchSpeak:
                batchSpeak(); //  批量合成并播放
                break;
            case R.id.loadModel:
                // 切换离线资源

                AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
                builder.setTitle("引擎空闲时切换");
                final Map<String, String> map = new LinkedHashMap<>(4);
                map.put("离线女声", OfflineResource.VOICE_FEMALE);
                map.put("离线男声", OfflineResource.VOICE_MALE);
                map.put("离线度逍遥", OfflineResource.VOICE_DUXY);
                map.put("离线度丫丫", OfflineResource.VOICE_DUYY);
                final String[] keysTemp = new String[4];
                final String[] keys = map.keySet().toArray(keysTemp);
                builder.setItems(keys, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadModel(map.get(keys[which]));
                    }
                });
                builder.show();
                break;
            case R.id.pause:
                pause(); // 播放暂停
                break;
            case R.id.resume:
                resume(); // 播放恢复
                break;
            case R.id.stop:
                stop(); // 停止合成引擎
                break;
            case R.id.help:
                start();
                //mShowText.setText(FileUtil.getResourceText(this, descTextId));
                break;
            default:
                break;
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(this, config, mainHandler); // 此处可以改为MySyntherizer 了解调用过程

    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
        // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getParams();
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        if (sn == null) {
            initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
        }
        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        return initConfig;
    }


    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
    //查看、申请日历读写权限
    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void speak() {
        mShowText.setText("");
        //String text = mInput.getText().toString();
        String text = "主人好！";
        Intent it=getIntent();
        String word=it.getStringExtra("word");
        if (word.indexOf("家庭食谱")!=-1) {
            text="好的主人！根据您提供的一家三口的饮食偏好，这里为您智能推荐了两个家庭食谱。食谱1：婴儿南瓜粥、清炒藕片、番茄水煮鱼、胡萝卜黄瓜炒鸡肉。食谱2：婴儿玉米粥、红烧牛肉马铃薯、清炒油麦菜，干锅粉丝。其中南瓜粥有利于宝宝补钙，而玉米粥富含宝宝需要的维生素，请问主人想要选择哪一种呢？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 100+250*text.length());
        }
        else if (word.indexOf("帮我打开")!=-1){
            text="好的，这就帮您打开食灵。请问您今天想吃什么菜呢？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 100+250*text.length());
        }
        else if (word.indexOf("想吃番茄")!=-1){
            text="好的主人！帮您把番茄炒蛋加入今晚的食谱。这里还为您智能推荐了近日火爆的青椒肉丝菜谱，请问您需要吗？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 100+250*text.length());
        }
        else if (word.indexOf("老地方")!=-1){
            text="好的主人！已为您将今日菜谱的原料纳入日程提醒，祝您下厨愉快！";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 100+250*text.length());
        }
        else if (word.indexOf("番茄炒蛋")!=-1){
            text="好的，这就为您打开番茄炒蛋的最热门菜谱。请问您需要将要买的菜加入日历提醒吗？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 100+250*text.length());
        }

        else if (word.indexOf("加入日历")!=-1){
            text="好的，这就为您创建买菜日程及具体菜单。请问主人还有什么想吃的吗？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    String title="番茄炒蛋菜谱";
                    String descript="番茄 700g ， 鸡蛋 3 个。";
                    final int callbackId = 42;
                    checkPermissions(callbackId, Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR);
                    CalendarReminderUtils.addCalendarEvent(SynthActivity.this,title,descript,System.currentTimeMillis()+15*60*1000,0);
                    //startCalendarEvent(title,descript,"常去的果蔬店",false,5);//设置日历事件的用法
                    finish();
                }
            }, 200+250*text.length());
        }
        else if (word.indexOf("休息")!=-1){
            text="好的主人，有事请随时呼叫我，再见！";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 280+200*text.length());
        }
        else if (word.indexOf("叮咚买菜")!=-1){
            text="没问题。正在帮您购买食谱1的原料。祝您回家路上一路顺风！";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 280+250*text.length());
        }
        else{
            text="抱歉主人，小艺没有听清您刚刚说的话，能够请您再说一遍吗？";
            int result = synthesizer.speak(text);
            checkResult(result, "speak");
            handler.postDelayed(new  Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finish();
                }
            }, 200+250*text.length());
        }

        //startCalendarEvent("test_title","test_description","locate",false,5);//设置日历事件的用法
//        int result = synthesizer.speak(text);
//        checkResult(result, "speak");
//        handler.postDelayed(new  Runnable() {
//            @Override
//            public void run() {
//                // Do something after 5s = 5000ms
//                finish();
//            }
//        }, 2000);
    }

    private void startCalendarEvent(String title, String description, String location, boolean isAllDay,  long duration){
        //duration is in minute

        Intent intent = new Intent(Intent.ACTION_INSERT);
        //long remindTime = 0;
        Calendar mCalender = Calendar.getInstance();
        //mCalender.setTimeInMillis(remindTime);
        long startTime = mCalender.getTime().getTime();
        //startTime += 15*60*1000;
        mCalender.setTimeInMillis(startTime + duration*60*1000);
        long endTime = mCalender.getTime().getTime();
        intent.setData(CalendarContract.Events.CONTENT_URI);
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        intent.putExtra(CalendarContract.Events.DTSTART,startTime);
        intent.putExtra(CalendarContract.Events.DTEND,endTime);
        intent.putExtra(CalendarContract.Events.TITLE,title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION,description);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION,location);
        intent.putExtra(CalendarContract.Events.ALL_DAY, isAllDay);
        intent.putExtra(Intent.EXTRA_EMAIL,"");
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }else{
            Toast.makeText(this,"There is no app that can support the action",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private void synthesize() {
        mShowText.setText("");
        String text = this.mInput.getText().toString();
        if (TextUtils.isEmpty(mInput.getText())) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
            mInput.setText(text);
        }
        int result = synthesizer.synthesize(text);
        checkResult(result, "synthesize");
    }

    /**
     * 批量播放
     */
    private void batchSpeak() {
        mShowText.setText("");
        List<Pair<String, String>> texts = new ArrayList<>();
        texts.add(new Pair<>("开始批量播放，", "a0"));
        texts.add(new Pair<>("123456，", "a1"));
        texts.add(new Pair<>("欢迎使用百度语音，，，", "a2"));
        texts.add(new Pair<>("重(chong2)量这个是多音字示例", "a3"));
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }


    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private void loadModel(String mode) {
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        toPrint("切换离线语音：" + offlineResource.getModelFilename());
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method);
        }
    }


    /**
     * 暂停播放。仅调用speak后生效
     */
    private void pause() {
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private void resume() {
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stop() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }


    @Override
    protected void onDestroy() {
        myRecognizer.release();
        synthesizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

    protected void handle(Message msg) {
        if (msg.what == INIT_SUCCESS) {
            for (Button b : buttons) {
                b.setEnabled(true);
            }
            msg.what = PRINT;
        }
        super.handle(msg);
    }

    private void initialButtons() {
        for (Button b : buttons) {
            b.setOnClickListener(this);
            b.setEnabled(false); // 先禁用按钮，等待引擎初始化后打开。
        }
        mHelp.setOnClickListener(this);
    }

}
