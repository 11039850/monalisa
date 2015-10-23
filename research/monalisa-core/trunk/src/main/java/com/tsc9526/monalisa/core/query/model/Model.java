package com.tsc9526.monalisa.core.query.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.tsc9526.monalisa.core.annotation.Table;
import com.tsc9526.monalisa.core.datasource.DBConfig;
import com.tsc9526.monalisa.core.datasource.DataSourceManager;
import com.tsc9526.monalisa.core.query.dao.Delete;
import com.tsc9526.monalisa.core.query.dao.Insert;
import com.tsc9526.monalisa.core.query.dao.Update;
import com.tsc9526.monalisa.core.query.dialect.Dialect;
import com.tsc9526.monalisa.core.query.partition.CreateTableCache;
import com.tsc9526.monalisa.core.tools.ClassHelper.FGS;
import com.tsc9526.monalisa.core.tools.ModelHelper;

/**
 * 数据库表模型
 * 
 * 
 * @author zzg.zhou(11039850@qq.com)
 * 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Model<T extends Model> implements Serializable{
	private static final long serialVersionUID = 703976566431364670L;

	protected static DataSourceManager dsm=DataSourceManager.getInstance();
	 
	protected transient ModelMeta modelMeta=new ModelMeta();
	  	
	public Model(){		
		
	}	
	
	public Model(String tableName,String... primaryKeys){
		modelMeta.tableName=tableName;
		modelMeta.primaryKeys=primaryKeys;
	}
 
	protected ModelMeta mm() {
		if(modelMeta.initialized==false){
			modelMeta.initModelMeta(this);			 
		}
		return modelMeta;
	}
	
	/**
	 * Check if the model is dirty 
	 * @return
	 */
	public boolean dirty(){
		return mm().dirty;
	}
	
	/**
	 * Set the model dirty
	 * @param dirty
	 * @return
	 */
	public T dirty(boolean dirty){
		mm().dirty=dirty;
		return (T)this;
	}
	
	/**
	 * Check if the model is db entity
	 * @return
	 */
	public boolean entity(){
		return mm().entity;
	}
	
	/**
	 * Set the model db entity
	 * @param entity
	 * @return
	 */
	public T entity(boolean entity){
		mm().entity=entity;
		return (T)this;
	}
	
	/**
	 * @see com.tsc9526.monalisa.core.tools.ModelHelper#parseModel(Model, Object)
	 * 
	 * @param dataObject HttpServletRequest, Map, JsonObject, String(json format), JavaBean
	 * @param mappings  [Options] Translate dataObject field to model field <br>
	 * For example: <br> 
	 * "user_id=id", ... // Parse dataObject.user_id to Model.id<br>
	 * Another example:<br>
	 * "~XXX"  //Only parse the fields with prefix: XXX 
	 * 
	 */
	public T parse(Object dataObject,String... mappings) {
		ModelHelper.parse(this, dataObject,mappings);
		
		return (T)this;
	}
	
	/**
	 * 保存对象到数据库,忽略该对象中值为null的字段
	 */
	public int save(){
		return save(true);
	}
	
	/**
	 * 保存或更新对象到数据库,忽略该对象中值为null的字段
	 */
	public int saveOrUpdate(){
		return saveOrUpdate(true);
	}
	
	/**
	 * 更新对象到数据库,忽略该对象中值为null的字段
	 */
	public int update(){
		return update(true);
	}	
	
	
	
	/**
	 * 存储对象到数据库
	 * 
	 * @param selective
	 * true-更新时忽略值为null的字段, false-存储所有字段(包括null)到数据库
	 * 
	 * @return 成功变更的记录数
	 */
	public int save(boolean selective){
		int r=-1;
		try{
			before(ModelEvent.INSERT);
			
			doValidate();
			
			if(selective){
				r= new Insert(this).insertSelective();
			}else{
				r= new Insert(this).insert();
			}
			
			return r;
		}finally{
			after(ModelEvent.INSERT, r);
		} 			 		
	}
	
	/**
	 * 存储对象到数据库， 如果主键冲突， 则执行更新操作
	 * 
	 * @param selective
	 * true-更新时忽略值为null的字段, false-存储所有字段(包括null)到数据库
	 * 
	 * @return 成功变更的记录数
	 */
	public int saveOrUpdate(boolean selective){
		int r=-1;
		try{
			before(ModelEvent.INSERT_OR_UPDATE);
			
			doValidate();
			
			if(selective){
				r= new Insert(this).insertSelective(true);
			}else{
				r= new Insert(this).insert(true);
			}
			
			return r;
		}finally{
			after(ModelEvent.INSERT_OR_UPDATE, r);
		} 			 
	}
	
	/**
	 * 更新对象到数据库
	 *  
	 * @param selective
	 * true-更新时忽略值为null的字段, false-存储所有字段(包括null)到数据库
	 * 
	 * @return 成功变更的记录数
	 */
	public int update(boolean selective){
		int r=-1;
		try{
			before(ModelEvent.UPDATE);
			
			doValidate();
			
			if(selective){
				r= new Update(this).updateSelective();
			}else{
				r= new Update(this).update();
			}
			
			return r;
		}finally{
			after(ModelEvent.UPDATE, r);
		} 		 		
	}
	 
	/**
	 * 从数据库删除该记录
	 * 
	 * @return 成功变更的记录数
	 */
	public int delete(){
		int r=-1;
		try{
			before(ModelEvent.DELETE);
			r= new Delete(this).delete();
			return r;
		}finally{
			after(ModelEvent.DELETE, r);
		} 
	}
	
	protected void before(ModelEvent event){
		if(mm().listener!=null){
			mm().listener.before(event, this);
		}
	}
	
	protected void after(ModelEvent event, int r) {
		if(r>=0){
			dirty(false);
			
			switch(event) {
				case INSERT: 			entity(true); 	break;
				case DELETE: 			entity(false); 	break;
				case UPDATE: 			entity(true);	break;
				case INSERT_OR_UPDATE: 	entity(true); 	break;	
				case LOAD: 				entity(true); 	break;
			}
		}
		
		if(mm().listener!=null){
			mm().listener.after(event, this,r);
		}
	}
	 
	/**
	 * 设置所有字段为null
	 * 
	 * @return 
	 */
	public T clear(){
		for(FGS fgs:fields()){
			fgs.setObject(this,null);
		}
		
		return (T)this;
	}
	
	
	/**
	 * 
	 * @return 返回数据库方言
	 */
	public Dialect dialect(){		 		
		return mm().dialect;
	}
	
	/**
	 * 
	 * @return 返回表的字段列表
	 */
	public Collection<FGS> fields() {		 
		return mm().fields();
	}	
	
	public FGS field(String name) {
		return mm().findFieldByName(name);		 
	}
	
	
	public boolean updateKey(){
		return mm().updateKey;
	}
	
	/**
	 * How to update the model's primary key:<br>
	 * <code>
	 *   Model model=new Model(oldId).load(); <br>
	 *   model.updateKey(true); //Set enable update primary key! <br>
	 *   model.setId(newId);    //Set new primary key <br>
	 *   model.setXxx ...       //Set other fields ...<br>
	 *   <br>
	 *   Update update=new Update(model); <br>
	 *   update.update("id=?",oldId);<br>
	 * </code>
	 * 
	 * @return Enable update the model's primary key, default is: false
	 */
	public T updateKey(boolean updateKey){
		return (T)this;
	}
	
	public T set(String name,Object value){
		FGS fgs=field(name);
		if(fgs!=null){
			fgs.setObject(this, value);	
			
			dirty(true);
		}		
		return (T)this;
	}
	
	public Object get(String name){
		FGS fgs=field(name);
		if(fgs!=null){
			return fgs.getObject(this);	
		}		
		return null;
	}
	
	/**
	 * 
	 * @return 返回表名等信息
	 */
	public Table table(){
		if(mm().mp!=null){			 
			return CreateTableCache.getTable(mm().mp, this, mm().table);
		}else{
			return mm().table;
		}
	}
	 
	/**
	 * 
	 * @return 返回表的自增字段， 如果没有则返回null
	 */
	public FGS autoField(){ 		
		return mm().autoField;
	}
	
	/**
	 * 
	 * @return 数据库连接信息
	 */
	public DBConfig db(){
		return mm().db;
	}
	
	/**
	 * 设置访问数据库
	 * 
	 * @param db
	 * @return
	 */
	public T use(DBConfig db){
		mm().use(db);
		
		return (T)this;
	}		 	
	
	public boolean readonly(){
		return mm().readonly;
	}
	
	public void readonly(boolean readonly){
		mm().readonly=readonly;		
	}
	
	
	/**
	 * 
	 * @return 逗号分隔的字段名， *： 表示返回所有字段
	 */
	public String filterFields(){
		return mm().filterFields();
	}
	
	
	/**
	 * 排除表的某些字段。 用于在查询表时， 过滤掉某些不必要的字段
	 * 
	 * @param fields 要排除的字段名
	 * 
	 * @return  模型本身
	 */
	public T exclude(String ... fields){		 
		mm().exclude(fields);
		
		return (T)this;
	}
		
	/**
	 * 排除大字段（字段长度 大于等于 #Short.MAX_VALUE)
	 * 
	 * @return 模型本身
	 */
	public T excludeBlobs(){
		mm().excludeBlobs();
		
		return (T)this;
	}
	
	/**
	 *  排除超过指定长度的字段
	 * 
	 * @param maxLength  字段长度
	 * 
	 * @return 模型本身
	 */
	public T excludeBlobs(int maxLength){
		mm().excludeBlobs(maxLength);
		
		return (T)this;
	}
	
	/**
	 * 只提取某些字段
	 * 
	 * @param fields  需要的字段名称
	 * @return 模型本身
	 */
	public T include(String ... fields){		 
		mm().include(fields);
		
		return (T)this;
	}		
	
	/**
	 * 复制对象数据
	 */
	public T copy(){
		return (T)mm().copyModel();
	}
	  
	protected void doValidate() {
		mm().doValidate();
	}
	
	 /**
	 * 校验字段数据的是否合法.
	 * 
	 *  @return 不合法的字段列表{字段名: 错误信息}. 如果没有错误, 则为空列表.
	 */
	public List<String> validate(){
		return mm().validate();
	}
	
	public List<ModelIndex> indexes(){
		return mm().indexes;
	}
	
	public List<ModelIndex> uniqueIndexes(){
		return mm().uniqueIndexes();
	}
	 
	public ModelListener listener(){
		return mm().listener;
	}	
}
