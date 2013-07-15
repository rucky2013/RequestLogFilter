package com.qly.b2b.servlet.filter.requestlog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WrapperResponse extends HttpServletResponseWrapper { 
    public static final int  OT_NONE = 0, OT_WRITER = 1, OT_STREAM = 2; 
    private int outputType =  OT_NONE; 
    private ServletOutputStream  output = null; 
    private PrintWriter writer =  null; 
    private ByteArrayOutputStream  buffer = null; 

    /*
     * 构造函数 
     */
    public  WrapperResponse(HttpServletResponse resp) throws IOException { 
        super(resp);  
        buffer  = new ByteArrayOutputStream(); 
    } 

    /*
     * 得到字符输出流
      *2013-6-3下午5:39:13
      * by  guop
      * TODO
     */
    public PrintWriter getWriter() throws IOException { 
        if  (outputType == OT_STREAM) 
            throw  new IllegalStateException(); //已经用了OutputStream流
        else  if (outputType == OT_WRITER) 
            return  writer; 
        else  { 
            outputType  = OT_WRITER; 
            writer  = new PrintWriter(new  OutputStreamWriter(buffer, getCharacterEncoding())); 
            return  writer; 
        } 
    }

    /*
     * 得到字节输出流
      *2013-6-3下午5:39:22
      * by  guop
      * TODO
     */
    public ServletOutputStream getOutputStream() throws IOException { 
        if  (outputType == OT_WRITER) 
            throw  new IllegalStateException(); //已经用了Writer流
        else  if (outputType == OT_STREAM) 
            return  output; 
        else  { 
            outputType  = OT_STREAM; 
            output  = new WrappedOutputStream(buffer); 
            return  output; 
        } 
    } 

    /*
     * 刷新输出内容
      *2013-6-3下午5:39:29
      * by  guop
      * TODO
     */
    public void flushBuffer() throws IOException { 
        if  (outputType == OT_WRITER) 
            writer.flush();  
        if  (outputType == OT_STREAM) 
            output.flush();  
    } 

    /*
     * 输出缓冲区复位
      *2013-6-3下午5:39:33
      * by  guop
      * TODO
     */
    public void reset() { 
        outputType  = OT_NONE; 
        buffer.reset();  
    } 
    
    /**
     * getResponseData(按JVM默认编码获取响应内容) 
     * (这里描述这个方法适用条件 – 可选) 
     * @return
     * @throws IOException  
     * String 
     * @exception
     */
    public byte[]  getResponseData() throws IOException { 
        flushBuffer();  
        return buffer.toByteArray(); 
    } 

    /*
     * 内部类，将数据写入自己的定义的缓冲区
      *2013-6-3下午5:39:37
      * by  guop
      * WrappedOutputStream
      * TODO
     */
    class WrappedOutputStream extends ServletOutputStream { 
            private  ByteArrayOutputStream buffer; 
            public  WrappedOutputStream(ByteArrayOutputStream buffer) { 
                this.buffer  = buffer; 
            }  
            public  void write(int b) throws IOException { 
                buffer.write(b);  
            }  
            public  byte[] toByteArray() { 
                return  buffer.toByteArray(); 
            }  
    } 
} 

