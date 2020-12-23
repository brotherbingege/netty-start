package com.aden.netty.nettyproto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yb
 * @date 2020/12/23 10:36
 */
@Data
public final class Header {
    /**
     * 消息的验证码：
     * 0xabef 固定值，表明该消息是Netty协议消息，1字节
     * 主版本号：1-255 1个字节 例：01
     * 次版本号：1-255 1个字节 例：01
     *
     * crcCode = 0xabef +主版本号 + 次版本号
     */
    private int crcCode = 0xabef0101;
    /**
     * 消息长度（消息头和消息体）
     */
    private int length;
    /**
     * 会话ID（全局唯一，由会话ID生成器生成）
     */
    private long sessionID;
    /**
     * 消息类型
     * 0：业务请求消息
     * 1：业务响应消息
     * 2：业务 ONE WAY 消息（既是请求又是响应消息）
     * 3：握手请求消息
     * 4：握手应答消息
     * 5：心跳请求消息
     * 6：心跳应答消息
     */
    private byte type;
    /**
     * 消息优先级 0-255
     */
    private byte priority;
    /**
     * 扩展消息头
     */
    private Map<String,Object> attachment = new HashMap<>();


    static class MessageType{
        public static final int LOGIN_REQ = 3;
        public static final int LOGIN_RESP = 4;
//         * 消息类型
//     * 0：业务请求消息
//     * 1：业务响应消息
//     * 2：业务 ONE WAY 消息（既是请求又是响应消息）
//                * 3：握手请求消息
//     * 4：握手应答消息
//     * 5：心跳请求消息
//     * 6：心跳应答消息
    }
}
