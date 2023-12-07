package com.relengxing.redemo;

import cn.hutool.core.thread.NamedThreadFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.relengxing.redemo.disruptor.ProductEvent;
import com.relengxing.redemo.disruptor.ProductEventFactory;
import com.relengxing.redemo.disruptor.ProductEventHandler;
import com.relengxing.redemo.disruptor.ProductEventProducer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

/**
 * @author chaoli
 * @date ${YEAR}-${MONTH}-${DAY} ${HOUR}:${MINUTE}
 * @Description
 **/
public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("demo1 start =============");
        demo1();
        System.out.println("demo1 end =============");
//        System.out.println("demo2 start =============");
//        demo2();
//        System.out.println("demo2 end =============");
    }


    public static void demo1() throws InterruptedException {
        ThreadFactory threadFactory = new NamedThreadFactory("customer-pool-", false);
        //单生产者模式，指定ProducerType.SINGLE，性能会快一点
        Disruptor<ProductEvent> disruptor = new Disruptor<>(new ProductEventFactory(), 4, threadFactory, ProducerType.MULTI, new SleepingWaitStrategy());

        //单消费者模式
        disruptor.handleEventsWith(new ProductEventHandler("消费者1",2000L), new ProductEventHandler("消费者2"));

        //启动
        disruptor.start();

        RingBuffer<ProductEvent> ringBuffer = disruptor.getRingBuffer();
        Long startTime = System.currentTimeMillis();
        long index1 = ringBuffer.next();
        long index2 = ringBuffer.next();
        long index3 = ringBuffer.next();
        long index4 = ringBuffer.next();
        ProductEvent event1 = ringBuffer.get(index1);
        event1.setUid("event1");
        event1.setStartTime(startTime);
        ProductEvent event2 = ringBuffer.get(index2);
        event2.setUid("event2");
        event2.setStartTime(startTime);
        ProductEvent event3 = ringBuffer.get(index3);
        event3.setUid("event3");
        event3.setStartTime(startTime);
        ProductEvent event4 = ringBuffer.get(index4);
        event4.setUid("event4");
        event4.setStartTime(startTime);

//        Thread.sleep(1000);
        ringBuffer.publish(index3);
//        Thread.sleep(2000);
        ringBuffer.publish(index1);
//        Thread.sleep(3000);
        ringBuffer.publish(index2);
//        Thread.sleep(4000);
        ringBuffer.publish(index4);

        //终止
        disruptor.shutdown();
    }

    public static void demo2() throws InterruptedException {
        ThreadFactory threadFactory = new NamedThreadFactory("customer-pool-", false);
        //单生产者模式，指定ProducerType.SINGLE，性能会快一点
        Disruptor<ProductEvent> disruptor = new Disruptor<>(new ProductEventFactory(), 4, threadFactory, ProducerType.MULTI, new SleepingWaitStrategy());

        //单消费者模式
        disruptor.handleEventsWith(new ProductEventHandler("消费者1"));

        //启动
        disruptor.start();

        RingBuffer<ProductEvent> ringBuffer = disruptor.getRingBuffer();

        Long startTime = System.currentTimeMillis();
        long index1 = ringBuffer.next();
        long index2 = ringBuffer.next();
        long index3 = ringBuffer.next();
        long index4 = ringBuffer.next();
        ProductEvent event1 = ringBuffer.get(index1);
        event1.setUid("event1");
        event1.setStartTime(startTime);
        ProductEvent event2 = ringBuffer.get(index2);
        event2.setUid("event2");
        event2.setStartTime(startTime);
        ProductEvent event3 = ringBuffer.get(index3);
        event3.setUid("event3");
        event3.setStartTime(startTime);
        ProductEvent event4 = ringBuffer.get(index4);
        event4.setUid("event4");
        event4.setStartTime(startTime);
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "event1";
        }).thenAccept(str -> {
            ringBuffer.publish(index1);
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "event2";
        }).thenAccept(str -> {
            ringBuffer.publish(index2);
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "event3";
        }).thenAccept(str -> {
            ringBuffer.publish(index3);
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "event4";
        }).thenAccept(str -> {
            ringBuffer.publish(index4);
        });

        //终止
        disruptor.shutdown();
    }
}
