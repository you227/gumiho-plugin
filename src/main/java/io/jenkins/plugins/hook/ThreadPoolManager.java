package io.jenkins.plugins.hook;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

	private static ThreadPoolExecutor executor;
	private static final int CORE_POOL_SIZE = 20;
	private static final int MAXIMUM_POOL_SIZE = 50;
	private static final int KEEP_ALIVE_TIME = 3;

	public static ThreadPoolExecutor getInstance() {
		if (executor == null) {
			synchronized (ThreadPoolManager.class) {
				if (executor == null) {
					executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
							TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
				}
			}
		}
		return executor;
	}
}
