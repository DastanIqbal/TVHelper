package com.dastanapps.poweroff.common.crash;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.File;


/**
 * Created by ruand on 2017/4/25.
 * 异常日志管理器
 */

public class BugLog {

    private static BugLog instance = new BugLog();
    public static Context mContext;//上下文
    private OnCompleteTxtRead mOnCompleteTxtRead;
    private static final int COMPLETE_TXT_READ = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == COMPLETE_TXT_READ) {
                if (mOnCompleteTxtRead != null)
                    mOnCompleteTxtRead.setOnCompleteTxtRead((String) msg.obj);
            }
            return false;
        }
    });


    private BugLog() {
    }

    public static BugLog getInstance() {
        return instance;
    }

    /**
     * 初始化日志管理器
     *
     * @param context 上下文
     */
    public void start(Context context) {
        mContext = context;
        LogFileManager.createLogDir(context);//创建日志文件目录
        CrashHandler.getInstance().init(context);//初始化crash日志管理器
        new ANRHandler(2000).start();//监听ANR异常

    }

    /**
     * 删除异常日志
     */
    public void deleteBugLog() {
        LogFileManager.delBugLogFile();
    }

    /**
     * 获取异常日志
     */
    public File getBugLogFile() {
        return LogFileManager.getBugLogFile();
    }

    public void getLogFileContent(final File file, final OnCompleteTxtRead onCompleteTxtRead) {
        Log.i("BugLog", file.getName());
        mOnCompleteTxtRead = onCompleteTxtRead;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = LogFileManager.readLogFileContent(file);
                Message msg = mHandler.obtainMessage();
                msg.what = COMPLETE_TXT_READ;
                msg.obj = result;
                msg.sendToTarget();
            }
        }).start();
    }

    /**
     * txt读取完成接口
     */
    public interface OnCompleteTxtRead {
        public abstract void setOnCompleteTxtRead(String result);
    }

}