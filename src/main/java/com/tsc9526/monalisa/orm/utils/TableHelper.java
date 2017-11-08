/*******************************************************************************************
 *	Copyright (c) 2016, zzg.zhou(11039850@qq.com)
 * 
 *  Monalisa is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.

 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.

 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************************/
package com.tsc9526.monalisa.orm.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import com.tsc9526.monalisa.orm.datasource.DBConfig;
import com.tsc9526.monalisa.orm.dialect.Dialect;
import com.tsc9526.monalisa.orm.meta.MetaColumn;
import com.tsc9526.monalisa.orm.meta.MetaIndex;
import com.tsc9526.monalisa.orm.meta.MetaTable;
import com.tsc9526.monalisa.tools.io.MelpClose;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class TableHelper {
	private TableHelper(){}
	
	public static MetaTable getMetaTable(DBConfig db,String tableName)throws SQLException{
		MTable mTable=new MTable(db,tableName);
		return mTable.getMetaTable();		  
	}
	 
	public static void getTableIndexes(DBConfig db,DatabaseMetaData dbm,MetaTable table)throws SQLException{
		Dialect dialect         = db.getDialect();
		
		String catalogPattern   = dialect.getMetaCatalogPattern(db);
		String schemaPattern    = dialect.getMetaSchemaPattern(db);
		String tablePattern     = dialect.getMetaTablePattern(db,table);
		
		ResultSet rs = dbm.getIndexInfo(catalogPattern, schemaPattern, tablePattern, false, true);  
	    while(rs.next()){
	    	short type=rs.getShort("TYPE");
	    	if(type != DatabaseMetaData.tableIndexStatistic){		    	
		    	String indexName  = rs.getString("INDEX_NAME");
		    	Integer position  = rs.getInt("ORDINAL_POSITION");			    	  
		    	String columnName = rs.getString("COLUMN_NAME");			    	 		    
		    	boolean nonUnique = rs.getBoolean("NON_UNIQUE"); 
		    	
		    	if(indexName.equalsIgnoreCase("PRIMARY")==false){		    	
			    	MetaIndex index=table.getIndex(indexName);
			    	if(index==null){			    	
				    	index=new MetaIndex();
				    	index.setName(indexName);
				    	index.setType(type);
				    	index.setUnique(!nonUnique);
				    					    	 
				    	table.addIndex(index);
			    	}
			    	
			    	MetaColumn c=table.getColumn(columnName);
			    	index.addColumn(c,position-1); 	
		    	}
	    	}		    	
	    }		    		    
	    rs.close(); 	 					    		    		 
	}
	
	public static void getTableColumns(DBConfig db,DatabaseMetaData dbm,MetaTable table)throws SQLException{
		getTableAllColumns(db,dbm,table);
		getTableKeyColumns(db,dbm,table);
	}
	
	private static void getTableAllColumns(DBConfig db,DatabaseMetaData dbm,MetaTable table)throws SQLException{
		Dialect dialect=db.getDialect();
		
		String catalogPattern = dialect.getMetaCatalogPattern(db);
		String schemaPattern  = dialect.getMetaSchemaPattern(db);
		String tablePattern   = dialect.getMetaTablePattern(db, table);
		
		ResultSet rs = dbm.getColumns(catalogPattern, schemaPattern,tablePattern, null);
	    while(rs.next()){
	    	boolean auto=false;
	    	 
	    	if(dialect.supportAutoIncrease()){
		    	String ai=(""+rs.getString("IS_AUTOINCREMENT")).toUpperCase();
		    	if("Y".equals(ai) || "YES".equals(ai) || "TRUE".equals(ai) || "1".equals(ai)){			    		 
		    		auto=true;
		    	}		
	    	}
	    	
	    	String value=rs.getString("COLUMN_DEF");
	    	if(value!=null && value.startsWith("'")){
	    		value=value.substring(1,value.length()-2);
	    	}
	    	
	    	MetaColumn column=new MetaColumn();		
	    	column.setTable(table);
	    	
	    	column.setName(rs.getString("COLUMN_NAME"));
	    	column.setValue(value);
	    	column.setKey(false);
	    	column.setJdbcType(rs.getInt("DATA_TYPE"));
	    	column.setLength(rs.getInt("COLUMN_SIZE"));
	    	column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
	    	column.setNotnull(rs.getInt("NULLABLE") != DatabaseMetaData.columnNullable);
	    	column.setRemarks(rs.getString("REMARKS"));
	    	column.setAuto(auto);
	    	 
	    	table.addColumn(column);
	    	
		}
	    rs.close();		    	   
	}

	private static void getTableKeyColumns(DBConfig db,DatabaseMetaData dbm,MetaTable table)throws SQLException{
		Dialect dialect         = db.getDialect();
		
		String catalogPattern   = dialect.getMetaCatalogPattern(db);
		String schemaPattern    = dialect.getMetaSchemaPattern(db);
		String tablePattern     = dialect.getMetaTablePattern(db,table);
		
		Map<Short, MetaColumn> keyColumns = new TreeMap<Short, MetaColumn>();
	    ResultSet rs = dbm.getPrimaryKeys(catalogPattern,schemaPattern, tablePattern);
	    while(rs.next()){
	    	String columnName = rs.getString("COLUMN_NAME"); //$NON-NLS-1$
	    	short keyseq      = rs.getShort("KEY_SEQ"); //$NON-NLS-1$
	    	
	    	MetaColumn column=table.getColumn(columnName);
	    	column.setKey(true);
	    	keyColumns.put(keyseq, column);
	    }
	  
	    for(MetaColumn c:keyColumns.values()){
	    	table.addKeyColumn(c);
	    }
	    rs.close();
	}
	 
	private static class MTable{
		private DBConfig db;
		private String tableName;
		private DatabaseMetaData dbm;
		private MetaTable table;
		
		public MTable(DBConfig db,String tableName){
			this.db=db; 
			this.tableName=tableName;
		}
		
		public MetaTable getMetaTable()throws SQLException{
			Connection conn=null;
			try{
				conn=db.getDataSource().getConnection();
				dbm=conn.getMetaData();
				
				table=new MetaTable(tableName);
				
				getTableColumns(db,dbm,table);								 
				getTableIndexes(db,dbm,table);
				
				if(table.getColumns().isEmpty()){
					return null;
				}else{				
					return table;
				}
			}finally{
				MelpClose.close(conn);
			}			  
		}
	}
}
