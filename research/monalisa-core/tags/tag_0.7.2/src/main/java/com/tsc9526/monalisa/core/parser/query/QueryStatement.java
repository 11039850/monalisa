package com.tsc9526.monalisa.core.parser.query;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tsc9526.monalisa.core.parser.jsp.JspCode;
import com.tsc9526.monalisa.core.parser.jsp.JspElement;
import com.tsc9526.monalisa.core.parser.jsp.JspEval;
import com.tsc9526.monalisa.core.parser.jsp.JspText;
import com.tsc9526.monalisa.core.tools.JavaWriter;

public class QueryStatement { 
	static Log logger=LogFactory.getLog(QueryStatement.class.getName());
	
	
	private QueryPackage queryPackage;
	
	private String comments;
	private String id;
	private String db;
	private String resultClass;
	
	private List<JspElement> elements=new ArrayList<JspElement>();
	 
	private final static String REGX_VAR="\\$[a-zA-Z_]+[a-zA-Z_0-9]*";
	private final static String REGX_ARG="[_a-z_A-Z]+[_a-z_A-Z0-9]*\\s+[_a-z_A-Z]+[_a-z_A-Z0-9]*\\s*=\\s*args.pop\\s*\\(.*;";
	
	private Pattern patternVar = Pattern.compile(REGX_VAR);
	private Pattern patternArg = Pattern.compile(REGX_ARG);
	
	private Method method;
	
	public void write(JavaWriter writer){
		writer.append("public void ").append(id).append("(Query q,Args args){\r\n");
		writeUseDb(writer); 
		writeElements(writer);
		writer.append("}\r\n\r\n");
	}
	 
	
	public void add(JspElement e){
		elements.add(e);
	}
	
	public List<JspElement> getElements(){
		return elements;
	}
	
	public List<String> getArgs(){
		List<String> args=new ArrayList<String>();
		
		for(JspElement e:elements){
			if(e instanceof JspCode){
				String code=e.getCode(); 
	
				Matcher m=patternArg.matcher(code);
				while(m.find()){
					String var=m.group();
					 
					args.add(var);
				}
			}
		}
		
		return args;
	}
	
	protected void writeElements(JavaWriter writer){
		boolean append=false;
		
		for(JspElement e:elements){
			if(e instanceof JspText){
				String code=e.getCode();
			 	
				String[] lines=code.split("\n");
				boolean lastLineEmpty=lines[lines.length-1].trim().length()==0;
				
				for(int i=0;i<lines.length;i++){
					String line=lines[i];
					
					if(line.trim().length()==0){
						if(!append || i==lines.length-1){
							continue;
						}
					}
					
					append=true;
					List<String> vars=new ArrayList<String>();
					Matcher m=patternVar.matcher(line);
					while(m.find()){
						String var=m.group();
						vars.add(var.substring(1));
					}
					
					String s=line.replaceAll(REGX_VAR, "?");
					writer.append("q.add(\"").append(s).append("\"");
					if(vars.size()>0){
						for(String v:vars){
							writer.append(","+v);
						}
					}
					
					if(lastLineEmpty){
						writer.append(").add(\"\\r\\n\");\r\n");
					}else{
						writer.append(");");
					}
				}
			}else if(e instanceof JspEval){
				String s=e.getCode();
				if(s.startsWith("\"")){
					writer.append("q.add("+s+");\r\n");
				}else{
					writer.append("q.add(\"?\",").append(s).append(");\r\n");
				}
			}else if(e instanceof JspCode){
				writer.append(e.getCode());
			}
		}
	}
	
	private void writeUseDb(JavaWriter writer){
		String db=this.db;
		if(db==null || db.length()<1){
			db=queryPackage.getDb();
		}
		 
		if(db!=null && db.length()>0){
			if(db.endsWith(".class")){
				writer.append("q.use(DBConfig.fromClass("+db+"));\r\n");
			}else if(db.endsWith(".DB")){
				writer.append("q.use("+db+");\r\n");
			}else{
				int x=db.indexOf(".class.getName");
				if(x>0){
					writer.append("q.use(DBConfig.fromClass("+db.substring(0,x+6)+"));\r\n");
				}else{
					writer.append("q.use(DBConfig.fromClass("+db+".class));\r\n");
				}
			}
		}
	}

	public QueryPackage getQueryPackage() {
		return queryPackage;
	}

	public void setQueryPackage(QueryPackage queryPackage) {
		this.queryPackage = queryPackage;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		if(db!=null)db=db.trim();
		
		this.db = db;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getResultClass() {
		return resultClass;
	}

	public void setResultClass(String resultClass) {
		if(resultClass!=null && resultClass.endsWith(".class")){
			resultClass=resultClass.substring(0,resultClass.length()-6);
		}
		this.resultClass = resultClass;
	}
	 
}