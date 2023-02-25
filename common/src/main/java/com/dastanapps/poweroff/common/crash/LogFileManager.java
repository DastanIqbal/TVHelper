package com.dastanapps.poweroff.common.crash;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ruand on 2017/4/27.
 * 日志文件管理类
 */

public class LogFileManager {
    private static final String LOG_DIR = "apm_log";//日志目录名
    private static final String BUG_LOG_FILE_NAME = "BugLog.txt";//异常日志文件名
    private static String LOG_PATH;//日志根路径

    /**
     * 创建日志文件根目录
     *
     * @return
     */
    public static void createLogDir(Context context) {
        String path = context.getFilesDir().getPath() + "/" + LOG_DIR + "/";
        boolean isSDCardExist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        boolean isRootDirExist = Environment.getExternalStorageDirectory().exists();
        if (isSDCardExist && isRootDirExist) {
            path = context.getExternalFilesDir(null) + "/" + LOG_DIR + "/";
        }
        //创建日志目录
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();
        LOG_PATH = path;
    }


    /**
     * 获取bug日志文件
     */
    public static File getBugLogFile() {
        File dest = new File(LOG_PATH + BUG_LOG_FILE_NAME);
        if (!dest.exists()) {
            try {
                dest.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dest;
    }

    /**
     * 删除bug日志文件
     */
    public static void delBugLogFile() {
        File logFile = getBugLogFile();
        if (logFile.exists()) {
            logFile.delete();
        }

    }


    public static String readLogFileContent(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" + sb;
    }

    /**
     * 写文件，从文件开始处写
     *
     * @param sb 写入内容
     * @return 写入成功
     */
    public static boolean write(String sb, File dest) {
        Log.i("buglog", "write");
        boolean result = false;
        FileChannel dstChannel = null;
        try {
            dstChannel = new FileOutputStream(dest).getChannel();
            dstChannel.write(ByteBuffer.wrap(sb.getBytes()));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        try {
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("buglog", "write result->" + result);
        return result;
    }

    /**
     * 写文件,在文件原内容下添加
     *
     * @param sb 写入内容
     * @return 写入成功
     */
    public static boolean writeAdd(String sb, File dest) {
        boolean result = false;
        FileChannel dstChannel = null;
        try {
            dstChannel = new FileOutputStream(dest, true).getChannel();
            dstChannel.write(ByteBuffer.wrap(sb.getBytes()));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        try {
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}