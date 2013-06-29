package falgout.utils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.locks.Lock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CloseableLockTest {
	private Lock mock;
	
	@Before
	public void init() {
		mock = mock(Lock.class);
	}
	
	@Test
	public void GettingAcquiresLock() {
		CloseableLock.lock(mock);
		verify(mock).lock();
	}
}