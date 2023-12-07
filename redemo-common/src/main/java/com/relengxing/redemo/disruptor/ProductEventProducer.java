package com.relengxing.redemo.disruptor;

import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chaoli
 * @date 2023-11-18 09:35
 * @Description
 **/
@Slf4j
public class ProductEventProducer {

    private final RingBuffer<ProductEvent> ringBuffer;

    public ProductEventProducer(RingBuffer<ProductEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(String uid) {
        // ringBuffer是个队列，其next方法返回的是最后一条记录之后的位置，即可用位置
        long sequence = ringBuffer.next();
        log.info("开始生产,uid={}", uid);
        try {
            // sequence位置取出的事件是空事件
            ProductEvent event = ringBuffer.get(sequence);
            // 空事件添加业务信息
            event.setUid(uid);
        } finally {
            // 发布
            ringBuffer.publish(sequence);
        }
    }


}
