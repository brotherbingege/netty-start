package com.aden.netty.nettyproto;

import lombok.Data;

/**
 * Netty
 * @author yb
 * @date 2020/12/23 10:33
 */
@Data
public final class NettyMessage {
    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体
     */
    private Object body;

}
