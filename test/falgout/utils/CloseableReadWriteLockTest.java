package falgout.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Scopes;

@RunWith(JukitoRunner.class)
public class CloseableReadWriteLockTest {
    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {
            bindMock(Lock.class).in(Scopes.NO_SCOPE);
        }
    }
    
    @Inject private ReadWriteLock l;
    @Inject private Lock read;
    @Inject private Lock write;
    
    @Before
    public void init() {
        when(l.readLock()).thenReturn(read);
        when(l.writeLock()).thenReturn(write);
    }
    
    @Test
    public void CallingReadLocksReadLock() {
        CloseableLock.read(l);
        
        verify(read).lock();
    }
    
    @Test
    public void CallingWriteLocksWriteLock() {
        CloseableLock.write(l);
        
        verify(write).lock();
    }
    
    @Test
    public void CallingReadInterruptiblyLocksReadLockInterruptibly() throws InterruptedException {
        CloseableLock.readInterruptibly(l);
        
        verify(read).lockInterruptibly();
    }
    
    @Test
    public void CallingWriteInterruptiblyLocksWritLockInterruptibly() throws InterruptedException {
        CloseableLock.writeInterruptibly(l);
        
        verify(write).lockInterruptibly();
    }
}
