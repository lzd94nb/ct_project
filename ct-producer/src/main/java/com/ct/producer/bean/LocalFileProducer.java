package com.ct.producer.bean;

import com.ct.common.bean.DataIn;
import com.ct.common.bean.DataOut;
import com.ct.common.bean.Producer;
import com.ct.common.util.DateUtil;
import com.ct.common.util.NumberUtil;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
* 本地数据文件生产者
* */
public class LocalFileProducer implements Producer {

    private DataIn in;
    private DataOut out;
    private volatile boolean flg=true;
    public void setIn(DataIn in) {
        this.in=in;
    }

    public void setOut(DataOut out) {
        this.out=out;
    }

    /*
    * 生产数据
    * */
    public void produce() {

        try{
            //读取通讯录数据
            List<Contact> contacts= in.read(Contact.class);
            /*for (Contact contact : contacts) {
                System.out.println(contact);
            }*/
            while (flg){
            //从通讯录中随机查找2个电话号码
                int call1Index=new Random().nextInt(contacts.size());
                int call2Index;
                while (true){
                    call2Index=new Random().nextInt(contacts.size());
                    if(call1Index!=call2Index){
                        break;
                    }
                }

                Contact call1=contacts.get(call1Index);
                Contact call2=contacts.get(call2Index);

                //生成随机通话时间
                String startDate="20180101000000";
                String endDate="20190101000000";
                long  startTime= DateUtil.parse(startDate,"yyyyMMddHHmmss").getTime();
                long endTime=DateUtil.parse(endDate,"yyyyMMddHHmmss").getTime();

                //通话时间
                long callTime=startTime+(long)((endTime-startTime)* Math.random());

                //通话时间字符串
                String callTimeString = DateUtil.format(new Date(callTime),"yyyyMMddHHmmss");
                //生成随机通话时长
                 String duration = NumberUtil.format(new Random().nextInt(3000),4);

                 //生成通话记录
                CallLog log = new CallLog(call1.getTel(), call2.getTel(), callTimeString, duration);

                System.out.println(log);
                //将通话记录刷写到数据文件
                out.write(log);
                Thread.sleep(500);
             }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
    * 关闭生产者
    * */
    public void close() throws IOException {
        if(in!=null){
            in.close();
        }
        if(out!=null){
            out.close();
        }
    }
}
