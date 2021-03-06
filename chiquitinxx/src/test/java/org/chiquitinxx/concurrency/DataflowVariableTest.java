package org.chiquitinxx.concurrency;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ExecutionException;

import static org.chiquitinxx.concurrency.Task.task;
import static org.junit.Assert.assertEquals;

/**
 * Created by jorge on 15/05/14.
 */
@RunWith(JUnit4.class)
public class DataflowVariableTest {

    volatile String text;
    volatile boolean done;

    @Test
    public void testMultipleVariables() throws InterruptedException, ExecutionException {
        for (int i = 0; i < 1000; i++) {
            assertEquals(calc(), 200);
        }
    }

    private int calc() throws InterruptedException, ExecutionException {
        DataflowVariable<Integer> initialDistance = new DataflowVariable<>();
        DataflowVariable<Integer> acceleration = new DataflowVariable<>();
        DataflowVariable<Integer> time = new DataflowVariable<>();
        task(() -> {
            initialDistance.set(100);
            acceleration.set(2);
            time.set(10);
        });

        int timeByTime = time.get() * time.get();
        int result = initialDistance.get() + acceleration.get() / 2 * timeByTime;
        return result;
    }

    @Test
    public void testWhenBound() {
        DataflowVariable<String> dv = new DataflowVariable<>();
        dv.whenBound((newValue) -> {
            System.out.println("New value is:"+newValue);
            text = "New value is:" + newValue;
        });
        task(() -> dv.set("VALUE")).join();
        assertEquals("New value is:VALUE", text);
    }

    @Test
    public void testThen() throws InterruptedException {
        done = false;
        DataflowVariable<String> dv = new DataflowVariable<>();
        dv.then(value -> value.toUpperCase()).
            then(value -> value + value).
            then(value -> {
                text = "New value is:" + value;
                return value;
            }).then( value -> {
                assertEquals("New value is:VALUEVALUE", text);
                done = true;
                return value;
            });
        dv.set("value");
        Thread.sleep(20);
        assertEquals(done, true);
    }

    volatile int total;
    void addToTotal(Integer amount) {
        total += amount;
    }

    @Test
    public void testAllListenValue() throws InterruptedException {
        total = 0;
        int number = 100;
        DataflowVariable<Integer> dv = new DataflowVariable<>();

        for (int i = 0; i < number; i++) {
            task(() -> {
                try {
                    addToTotal(dv.get());
                } catch (Exception e) {
                    System.out.println("Exception message: " + e.getMessage());
                }
            }).then(() -> addToTotal(1));
        }
        dv.set(3);
        Thread.sleep(300);
        assertEquals(number * 4, total);
    }
}
