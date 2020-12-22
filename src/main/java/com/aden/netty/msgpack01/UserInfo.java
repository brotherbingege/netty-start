package com.aden.netty.msgpack01;

import org.msgpack.annotation.Message;

/**
 * @author yb
 * @date 2020/12/22 11:25
 */
@Message //不加这个注解会报错
public class UserInfo {
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
