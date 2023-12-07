package com.relengxing.redemo.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chaoli
 * @date 2023-11-18 09:39
 * @Description
 **/
@Slf4j
public class ProductEventHandler implements EventHandler<ProductEvent>, WorkHandler<ProductEvent> {

    private String name;
    private Long sleep;

    public ProductEventHandler(String name) {
        this.name = name;
        this.sleep = 1000L;
    }

    public ProductEventHandler(String name,Long sleep) {
        this.name = name;
        this.sleep = sleep;
    }

    @Override
    public void onEvent(ProductEvent event, long l, boolean b) throws Exception {
        log.info("广播模式，线程{}消费者开始处理event:{}!", name, event.getUid());

        long time = System.currentTimeMillis() - event.getStartTime();
        System.out.println(time + "  消费者" + name + "打印uid:" + event.getUid());
        Thread.sleep(sleep);

        log.info("广播模式，线程{}消费者处理event完毕!", Thread.currentThread().getName());
    }

    @Override
    public void onEvent(ProductEvent productEvent) throws Exception {
        log.info("集群模式，线程{}消费者开始处理event:{}!", name, productEvent.getUid());

        System.out.println(System.currentTimeMillis() + "  消费者" + Thread.currentThread().getName() + "打印uid:" + productEvent.getUid());
        //保证生产快于消费
        Thread.sleep(sleep);

        log.info("集群模式，线程{}消费者处理event完毕!", name);
    }

}
