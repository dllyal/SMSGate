package com.dllyal.cmpp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 短信接口辅助工具类
 * @author
 */
public class CmppUtils {

    static Logger logger = LoggerFactory.getLogger(CmppUtils.class);

    //序列编号起始值
    private static int sequenceId=  0;
    //序列峰值
    private static int MAX_VALUE=Integer.MAX_VALUE/2;


    /**
     * 序列 redis自增
     */
    /*public static int getSequence(){
        int seqId = 0;
        try {
            Long exp = RedisUtil.incr(Constants.DEMO, Constants.CMPP_SEQ_REDIS_KEY);
            if(exp >= 1846000000){
                RedisUtil.del(Constants.DEMO, Constants.CMPP_SEQ_REDIS_KEY);
            }
            seqId = exp.intValue();
        } catch (Exception e) {
            logger.error("CMPP序列号生产失败",e);

            //Redis异常，则随机产生ID
            int orderId= UUID.randomUUID().toString().hashCode();
            orderId = Math.abs(orderId)/10;
            Long timeSeqIdTemp = System.currentTimeMillis();
            timeSeqIdTemp = timeSeqIdTemp - timeSeqIdTemp / 100000000 * 100000000;
            int timeSeqId = Math.abs(timeSeqIdTemp.intValue());
            seqId = orderId + timeSeqId + 1846000000;
        }
        return seqId;
    }*/

    /**
     * 序列 自增
     */
    public static int getSequence(){
        ++sequenceId;
        if(sequenceId>MAX_VALUE){
            sequenceId=Math.abs(new Long(System.currentTimeMillis()).intValue());
        }
        return sequenceId;
    }

    //序列编号
    //private static int sequenceId=0;

    /**
     * 序列 自增
     */
    /*public static int getSequence(){
        ++sequenceId;
        if(sequenceId>255){
            sequenceId=0;
        }
        return sequenceId;
    }*/

    /**
     * 时间戳的明文,由客户端产生,格式为MMDDHHMMSS，即月日时分秒，10位数字的整型，右对齐 。
     */
    public static String  getTimestamp(){
        DateFormat format=new SimpleDateFormat("MMddhhmmss");
        return format.format(new Date());
    }

    /**
     * 用于鉴别源地址。其值通过单向MD5 hash计算得出，表示如下：
     * AuthenticatorSource =
     * MD5（Source_Addr+9 字节的0 +shared secret+timestamp）
     * Shared secret 由中国移动与源地址实体事先商定，timestamp格式为：MMDDHHMMSS，即月日时分秒，10位。
     * @return
     */
    public static byte[] getAuthenticatorSource(String spId,String secret){
        try {
            MessageDigest md5=MessageDigest.getInstance("MD5");
            byte[] data=(spId+"000000000"+secret+ CmppUtils.getTimestamp()).getBytes();
            System.out.println("----------------------------------");
            for (int i = 0; i < data.length; i++) {
                System.out.println("==data["+i+"]："+data[i]);
            }
            
            System.out.println("----------------------------------");
            byte[] data2=("3"+spId+"000000000"+secret+ CmppUtils.getTimestamp()+"secret").getBytes();
            for (int i = 0; i < data.length; i++) {
                System.out.println("==data2["+i+"]："+data2[i]);
            }
            System.out.println("----------------------------------");
            return md5.digest(data);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SP链接到ISMG拼接AuthenticatorSource失败："+e.getMessage());
            return null;
        }
    }
    
