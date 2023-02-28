package com.dastanapps.poweroff.common.crash;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Iqbal Ahmed on 25/02/2023 9:07 PM
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static String TAG = "BUGLOG";
    private boolean isExFirstTime = true;//是否第一次发生异常，避免记录普通异常引发的ANR
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler; // 系统默认的UncaughtException处理类
    private static CrashHandler instance = new CrashHandler();
    private String mNetStatus;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    /**
     * init CrashHandler with an appliction object
     *
     * @param context application
     */
    public void init(Context context) {
        mContext = context;
        // get default UncaughtException handler
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // set CrashHandler as default UncaughtException handler
        Thread.setDefaultUncaughtExceptionHandler(this);

    }

    /**
     * when UncaughtException happens this method will handle it
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // if the exception has not been handle ，the default handler will handle it
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            SystemClock.sleep(1000);
            // exit
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    // celloct device infos
    private Map<String, String> mInfos = new HashMap<String, String>();

    /**
     * this method will handle and report exception
     *
     * @param ex
     * @return true:如果处理了该异常信息; 否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        if (isExFirstTime) {
            isExFirstTime = false;
            ex.printStackTrace();//report exception
            try {
                // warm user with toast
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Log.e(TAG, "Restarting Log");
                        Looper.loop();
                    }
                }.start();
                // 收集设备参数信息
                mInfos = DeviceInfoCollecter.collectDeviceInfo(mContext);
                // 收集设备状态信息
                mNetStatus = DeviceInfoCollecter.getNetwork(mContext);
                // 保存日志文件
                saveCrashInfoFile(ex);
                SystemClock.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * 保存错误信息到文件中
     *
     * @param ex 上下文
     * @throws Exception
     */
    private void saveCrashInfoFile(Throwable ex) throws Exception {
        StringBuffer sb = new StringBuffer();
        try {
            //日期
            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            sb.append("\r\n" + "date：" + date + "\n");
            //设备信息
            sb.append("----deviceInfo----" + "\n");
            if (mInfos != null) {
                for (Map.Entry<String, String> entry : mInfos.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    sb.append(key + "=" + value + "\n");
                }
            }
            //网络状态
            sb.append("\r\n" + "----netState----" + "\n");
            sb.append("networkState =" + mNetStatus + "\n");

            //崩溃信息
            sb.append("\r\n" + "----crashInfo----" + "\n");
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            LogFileManager.write(sb + "", LogFileManager.getBugLogFile());

        } catch (Exception e) {
            sb.append("an error occured while writing file...\r\n");
            LogFileManager.write(sb + "", LogFileManager.getBugLogFile());
        }

    }


}
