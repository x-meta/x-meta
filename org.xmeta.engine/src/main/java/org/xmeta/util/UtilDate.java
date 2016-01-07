package org.xmeta.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UtilDate  {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    
    static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    static SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
    static SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP);
    
    /**
     * 返回持续时间的信息，如36分32秒这样的字符串。
     * 
     * @param time 时间
     * @return 格式化后的字符串
     */
    public static String getLastTimeInfo(long time){
    	long day = 24 * 3600000;
    	long hour = 3600000;
    	long minute = 60000;
    	
    	String info = "";
    	long d1 = time / day;
    	long d2 = time % day;
    	if(d1 > 0){
    		info = info + d1 + "天";
    	}
    	
    	if(d2 > 0){
    		d1 = d2 / hour;
    		d2 = d2 % hour;
    		
    		if(d1 > 0){
    			info = info + d1 + "小时";
    		}
    		
    		if(d2 > 0){
    			d1 = d2 / minute;
    			d2 = d2 % minute;
    			
    			if(d1 > 0){
    				info = info + d1 + "分钟";
    			}
    			
    			if(d2 > 0){
    				d2 = d2 / 1000;
    				if(d2 > 0){
    					info = info + d2 + "秒";
    				}
    			}
    		}
    	}   
    	
    	return info;
    }
    
    /**
     * 获取本星期的第一天。
     * 
     * @return 日期
     */
    public static Date getWeekStart(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	//把时分秒清空
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	//设置本星期第一天
    	calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - calendar.get(Calendar.DAY_OF_WEEK) + 1);
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 获取本星期的最后一天。
     * 
     * @return 日期
     */
    public static Date getWeekEnd(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	//把时分秒清空
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	//设置本星期第一天
    	calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - calendar.get(Calendar.DAY_OF_WEEK) + 7);
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 获取本月的第一天。
     * 
     * @return 日期
     */
    public static Date getMonthStart(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	//把时分秒清空
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	//设置本星期第一天
    	calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - calendar.get(Calendar.DAY_OF_MONTH) + 1);
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 获取本月的最后一天。
     * 
     * @return 日期
     */
    public static Date getMonthEnd(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	//把时分秒清空
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	
    	//本月的最后一天，也是下个月一号的前一天
    	calendar.set(Calendar.DATE, 1);
    	calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    	calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);    	
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 获取当年的第一天。
     * 
     * @return 日期
     */
    public static Date getYearStart(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	calendar.set(Calendar.DATE, 1);
    	calendar.set(Calendar.MONTH, 0);    	
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 获取本年的最后一天。
     * @return 日期
     */    
    public static Date getYearEnd(){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());
    	
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	calendar.set(Calendar.MINUTE, 0);
    	calendar.set(Calendar.SECOND, 0);
    	calendar.set(Calendar.MILLISECOND, 0);
    	calendar.set(Calendar.DATE, 31);
    	calendar.set(Calendar.MONTH, 11);    	
    	
    	return calendar.getTime(); 
    }
    
    /**
     * 返回指定日期所在月的天数。
     * 
     * @param date 日期
     * @return 天数
     */
    public static int getMonthDayCount(Date date){
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(date);
    	
    	calendar.set(Calendar.DATE, 1);
    	calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
    	calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
    	return calendar.get(Calendar.DATE);
    }
    
    public static String getDateString(Date date, String format){
    	SimpleDateFormat f = new SimpleDateFormat(format);
    	return f.format(date);
    }
    
    /**
     * 是否是在一个时间段范围里。
     * 
     * @param adate 第一个日期
     * @param start 第二个日期
     * @param end 结束日期
     * @return 是否
     */
    public static boolean isBetween(Date adate, Date start, Date end){
    	if(adate == null) return false;
    	
    	if(start != null && start.getTime() <= adate.getTime()){
    		return true;
    	}
    	
    	if(end != null && end.getTime() >= adate.getTime()){
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * 查看是否是今天。
     * 
     * @param date 日期
     * @return 是否
     */
    public static boolean isToday(Date date){
    	Date now = new Date();
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
    	if(sf.format(now).equals(sf.format(date))){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    /**
     * 获取一个时间加一个时间间隔（毫秒）后的时间。
     * 
     * @param date1 日期
     * @param interval 间隔
     * @return 结果
     */
    public static Date getDate(Date date1, long interval){
    	if(date1 == null) return null;
    	
    	long time = date1.getTime();
    	return new Date(time + interval);
    }
    
    /**
     * 获取一个时间加一个时间间隔（天）后的时间。
     * 
     * @param date 日期
     * @param interval 间隔
     * @return 结果
     */
    public static Date getDate(Date date, double interval){
    	if(date == null){
    		return null;
    	}
    	
    	return new Date(date.getTime() + (long)(interval * 86400000));
    }
    
    /**
     * 返回昨天
     * @return 日期
     */
    public static Date getYesterday(){
    	Date date = new Date();
    	date = new Date(date.getTime() - 24 * 3600 * 1000);
    	try {
			return getDate(UtilDate.getDateString(date, "yyyy-MM-dd"), "yyyy-MM-dd");
		} catch (ParseException e) {
			return date;
		}
    }
    
    /**
     * 取得明天。
     * 
     * @return 明天
     */
    public static Date getTomorrow(){
    	Date date = new Date();
    	date =  new Date(date.getTime() + 24 * 3600 * 1000);
    	try {
			return getDate(UtilDate.getDateString(date, "yyyy-MM-dd"), "yyyy-MM-dd");
		} catch (ParseException e) {
			return date;
		}
    }
        
    public static Date getDate(String dateStr) {
    	Date date = null;
    	if("current_date".equals(dateStr)){
    		date =  new Date();
    	}else{
    		try {
				date =  dateFormat.parse(dateStr);
			} catch (ParseException e) {
				//e.printStackTrace();
			}
    	}
    	return date;
    }
    
    public static Date getTime(String timeStr) throws ParseException{
    	Date date = null;
    	if("current_time".equals(timeStr)){
    		date = new Date();
    	}else{
    		date = timeFormat.parse(timeStr);
    	}
    	return date;
    }
    
    public static Date getTimestamp(String timeStr) throws ParseException{
    	Date date = null;
    	if("current_date".equals(timeStr)){
    		date =  new Date();
    	}else{	    
	    	try{
	    		date = timestampFormat.parse(timeStr);
	    	}catch(Exception e){
	    		try{
	    			date =  dateFormat.parse(timeStr);
	    		}catch(Exception ee){
	    			date = timestampFormat.parse("1970-01-01 " + timeStr);
	    		}
	    	}
    	}
    	return date;
    }
    
    public static Date getDate(String dateStr, String format) throws ParseException{
    	SimpleDateFormat f = new SimpleDateFormat(format);
    	return f.parse(dateStr);
    }
    
    /**
     * 取两个时间的时间差。
     * 
     * @param date1 日期1
     * @param date2 日期2
     * @return 间隔
     */
    public static long getInterval(Date date1, Date date2){
    	//如果其中的一个时间为空，返回0
    	if(date1 == null || date2 == null){
    		return 0;
    	}
    	
    	long t1 = date1.getTime();
    	long t2 = date2.getTime();
    	
    	if(t1 > t2){
    		return t1 - t2;
    	}else{
    		return t2 - t1;
    	}
    }

}
