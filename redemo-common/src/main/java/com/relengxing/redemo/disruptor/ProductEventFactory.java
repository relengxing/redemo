package com.relengxing.redemo.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author chaoli
 * @date 2023-11-18 09:35
 * @Description
 **/
public class ProductEventFactory implements EventFactory<ProductEvent> {
    @Override
    public ProductEvent newInstance() {
        return new ProductEvent();
    }
}

