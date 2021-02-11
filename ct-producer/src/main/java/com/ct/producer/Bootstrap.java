package com.ct.producer;

import com.ct.common.bean.Producer;
import com.ct.producer.bean.LocalFileProducer;
import com.ct.producer.io.LocalFileDataIn;
import com.ct.producer.io.LocalFileDataOut;

import java.io.IOException;

/*
* 启动对象
* */
public class Bootstrap {
    public static void main(String[] args) throws IOException {
        if(args.length<2){
            System.out.println("no arguments");
            System.exit(1);
        }
        //构建生产者对象
        Producer producer = new LocalFileProducer();
        producer.setIn(new LocalFileDataIn(args[0]));
        producer.setOut(new LocalFileDataOut(args[1]));
        //生产数据
        producer.produce();

        //关闭生产者对象
        producer.close();
    }
}
