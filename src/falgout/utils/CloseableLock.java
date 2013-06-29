package falgout.utils;

import java.util.concurrent.locks.Lock;

public class CloseableLock {
	public static void lock(Lock l) {
		l.lock();
	}
}