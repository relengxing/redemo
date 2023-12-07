package com.relengxing.redemo;

import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import jdk.jfr.EventFactory;
import lombok.extern.slf4j.Slf4j;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author chaoli
 * @date ${YEAR}-${MONTH}-${DAY} ${HOUR}:${MINUTE}
 * @Description
 **/
@Slf4j
public class Main {
    public static void main(String[] args) {
//        Long totalLine = 1024 * 1024L;


        // 创建RingBuffer的大小，必须是2的幂次方
        int bufferSize = 1024;

        // 创建线程池
        Executor executor = Executors.newFixedThreadPool(4);

        // 创建Disruptor
        Disruptor<Event> disruptor = new Disruptor<>(eventFactory, bufferSize, executor);

        // 创建事件处理器
        EventHandler<Event> eventHandler = (event, sequence, endOfBatch) -> {
            // 处理事件的逻辑
            System.out.println("消费事件：" + event.getData());
        };

        // 将事件处理器注册到Disruptor中
        disruptor.handleEventsWith(eventHandler);

        // 启动Disruptor
        disruptor.start();

        // 获取RingBuffer
        RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();

        // 发布事件
        for (int i = 0; i < 10; i++) {
            long sequence = ringBuffer.next();
            Event event = ringBuffer.get(sequence);
            event.setData("Event " + i);
            ringBuffer.publish(sequence);
        }

        // 关闭Disruptor
        disruptor.shutdown();
    }


    // 定义事件类
    static class Event {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }


    public static class ProductEventFactory implements EventFactory<ProductEvent> {
        @Override
        public ProductEvent newInstance() {
            return new ProductEvent();
        }
    }

}
