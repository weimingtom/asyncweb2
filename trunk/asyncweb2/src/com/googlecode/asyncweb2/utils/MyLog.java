package com.googlecode.asyncweb2.utils;
/****************************************************************
*	CopyLeft: 请遵守GPL版权许可；同时保留所有文字和注释；	*
*****************************************************************
 * Created on 2005-7-12
 * @author liusheng<nike.lius@gmail.com>
 * 日志辅助类，功能如下：
 * 1，动态配置log4j配置文件；
 * 2，隔离commons-log和slf4j差异，方便切换；
 * 3，定义logMpsp()业务日志
 * 20090205 修改：UtilLog > MyLog
 * 20090205 修改：便于在commons-log和slf4j之间切换
 * 20090205 修改：参考play.Logger进行修改，支持自定义参数；
 * 20090224 增加 logMpsp()业务日志
 * 20090313 采用方案3（直接使用log4j）
 * @see http://zeroliu.iteye.com/blog/326595

public interface MyLogInf {  
    public void debug(String message, Object... args);  
    public void info(String message, Object... args);  
    public void warn(String message, Object... args);  
    public void error(Throwable e, String message, Object... args);  
    public boolean isDebugEnabled();//仅仅用于打印辅助调试信息（而在debug()中已经，无需单独代码调用）  
}  
 */


public class MyLog {  //implements MyLogInf
    public static MyLog getLog(Class clz)		{ return new MyLog(clz.getName()); }  
    public static MyLog getLog(String clz)		{ return new MyLog(clz);           }  
    static MyLog getLogger()         			{ return getLog(MyLog.class); 		}  
    //------------------ 方案1  
//  private org.apache.commons.logging.Log _log = null;  
//  public MyLog(String clz) { _log = org.apache.commons.logging.LogFactory.getLog(clz);}  
    //------------------ 方案2  
    private org.slf4j.Logger 	_log = null;  
    public MyLog(String clz) { _log = org.slf4j.LoggerFactory.getLogger(clz);}  
    //------------------  
    public void debug(String message, Object... args){  
        if (_log.isDebugEnabled())  _log.debug(String.format(message, args));  
    }  
    public void info(String message, Object... args) {  
        if (_log.isInfoEnabled())   _log.info(String.format(message, args));  
    }  
    public void warn(String message, Object... args){  
        if (_log.isWarnEnabled())   _log.warn(String.format(message, args));  
    }  
    public void error(Throwable e, String message, Object... args){  
        if (_log.isErrorEnabled())  _log.error(String.format(message, args), e);  
    }  
    //------------------  
    public boolean isDebugEnabled() { return _log.isDebugEnabled();    }  
}  