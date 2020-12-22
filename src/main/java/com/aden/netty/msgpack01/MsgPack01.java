package com.aden.netty.msgpack01;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yb
 * @date 2020/12/21 18:01
 */
public class MsgPack01 {
    public static void main(String[] args) throws Exception{
        List<String> ls = new ArrayList<>();
        ls.add("AA");
        ls.add("BB");
        ls.add("CC");
        ls.add("DD");

        //编码（类似 output 流）
        MessagePack pack = new MessagePack();
        final byte[] write = pack.write(ls);

        //解码（类似input 流）
        final List<String> read = pack.read(write, Templates.tList(Templates.TString));
        System.out.println(read);
    }
}