    public static byte[] md5(String spid,String password) {
        byte sp[] = spid.getBytes();
        byte bzero[] = new byte[9];
        byte[] bSPpassword = password.getBytes();

        byte btimestamp[] = (CmppUtils.getTimestamp()).getBytes();
        byte bmd5[] = new byte[sp.length + 9 + bSPpassword.length
                + btimestamp.length];
        int cur = 0;
        System.arraycopy(sp, 0, bmd5, cur, sp.length);
        cur += sp.length;
        System.arraycopy(bzero, 0, bmd5, cur, 9);
        cur += bzero.length;
        System.arraycopy(bSPpassword, 0, bmd5, cur, bSPpassword.length);
        cur += bSPpassword.length;
        System.arraycopy(btimestamp, 0, bmd5, cur, btimestamp.length);
        byte[] result = new byte[16];
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(bmd5);
            result = md.digest();
            //logger.info("md5散列码：" + bytes2hex(result));
            //System.out.println("md5散列码：" + bytes2hex(result));
        } catch (NoSuchAlgorithmException e) {
            logger.error("CmppUtils md5 error",e);
            //System.out.println(e.toString());
        }
        return result;
    }

    /**
     * 将字节数组转换为字符串
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    } 
    
    public static String bytes2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(byte2hex(b[i]));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String byte2hex(byte b) {
        char hex[] = new char[2];
        hex[0] = chars[(new Byte(b).intValue() & 0xf0) >> 4];
        hex[1] = chars[new Byte(b).intValue() & 0xf];
        return new String(hex);
    }

    private static final char chars[] = { '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * 字符串表示转成字节数组
     * @param hexString
     * @return 转换后的字节数组
     **/
    public static byte[] hexToBytes(String hexString) {
        char[] hex = hexString.toCharArray();
        // 转rawData长度减半
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            // 先将hex转10进位数值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            // 將第一個值的二進位值左平移4位,ex: 00001000 => 10000000 (8=>128)
            // 然后与第二个值的二进位值作联集ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            // 与FFFFFFFF作补集
            if (value > 127) {
                value -= 256;
            }
            // 最后转回byte就OK
            rawData[i] = (byte) value;
        }
        return rawData;
    }

    /**
     * 功能：
     *     将字节转换为16进制码（在此只是为了调试输出，此函数没有实际意义）
     * @param b
     * @return 转化后的16进制码
     * @Author: eric(eric_cheung709 @ hotmail.com)
     * created in 2007/04/28 16:33:06
     */
    public static String bytesToHexStr(byte[] b) {
        if (b == null) return "";
        StringBuffer strBuffer = new StringBuffer(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            strBuffer.append(Integer.toHexString(b[i] & 0xff));
            strBuffer.append(" ");
        }
        return strBuffer.toString();
    }

    /***
     * * 功能：
     *     将src里的字节与add里的从start开始到end（不包括第end个位置)的字节串连在一起返回
     * @param src
     * @param add
     * @param start    add 开始位置
     * @param end      add 的结束位置(不包括end位置)
     * @return 也即实现类似String类型的src+add.subString(start,end)功能
     */

    public static byte[] byteAddLongMsg(byte[] src, byte[] add, int start, int end) {
        byte[] dst = new byte[src.length + end - start];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
        }
        for (int i = 0; i < end - start; i++) {
            dst[src.length + i] = add[start + i];
        }
        return dst;

    }
    
    /**
     * 向流中写入指定字节长度的字符串，不足时补0
     * @param dous:要写入的流对象
     * @param s:要写入的字符串
     * @param len:写入长度,不足补0
     */
    public static void writeString(DataOutputStream dous,String s,int len){
        
        try {
            byte[] data=s.getBytes("gb2312");
            if(data.length>len){
                logger.info("向流中写入的字符串超长！要写"+len+" 字符串是:"+s);
                //System.out.println("向流中写入的字符串超长！要写"+len+" 字符串是:"+s);
            }
            int srcLen=data.length;
            dous.write(data);
            while(srcLen<len){
                dous.write(0x00);
                srcLen++;
            }
        } catch (IOException e) {
            logger.error("向流中写入指定字节长度的字符串失败",e);
            //System.out.println("向流中写入指定字节长度的字符串失败："+e.getMessage());
        }
    }
    
    /**
     * 从流中读取指定长度的字节，转成字符串返回
     * @param ins:要读取的流对象
     * @param len:要读取的字符串长度
     * @return:读取到的字符串
     */
    public static String readString(DataInputStream ins, int len){
        byte[] b=new byte[len];
        try {
            ins.read(b);
            String s=new String(b);
            s=s.trim();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 从流中读取指定长度的字节，转成字符串返回
     * @param ins:要读取的流对象
     * @param len:要读取的字符串长度
     * @return:读取到的字符�?
     */
    public static String readString(DataInputStream ins, int len, String charset) {
        byte[] b = new byte[len];
        try {
            ins.read(b);
            String s;
            if (charset == null)
                s = new String(b);
            else
                s = new String(b, charset);
            s = s.trim();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 从流中读取指定长度的字节，转成字符串返回
     * @param ins:要读取的流对象
     * @param len:要读取的字符串长度
     * @return:读取到的字符串
     */
    public static String readStringUntil(DataInputStream ins, int len, byte untilByte){
        byte[] b=new byte[len];
        try {
            ins.read(b);
            int index = 0;
            for (byte one : b){
                if (one == untilByte){
                    break;
                }
                index++;
            }
            byte[] c=new byte[index];
            System.arraycopy(b, 0, c, 0, index);
            String s=new String(c);
            s=s.trim();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 截取字节
     * @param msg
     * @param start
     * @param end
     * @return
     */
    public static byte[] getMsgBytes(byte[] msg,int start,int end){
        byte[] msgByte=new byte[end-start];
        int j=0;
        for(int i=start;i<end;i++){
            msgByte[j]=msg[i];
            j++;
        }       
        return msgByte;
    }

    /**  
     * UCS2解码  
     *   
     * @param src  
     *            UCS2 源串  
     * @return 解码后的UTF-16BE字符串  
     */  
    public static String DecodeUCS2(String src) {   
        byte[] bytes = new byte[src.length()/2];   
        for (int i = 0; i < src.length(); i += 2) {   
            bytes[i/2]=(byte)(Integer.parseInt(src.substring(i, i + 2), 16));   
        }   
        String reValue = "";   
        try {   
            reValue = new String(bytes, "UTF-16BE");   
        } catch (UnsupportedEncodingException e) {   
            reValue="";
        }   
        return reValue;   
      
    }   
      
    /**  
     * UCS2编码  
     *   
     * @param src  
     *            UTF-16BE编码的源串  
     * @return 编码后的UCS2串  
     */  
    public static String EncodeUCS2(String src) {   
        byte[] bytes;   
        try {   
            bytes = src.getBytes("UTF-16BE");   
        } catch (UnsupportedEncodingException e) {   
            bytes=new byte[0]; 
        }   
        StringBuffer reValue = new StringBuffer();   
        StringBuffer tem = new StringBuffer();   
        for (int i = 0; i < bytes.length; i++) {   
            tem.delete(0, tem.length());   
            tem.append(Integer.toHexString(bytes[i] & 0xFF));   
            if(tem.length()==1){   
                tem.insert(0,'0');   
            }   
            reValue.append(tem);   
        }   
        return reValue.toString().toUpperCase();   
    }


    public static byte[] getLenBytes(String s, int len) {
        if (s == null) {
            s = "";
        }
        byte[] rb = new byte[len];
        byte[] sb = s.getBytes();
        for (int i = sb.length; i < rb.length; i++) {
            rb[i] = 0;
        }
        if (sb.length == len) {
            return sb;
        } else {
            for (int i = 0; i < sb.length && i < len; i++) {
                rb[i] = sb[i];
            }
            return rb;
        }
    }


    /**
     * byte[] 转  long
     * 2016年9月30日
     */
    public static long bytesToLong(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i = 0; i < 8; i++) {
            temp = b[i] & 0xff;
            temp <<= 8*i;
            res |= temp;
        }
        return res;
    }

    public static int bytesToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] getInData(byte[] buf) {
        try{
            if(buf != null && buf.length >0){
                int len = CmppUtils.bytesToInt(ArrayUtils.subarray(buf, 0, 4));
                //logger.info("读取消息长度："+len);
                //System.out.println("读取消息长度："+len);
                //获取到该长度的字节数组
                byte[] returnData = ArrayUtils.subarray(ArrayUtils.subarray(buf, 0, len), 4, len);
                return returnData;
            }else {
                return null;
            }
        }catch(NullPointerException ef){
            logger.error("在本连结上接受字节消息:无流输入");
            //System.out.println("在本连结上接受字节消息:无流输入");
            return null;
        }
    }

    /**
     * int 转 byte[]
     * @param number
     * @return
     */
    public static byte[] toBytes(int number){
        byte[] bytes = new byte[4];
        bytes[0] = (byte)number;
        bytes[1] = (byte) (number >> 8);
        bytes[2] = (byte) (number >> 16);
        bytes[3] = (byte) (number >> 24);
        return bytes;
    }

    /**
     * byte[] 转 int
     * @param bytes
     * @return
     */
    public static int toInt(byte[] bytes){
        int number = 0;
        for(int i = 0; i < 4 ; i++){
            number += bytes[i] << i*8;
        }
        return number;
    }

    /**
     * byte转int类型
     * 如果byte是负数，则转出的int型是正数
     * @param b
     * @return
     */
    public static int byteToInt(byte b){
        //System.out.println("byte 是:"+b);
        int x = b & 0xff;
        //System.out.println("int 是:"+x);
        return x;
    }
    /**
     * int 类型转换为byte 类型
     * 截取int类型的最后8位,与 0xff
     * @param x
     * @return
     */
    public static byte intToByte(int x){
        //System.out.println("int 是:"+x);
        //System.out.println("int的二进制数据为:"+Integer.toBinaryString(x));
        byte b =(byte) (x & 0xff);
        //System.out.println("截取后8位的二进制数据为:"+Integer.toBinaryString(x & 0xff));
        //System.out.println("byte 是:"+b);
        return b;
    }

    /**
     * 去除号码前的86、+86
     * @param number
     * @return
     */
    public static String trimPhoneNum(String number) {
        String s = number;
        if (number.startsWith("86")) {
            s = number.substring("86".length());
        }
        if (number.startsWith("+86")) {
            s = number.substring("+86".length());
        }
        return s;
    }

    /*public static void main(String[] args) {
        //getStartSeqId();
        int count=30000;
        final CountDownLatch cdl=new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        int seqId = 0;
                        int orderId= UUID.randomUUID().toString().hashCode();
                        orderId = Math.abs(orderId)/10;
                        Long timeSeqIdTemp = System.currentTimeMillis();
                        timeSeqIdTemp = timeSeqIdTemp - timeSeqIdTemp/100000000*100000000;
                        int timeSeqId = Math.abs(timeSeqIdTemp.intValue());
                        seqId = orderId + timeSeqId + 1846000000;
                        //System.out.println("seqId:" + seqId);

                        FileWriter fw = null;
                        try {
                            //如果文件存在，则追加内容；如果文件不存在，则创建文件
                            File f=new File("D:\\bbb.txt");
                            fw = new FileWriter(f, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PrintWriter pw = new PrintWriter(fw);
                        pw.println(seqId);
                        pw.flush();
                        try {
                            fw.flush();
                            pw.close();
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }*/

}
