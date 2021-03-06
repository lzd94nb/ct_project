package com.ct.common.bean;

import com.ct.common.api.Column;
import com.ct.common.api.Rowkey;
import com.ct.common.api.TableRef;
import com.ct.common.constant.Names;
import com.ct.common.constant.ValueConstant;
import com.ct.common.util.DateUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 基础数据访问对象
 */
public abstract class BaseDao {

    private ThreadLocal<Connection> connHolder = new ThreadLocal<Connection>();
    private ThreadLocal<Admin> adminHolder = new ThreadLocal<Admin>();

    protected void start() throws IOException {
        getConnection();
        getAdmin();
    }

    protected void end() throws IOException {
        Admin admin = getAdmin();
        if(admin!=null){
            admin.close();
            adminHolder.remove();
        }
        Connection conn = getConnection();
        if(conn!=null){
            conn.close();
            connHolder.remove();
        }
    }

    /**
     * 创建表
     * @param name
     * @param families
     */
    protected void createTableXX(String name,String... families) throws IOException {
        createTableXX(name,null,null,families);
    }
    protected void createTableXX(String name,String coprocessorClass,Integer regionCount,String... families) throws IOException {
        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        if(admin.tableExists(tableName)){
            //表存在，删除表
            deleteTable(name);
        }
        //创建表
        createTable(name,coprocessorClass,regionCount,families);
    }

    private void createTable(String name,String coprocessorClass,Integer regionCount,String... families) throws IOException {
        Admin admin = getAdmin();
        TableName tableName = TableName.valueOf(name);
        HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
        if(families==null||families.length==0){
            families= new String[1];
            families[0]= Names.CF_INFO.getValue();
        }
        for (String family : families) {
            HColumnDescriptor columnDescriptor = new HColumnDescriptor(family);
            tableDescriptor.addFamily(columnDescriptor);
        }

        if(coprocessorClass!=null&&!"".equals(coprocessorClass)){
            tableDescriptor.addCoprocessor(coprocessorClass);
        }



        //增加预分区
        if(regionCount==null||regionCount<=1){
            admin.createTable(tableDescriptor);
        }
        else {
            //分区键
            byte[][] splitKeys=genSplitKeys(regionCount);
            admin.createTable(tableDescriptor,splitKeys);
        }
    }

    /**
     * 获取查询时startrow和stoprow集合
     * @param tel
     * @param start
     * @param end
     * @return
     */
    protected List<String[]> getStartStopRowkeys(String tel,String start,String end){
        List<String[]> rowkeyss = new ArrayList<String[]>();
        String startTime = start.substring(0,6);
        String endTime = end.substring(0,6);

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(DateUtil.parse(startTime,"yyyyMM"));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(DateUtil.parse(endTime,"yyyyMM"));

        while(startCal.getTimeInMillis()<=endCal.getTimeInMillis()){
            //当前时间
            String nowTime = DateUtil.format(startCal.getTime(),"yyyyMM");
            int regionNum = genRegionNum(tel,nowTime);

            String startRow  = regionNum+"_"+tel+"_"+nowTime;
            String stopRow  = startRow+"|";
            String[] rowkeys = {startRow,stopRow};

            rowkeyss.add(rowkeys);
            //月份+1
            startCal.add(Calendar.MONTH,1);
        }
        return rowkeyss;
    }

    /**
     * 计算分区号
     * @param tel
     * @param date
     * @return
     */
    protected  int genRegionNum(String tel,String date){
        String usercode = tel.substring(tel.length()-4);
        String yearMonth = date.substring(0,6);
        int userCodeHash = usercode.hashCode();
        int yearMonthHash = yearMonth.hashCode();
        //crc校验异或算法
        int crc = Math.abs(userCodeHash ^ yearMonthHash);
        //取模
        int regionNum = crc % ValueConstant.REGION_COUNT;
        return regionNum;
    }

    /**
     * 生成分区键
     * @return
     */
    private byte[][] genSplitKeys(int regionCount){
        int splitkeyCount=regionCount-1;
        byte[][] bs = new byte[splitkeyCount][];
        List<byte[]> bsList = new ArrayList<byte[]>();
        for (int i = 0; i < splitkeyCount; i++) {
            String splitkey = i+"|";
            bsList.add(Bytes.toBytes(splitkey));
        }
        bsList.toArray(bs);
        return bs;
    }

    /**
     * 增加对象，自动封装数据
     * @param obj
     */
    protected void putData(Object obj) throws IOException, IllegalAccessException {

        //反射
        Class aClazz = obj.getClass();
        TableRef tableRef = (TableRef) aClazz.getAnnotation(TableRef.class);
        String tableName=tableRef.value();

        Field[] fs = aClazz.getDeclaredFields();
        String stringRowkey = "";
        for (Field f : fs) {
            Rowkey rowkey = f.getAnnotation(Rowkey.class);
            if(rowkey!=null){
                f.setAccessible(true);
                stringRowkey=(String)f.get(obj);
                break;
            }
        }

        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(stringRowkey));
        for (Field f : fs) {
            Column column = f.getAnnotation(Column.class);
            if(column!=null){
                String family=column.family();
                String colName = column.column();
                if(colName==null|| "".equals(colName)){
                    colName=f.getName();
                }
                f.setAccessible(true);
                String value = (String)f.get(obj);
                put.addColumn(Bytes.toBytes(family),Bytes.toBytes(colName),Bytes.toBytes(value));
            }
        }
        table.put(put);
        table.close();
    }

    /**
     * 增加多条数据
     * @param name
     * @param puts
     */
    protected void putData(String name, List<Put> puts) throws IOException {
        //获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));
        //增加数据
        table.put(puts);
        //关闭表
        table.close();
    }


    /**
     * 增加数据
     * @param name
     * @param put
     */
    protected void putData(String name, Put put) throws IOException {
        //获取表对象
        Connection conn = getConnection();
        Table table = conn.getTable(TableName.valueOf(name));
        //增加数据
        table.put(put);
        //关闭表
        table.close();
    }

    /**
     * 删除表
     * @param name
     * @throws IOException
     */
    protected void deleteTable(String name) throws IOException {
        TableName tableName = TableName.valueOf(name);
        Admin admin = getAdmin();
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    /**
     * 创建命名空间
     * @param namespace
     */
    protected void createNamespaceNX(String namespace) throws IOException {
        Admin admin = getAdmin();
        try {
            admin.getNamespaceDescriptor(namespace);
        }catch (NamespaceNotFoundException e){
            NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(namespace).build();
            admin.createNamespace(namespaceDescriptor);
        }

    }

    /**
     * 获取连接对象
     */
    protected synchronized Connection getConnection() throws IOException {
        Connection conn = connHolder.get();
        if(conn==null){
            Configuration conf = HBaseConfiguration.create();
            conn = ConnectionFactory.createConnection(conf);
            connHolder.set(conn);
        }
        return conn;
    }

    /**
     * 获取连接对象
     */
    protected synchronized Admin getAdmin() throws IOException {
        Admin admin = adminHolder.get();
        if(admin==null){
            admin=getConnection().getAdmin();
            adminHolder.set(admin);
        }
        return admin;
    }

}
