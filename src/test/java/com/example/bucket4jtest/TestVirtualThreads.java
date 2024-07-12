package com.example.bucket4jtest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;


@Slf4j
@Import(TestcontainersConfiguration.class)
@SpringBootTest

//-Djdk.tracePinnedThreads=full
//-agentpath:libasyncProfiler.so=start,event=cpu,alloc=2m,lock=10ms,loop=1m,jfrsync=+jdk.VirtualThreadStart+jdk.VirtualThreadEnd+jdk.VirtualThreadPinned+jdk.VirtualThreadSubmitFailed,file=profile-%t.jfr
public class TestVirtualThreads {


    @Test
    void testVt() throws InterruptedException {
        int count = 100000;
        var cdl = new CountDownLatch(count);
        var obj = new Object(); // something to lock

        for (int i = 0; i < count; i++) {
            Thread.ofVirtual().name("vt" + i).start(() -> {
                synchronized (obj) {
                    log.info(Thread.currentThread().getName() + "(" + Thread.currentThread().threadId() + ")" + ": " + Thread.currentThread().isVirtual());
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                cdl.countDown();
            });
        }

        cdl.await();
    }
}
