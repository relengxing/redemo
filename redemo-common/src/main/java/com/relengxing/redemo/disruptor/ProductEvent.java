package com.relengxing.redemo.disruptor;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * @author chaoli
 * @date 2023-11-18 09:20
 * @Description
 **/
@Data
public class ProductEvent {
    private String uid;
    private Long startTime;

}
