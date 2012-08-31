package appengine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import com.google.appengine.api.rdbms.AppEngineDriver;

public class SqlCloud {

	private String dsn = null;
	private String user = null;
	private String password = null;
	private boolean keepConnected = false;
	private Connection conn = null;
	private Statement statement = null;
	
	// initializer without user/pwd
	public SqlCloud( String dsn ) throws SQLException {
		
		this.dsn = dsn;
		DriverManager.registerDriver( new AppEngineDriver() );
	}
	
	// initializer with dns/user/pwd
	public SqlCloud( String dsn, String user, String password ) throws SQLException {
		
		this.dsn = dsn;
		this.user = user;
		this.password = password;
		DriverManager.registerDriver( new AppEngineDriver() );
	}
	
	//
	
	
	
	// connect to database
	private void Connect() throws SQLException {
		
		// check if its closed, then open
		if( conn == null || conn.isClosed() ) {	
			conn = ( this.user == null ) ? DriverManager.getConnection(this.dsn) : DriverManager.getConnection( this.dsn, this.user, this.password );
		}
		
		// check if statement is closed, then create
		if( statement == null || statement.isClosed() ) {
			statement = conn.createStatement();
		}
		
		PreparedStatement prepared = conn.prepareStatement("INSERT INTO b (name,number,age,tel) VALUES ( ?, ?, ?, ? );");
		
		
		prepared.setObject(1, "la");
		
		
		
	}
	
	// close
	public void Close() throws SQLException { conn.close(); }
	
}
