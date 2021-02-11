package com.ct.producer.io;

import com.ct.common.bean.DataOut;

import java.io.*;

public class LocalFileDataOut implements DataOut {
    private PrintWriter writer=null;

    public LocalFileDataOut(String path){
        setPAth(path);
    }
    public void setPAth(String path) {
        try {
            writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(Object data) throws Exception {
        write(data.toString());
    }

    /**
     * 将数据字符串生成到文件
     * @param data
     * @throws Exception
     */
    public void write(String data) throws Exception {
        writer.println(data);
        writer.flush();
    }

    /**
     * 关闭资源
     * @throws IOException
     */
    public void close() throws IOException {
        if(writer!=null){
            writer.close();
        }
    }
}
