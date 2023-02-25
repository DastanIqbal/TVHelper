package com.dastanapps.poweroff.common.crash;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * ANR处理器
 */
@SuppressWarnings("UnusedDeclaration")
public class ANRHandler extends Thread {

    public interface ANRListener {
        public void onAppNotResponding(ANRError error);
    }

    public interface InterruptionListener {
        public void onInterrupted(InterruptedException exception);
    }


    private static final int DEFAULT_ANR_TIMEOUT = 5000;

    //默认ANR监听器
    private static final ANRListener DEFAULT_ANR_LISTENER = new ANRListener() {
        @Override
        public void onAppNotResponding(ANRError error) {
            throw error;
        }
    };

    //默认线程中断监听器
    private static final InterruptionListener DEFAULT_INTERRUPTION_LISTENER = new InterruptionListener() {
        @Override
        public void onInterrupted(InterruptedException exception) {
            Log.w(TAG, "Interrupted: " + exception.getMessage());
        }
    };

    private ANRListener mAnrListener = DEFAULT_ANR_LISTENER;//ANR监听器
    private InterruptionListener mInterruptionListener = DEFAULT_INTERRUPTION_LISTENER;//线程中断监听器
    private static final String TAG = "ANRHandler";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final int mTimeoutInterval;//ANR超时时间

    private String mNamePrefix = null;//线程名前缀
    private boolean mLogThreadsWithoutStackTrace = false;
    private boolean mIgnoreDebugger = false;//不忽略调试器

    private volatile int mTick = 0;//
    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            mTick = (mTick + 1) % Integer.MAX_VALUE;
        }
    };


    public ANRHandler() {
        this(DEFAULT_ANR_TIMEOUT);
    }

    public ANRHandler(int timeoutInterval) {
        super();
        mTimeoutInterval = timeoutInterval;
    }

    /**
     * 设置ANR监听器
     * 如果为空，则设置默认的监听器
     *
     * @param listener ANR监听器
     * @return ANR处理器
     */
    public ANRHandler setANRListener(ANRListener listener) {
        if (listener == null) {
            mAnrListener = DEFAULT_ANR_LISTENER;
        } else {
            mAnrListener = listener;
        }
        return this;
    }

    /**
     * 设置线程中断监听器
     * 如果为空，则设置默认线程中断监听
     *
     * @param listener 线程中断监听器
     * @return ANR处理器
     */
    public ANRHandler setInterruptionListener(InterruptionListener listener) {
        if (listener == null) {
            mInterruptionListener = DEFAULT_INTERRUPTION_LISTENER;
        } else {
            mInterruptionListener = listener;
        }
        return this;
    }

//    /**
//     * 设置线程名的前缀
//     * 打印报告时，作为线程的完整名称
//     * 默认为空
//     *
//     * @param prefix 线程被打印时的前缀名
//     * @return ANR处理器
//     */
//    public ANRHandler setReportThreadNamePrefix(String prefix) {
//        if (prefix == null)
//            prefix = "";
//        mNamePrefix = prefix;
//        return this;
//    }

//    /**
//     * Set that only the main thread will be reported.
//     *
//     * @return itself for chaining.
//     */
//    public ANRHandler setReportMainThreadOnly() {
//        mNamePrefix = null;
//        return this;
//    }
//
//    /**
//     * 设置所有正在运行的线程（包括没有堆栈跟踪的线程）信息都被打印
//     * 默认不打印
//     *
//     * @param logThreadsWithoutStackTrace 是否打印所有运行中的线程信息
//     * @return ANR处理器
//     */
//    public ANRHandler setLogThreadsWithoutStackTrace(boolean logThreadsWithoutStackTrace) {
//        mLogThreadsWithoutStackTrace = logThreadsWithoutStackTrace;
//        return this;
//    }

    /**
     * 设置是否忽略调试器
     * 选择忽略处理器时, 即使处于debug模式，ANR处理器依然监听ANR
     * 默认情况不忽略
     *
     * @param ignoreDebugger 否忽略调试器
     * @return ANR处理器
     */
    public ANRHandler setIgnoreDebugger(boolean ignoreDebugger) {
        mIgnoreDebugger = ignoreDebugger;
        return this;
    }

    @Override
    public void run() {
        setName("|ANRHandler|");
        int lastTick;
        int lastIgnored = -1;
        while (!isInterrupted()) {
            lastTick = mTick;
            mainHandler.post(mTicker);//向主线程发送消息 计数器值将改变
            try {
                Thread.sleep(mTimeoutInterval);//监听线程等待片刻（超时时间）
            } catch (InterruptedException e) {
                mInterruptionListener.onInterrupted(e);
                return;
            }

            // 如果计数器值没变化，说明发生了ANR
            if (mTick == lastTick) {
                if (!mIgnoreDebugger && Debug.isDebuggerConnected()) {//当前处于debug模式
                    if (mTick != lastIgnored)
                        Log.w(TAG, "An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))");
                    lastIgnored = mTick;
                    continue;//debug模式下跳出循环，不打印ANR日志
                }
                ANRError error;
//                if (mNamePrefix != null) {
//                    //添加线程名前缀
//                   // error = ANRError.New(mNamePrefix, mLogThreadsWithoutStackTrace);
//                    error = ANRError.NewMainOnly();
//                } else
                    //主线程ANR
                    error = ANRError.NewMainOnly();
                mAnrListener.onAppNotResponding(error);
                return;
            }
        }
    }

}