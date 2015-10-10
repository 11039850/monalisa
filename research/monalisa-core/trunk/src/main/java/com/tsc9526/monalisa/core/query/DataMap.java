package com.tsc9526.monalisa.core.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class DataMap extends LinkedHashMap<String,Object>{ 
	private static final long serialVersionUID = -8132926422921115814L;	
	
	public Object put(String key,Object value){
		if(key!=null){
			key=key.toLowerCase();
		}		
		return super.put(key, value);
	}
	
	public Object get(String key){
		if(key!=null){
			key=key.toLowerCase();
		}
		 
		return super.get(key);
	}
	
	public Object remove(String key){
		if(key!=null){
			key=key.toLowerCase();
		}
		return super.remove(key);
	}
	
	public boolean containsKey(String key){
		if(key!=null){
			key=key.toLowerCase();
		}
		 
		return super.containsKey(key);
	}
	
	public String getString(String key){
		if(key!=null){
			key=key.toLowerCase();
		}
		 
		Object v=super.get(key);
		if(v==null){
			return null;
		}else {
			return v.toString();
		}
	}
	
	public Integer getInteger(String key){
		Object v=(Object)get(key);
		if(v==null){
			return null;
		}else{
			if(v instanceof Integer){
				return (Integer)v;
			}else{
				return Integer.parseInt(""+v);
			}
		}
	}
	
	public Long getLong(String key){
		Object v=(Object)get(key);
		if(v==null){
			return null;
		}else{
			if(v instanceof Long){
				return (Long)v;
			}else{
				return Long.parseLong(""+v);
			}
		}
	}
	
	public Float getFloat(String key){
		Object v=(Object)get(key);
		if(v==null){
			return null;
		}else{
			if(v instanceof Float){
				return (Float)v;
			}else{
				return Float.parseFloat(""+v);
			}
		}
	}
	
	public Double getDouble(String key){
		Object v=(Object)get(key);
		if(v==null){
			return null;
		}else{
			if(v instanceof Double){
				return (Double)v;
			}else{
				return Double.parseDouble(""+v);
			}
		}
	}
	
	public Date getDate(String key){
		Object v=(Object)get(key);
		if(v==null){
			return null;
		}else{
			if(v instanceof Date){
				return (Date)v;
			}else{
				String x=""+v;
				
				try{
					if(x.indexOf(" ")>0){
						SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						return sdf.parse(x);
					}else{
						SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
						return sdf.parse(x);
					}	
				}catch(ParseException e){
					throw new RuntimeException("Invalid date: "+x,e);
				}
			}
		}
	}
}
