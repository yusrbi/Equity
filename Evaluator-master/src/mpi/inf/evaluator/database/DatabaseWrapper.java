package mpi.inf.evaluator.database;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.copy.CopyManager;


import mpi.inf.evaluator.model.Document;
import mpi.inf.evaluator.model.Table;
import mpi.inf.evaluator.model.Unit;

public class DatabaseWrapper {

	private static DatabaseWrapper db_wrapper = null;
	DataSource ds = null;
	
	public static DatabaseWrapper getDatabaseWrapper() {
	
		if(db_wrapper == null){
			db_wrapper = new DatabaseWrapper();
		}
		return db_wrapper;
	}
	private DatabaseWrapper(){
		try {
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/postgres");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public void load_document(Table table, Document document,String user_name) throws ClassNotFoundException {
		String table_title="",table_html="";
		String document_title="", document_id="", document_html="", document_url="", experiment_id="";
		int table_id=0;
		
		String query =  "SELECT evaluation.table_html_results.table_id, evaluation.document_html_results.document_id, evaluation.document_html_results.experiement_id as experiment_id, evaluation.document_html_results.html_content as document_html,evaluation.table_html_results.html_content as table_html, title as document_title, url"
		+ "  FROM evaluation.document_html_results"
		+ "  inner join evaluation.document_data on evaluation.document_data.document_id = evaluation.document_html_results.document_id"
		+ "  inner join evaluation.table_html_results on  evaluation.table_html_results.document_id = evaluation.document_html_results.document_id and"
		+ "  evaluation.table_html_results.experiement_id = evaluation.document_html_results.experiement_id"
		+ " where concat_ws('_',  evaluation.document_html_results.document_id, evaluation.table_html_results.table_id) not in (select id from evaluation.assignment) "
		+ " and concat_ws('_',  evaluation.document_html_results.document_id, evaluation.table_html_results.table_id) not in (select id from evaluation.skipped where \"user\" ='"
		+ user_name
		+ "') "
		+ " order by evaluation.document_html_results.experiement_id, evaluation.document_html_results.document_id, evaluation.table_html_results.table_id"
		+ " limit 1;";
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			while (rs.next()) {
				table_html = rs.getString("table_html");
				table_id = rs.getInt("table_id");
				document_title = rs.getString("document_title");
				document_url = rs.getString("url");
				document_html = rs.getString("document_html");
				document_id = rs.getString("document_id");
				experiment_id = rs.getString("experiment_id");
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		
		table.setDocument_id(document_id);
		table.setTable_html(table_html);
		table.setTable_id(table_id);
		table.setTable_title(table_title);
		document.setContent_html(document_html);
		document.setId(document_id);
		document.setTitle(document_title);
		document.setUrl(document_url);
		document.setExperiment_id(experiment_id);
		
	}
	public void copyToDB(String table_name, StringBuilder sb, int nvalues){
		if(sb.length() <=0)
			return;
		CopyManager cpManager;
		PushbackReader reader;
		Connection connection = null;
		Connection pgconn;
		try {
			connection = ds.getConnection();
			pgconn = (((DelegatingConnection) connection).getInnermostDelegate());

			cpManager = ((PgConnection) pgconn).getCopyAPI();
			reader = new PushbackReader( new StringReader(sb.toString()), nvalues );			
			cpManager.copyIn("COPY "+table_name+" FROM STDIN WITH CSV", reader );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	public void submitTableEvaluation(StringBuilder evaluation, int count) {
		copyToDB("evaluation.table_evaluation", evaluation, count);
		
	}
	public void submitDocumentEvaluation(String document_id , StringBuilder evaluation, int count) {
		if(!evaluation_contains(document_id)){
			copyToDB("evaluation.document_evaluation", evaluation, count);
		}
	
	}
	private boolean evaluation_contains(String document_id) {
		
		String query =  String.format("SELECT * FROM evaluation.document_evaluation"
				+ " WHERE document_id= '%s'", document_id );
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		boolean contains =false;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			if(rs.next()){
				contains = true;
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return contains;
	}
	public void remove_assignment(int table_id, String document_id, String user_name) {
		String query =  String.format("DELETE FROM evaluation.assignment WHERE \"user\" = '%s' and id ='%s';", 
				user_name, document_id+"_"+table_id);		
		Statement stmt = null;		
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			stmt.execute(query);
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}		
	}
	public void assign(int table_id, String document_id, String user_name) {
		
		String query =  String.format("INSERT INTO evaluation.assignment(\"user\", id)  "
				+ " VALUES ('%s', E'%s');", 
				user_name, document_id+"_"+table_id);		
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			stmt.execute(query);
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}		
	}
	public void skip(int table_id, String document_id, String user_name) {
		String query =  String.format("INSERT INTO evaluation.skipped(\"user\", id)  "
				+ " VALUES ('%s', E'%s');", 
				user_name, document_id+"_"+table_id);		
		Statement stmt = null;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			stmt.execute(query);
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		
	}
	public List<SelectItem> getDimensions() {
		String query =  String.format("SELECT name, freebase_id as id  FROM qkb.dimension;");
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		String name;
		String id;
		List<SelectItem>  dimensions = new ArrayList<SelectItem>();
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			while(rs.next()){
				id = rs.getString("id");
				name = rs.getString("name");
				dimensions.add(new SelectItem(id,name));
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return dimensions;
	
	}
	public List<SelectItem> getDataTyeps() {
     String query =  String.format("SELECT id, name  FROM qkb.data_type;");
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		String name;
		String id;
		List<SelectItem>  data_type = new ArrayList<SelectItem>();
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			while(rs.next()){
				id = rs.getString("id");
				name = rs.getString("name");
				data_type.add(new SelectItem(id,name));
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return data_type;
	}
	public String add_unit(Unit unit) {
		if(unit_exists(unit)){
			return "";
		}
		String query =  String.format("INSERT INTO qkb.unit(  name, freebase_id, dimension, "
				+ "wikipedia_id, wikipedia_title, key, data_type)"
				+ "  VALUES ( E'%s', E'%s', E'%s', %d, E'%s', E'%s', %d) RETURNING id; ", 
				unit.getName().trim(), unit.getFreebase_id().trim(), unit.getDimension(),
				unit.getWikipedia_id(), unit.getWikipedia_title().trim(), unit.getKey().trim(), unit.getData_type());		
		ResultSet rs = null;
		Statement stmt = null;
		
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);
			connection.setAutoCommit(true);
		if(rs.next()){
			int id = rs.getInt("id");
			if(id >0){
				int alias_id=0;
				for(String alias : unit.getAliases().split("\n")){
					if(alias.isEmpty())
						continue;
					alias_id = add_alias(alias.trim());
					if(alias_id >0)
						add_unit_alias(id,alias_id);
				}
			}
			
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return query;
	}
	private boolean unit_exists(Unit unit) {
		String query =  String.format("SELECT * FROM qkb.unit  "
				+ " WHERE name= E'%s' or key =E'%s'", unit.getName().trim(), unit.getKey().trim() );
		
		Statement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		boolean exists =false;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);

			if(rs.next()){
				exists = true;
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}
		return exists;
	}
	private void add_unit_alias(int unit_id, int alias_id) {
		String query =  String.format("INSERT INTO qkb.unit_alias(unit_id, alias_id)   VALUES (%d, %d); ",	unit_id,alias_id);		
		Statement stmt = null;
		int id =-1;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			stmt.execute(query);
			connection.setAutoCommit(true);
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return;
		
	}
	private int add_alias(String alias) {
		String query =  String.format("INSERT INTO qkb.alias(name) VALUES (E'%s') RETURNING id; ",	alias);		
		ResultSet rs = null;
		Statement stmt = null;
		int id =-1;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			stmt.setFetchSize(1000000);
			rs = stmt.executeQuery(query);
			connection.setAutoCommit(true);
		if(rs.next()){
			id = rs.getInt("id");
			
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) { /* ignored */
			}
			try {
				connection.close();
			} catch (Exception e) { /* ignored */
			}
		}

		return id;
	}
	public void submitTableGoldStandards(StringBuilder input, int count) {
		copyToDB("evaluation.table_gold_standard", input, count);
		
	}
	public void submitDocumentGoldStandards(String document_id, StringBuilder input, int count) {
		//if(!evaluation_contains(document_id)){
			copyToDB("evaluation.document_gold_standard", input, count);
	//	}
		
		
	}
}
