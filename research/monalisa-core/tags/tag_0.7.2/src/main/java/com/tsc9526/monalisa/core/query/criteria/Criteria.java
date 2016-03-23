package com.tsc9526.monalisa.core.query.criteria;

import java.util.ArrayList;
import java.util.List;

import com.tsc9526.monalisa.core.datasource.DBConfig;
import com.tsc9526.monalisa.core.query.Query;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("unchecked")
public abstract class Criteria<X extends Criteria<?>> {
	public final static int IGNORE_NONE =0;
	public final static int IGNORE_NULL =1;
	public final static int IGNORE_EMPTY=2;
	
	Query q=new Query();		
	
	private List<String> orderBy=new ArrayList<String>();
	
	private int ignore=IGNORE_NONE;
		
	
	void addOrderByAsc(String field){
		addOrderBy(field+" ASC");
	}
	
	void addOrderByDesc(String field){
		addOrderBy(field+" DESC");
	}
	
	protected void use(DBConfig db){
		q.use(db);
	}
	
	private void addOrderBy(String by){
		if(!orderBy.contains(by)){
			orderBy.add(by);		 
		}	
	}
	
	List<String> getOrderBy(){
		return orderBy;
	}

	public int getIgnore() {
		return ignore;
	}
	
	public X ingoreNull() {
		this.ignore = IGNORE_NULL;		
		return (X)this;
	}
	
	public X ingoreEmpty() {
		this.ignore = IGNORE_EMPTY;		
		return (X)this;
	}		 
}