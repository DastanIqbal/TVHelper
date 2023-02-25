package com.dastanapps.poweroff.common.crash;

import android.os.Looper;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * ANR错误
 */
public class ANRError extends Error {

    private static class ANRThrowableInfo implements Serializable {
        private final String mName;
        private final StackTraceElement[] mStackTrace;

        private class ANRThrowable extends Throwable {
            private ANRThrowable(ANRThrowable other) {
                super(mName, other);
            }

            @Override
            public Throwable fillInStackTrace() {
                setStackTrace(mStackTrace);
                return this;
            }
        }

        private ANRThrowableInfo(String name, StackTraceElement[] stackTrace) {
            mName = name;
            mStackTrace = stackTrace;
        }
    }

    private static final long serialVersionUID = 1L;

    private ANRError(ANRThrowableInfo.ANRThrowable st) {
        super("Application Not Responding", st);
    }

    @Override
    public Throwable fillInStackTrace() {
        setStackTrace(new StackTraceElement[]{});
        return this;
    }

    static ANRError New(String prefix, boolean logThreadsWithoutStackTrace) {
        final Thread mainThread = Looper.getMainLooper().getThread();
        final Map<Thread, StackTraceElement[]> stackTraces = new TreeMap<>(new Comparator<Thread>() {
            @Override
            public int compare(Thread lhs, Thread rhs) {
                if (lhs == rhs)
                    return 0;
                if (lhs == mainThread)
                    return 1;
                if (rhs == mainThread)
                    return -1;
                return rhs.getName().compareTo(lhs.getName());
            }
        });

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet())
            if (
                    entry.getKey() == mainThread || (entry.getKey().getName().startsWith(prefix) && (logThreadsWithoutStackTrace || entry.getValue().length > 0))
            )
                stackTraces.put(entry.getKey(), entry.getValue());

        // Sometimes main is not returned in getAllStackTraces() - ensure that we list it
        if (!stackTraces.containsKey(mainThread)) {
            stackTraces.put(mainThread, mainThread.getStackTrace());
        }

        ANRThrowableInfo.ANRThrowable tst = null;
        for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
            tst = new ANRThrowableInfo(getThreadTitle(entry.getKey()), entry.getValue()).new ANRThrowable(tst);
        }

        return new ANRError(tst);
    }

    static ANRError NewMainOnly() {
        final Thread mainThread = Looper.getMainLooper().getThread();
        final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();
        return new ANRError(new ANRThrowableInfo(getThreadTitle(mainThread), mainStackTrace).new ANRThrowable(null));
    }

    private static String getThreadTitle(Thread thread) {
        return thread.getName() + " (state = " + thread.getState() + ")";
    }
}