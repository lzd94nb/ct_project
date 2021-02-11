package com.ct.consumer;

import com.ct.common.bean.Consumer;
import com.ct.consumer.bean.CalllogConsumer;

import java.io.IOException;

public class Bootstrap {
    public static void main(String[] args) throws IOException {

        //创建消费者
        Consumer consumer = new CalllogConsumer();

        //消费数据
        consumer.consume();

        //关闭资源
        consumer.close();

    }
}
