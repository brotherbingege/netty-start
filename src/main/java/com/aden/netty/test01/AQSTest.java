package com.aden.netty.test01;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author yb
 * @date 2020/12/21 14:08
 */
public class AQSTest {
    private Node tail;
    public static void main(String[] args) throws Exception{
        AQSTest test = new AQSTest();
        test.tail = new Node();
        //拿到上一个tail节点
        Node pred = test.tail;
        Node node = new Node(null,null,1);
        //当前新节点的pre 指向上一个节点的tail
        node.pre = pred;

        long tailOffset = getUnsafe().objectFieldOffset
                (AQSTest.class.getDeclaredField("tail"));
        //交换tail 和 node
        getUnsafe().compareAndSwapObject(test, tailOffset, pred, node);

        //上一个节点的next -> node
        pred.next = node;
        System.out.println(test.tail);
        System.out.println(node);
        System.out.println(test.tail.next);
        System.out.println(test.tail.pre);
    }
    static class Node{
        Node pre;
        Node next;
        int data;

        public Node(){

        }
        public Node(Node pre,Node next,int data){
            this.pre = pre;
            this.next = next;
            this.data = data;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "pre=" + pre +
                    ", next=" + next +
                    ", data=" + data +
                    '}';
        }
    }

    public static Unsafe getUnsafe() throws Exception{
        Field unsafeField = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        return unsafe;
    }
}
