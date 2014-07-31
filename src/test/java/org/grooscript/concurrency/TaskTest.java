package org.grooscript.concurrency;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.grooscript.concurrency.Task.*;

/**
 * Created by jorge on 15/05/14.
 */
@RunWith(JUnit4.class)
public class TaskTest {

    int number;
    String info;

    @Test
    public void testExecuteTask() throws InterruptedException {
        number = 0;
        task((Runnable)() -> number = 1);
        assertEquals(0, number);
        Thread.sleep(50);
        assertEquals(1, number);
    }

    @Test
    public void testExecuteTaskWithThen() throws InterruptedException {
        number = 0;
        task((Runnable)() -> number = 3).then(() -> {
            number = number + 5;
        }).then(() -> number = number + 4);
        Thread.sleep(100);
        assertEquals(12, number);
    }

    @Test
    public void testExecuteTaskOnError() throws InterruptedException {
        number = 0;
        task((Runnable)() -> {
            List list = null;
            list.add(5);
        }).onError((t) -> {
            number = -1;
        });
        Thread.sleep(100);
        assertEquals(-1, number);
    }


    @Test
    public void testExecuteTaskThatReturnsFuture() throws Exception {
        Future result = task(() -> {
            Thread.sleep(10);
            return 5;
        });
        assertEquals(5, result.get());

        assertEquals("Hello world!", task(() -> "Hello world!").get());
    }

    @Test
    public void testWhenAllBound() throws Exception {
        info = "";
        DataflowVariable hello = new DataflowVariable();
        Future world = task(() -> {
            hello.set("Hello");
            return "World";
        });
        whenAllBound((values -> info = values[0] + " - " + values[1]), hello, world);
        Thread.sleep(50);
        assertEquals("Hello - World", info);
    }
}
