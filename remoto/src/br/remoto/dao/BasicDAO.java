package br.remoto.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import br.remoto.model.ReMoto;


public class BasicDAO 
{
	protected static final String jdbcDriver = "org.hsqldb.jdbcDriver";
	protected static final String jdbcUser = "sa";
	protected static final String jdbcPassword = "";
	protected static String jdbcUrl;
	
	protected static Connection con;
	protected static boolean serverStarted = false;
	protected ResultSet rs;
	protected Statement st;
	protected PreparedStatement pst; 	
	
	
	public BasicDAO()
	{
	}
	

	public void connect()
	{
		try 
		{
			if( con == null )
			{
				Class.forName( jdbcDriver );
				
				//jdbcUrl = "jdbc:hsqldb:file:" + ReMoto.path + "db/" + "remoto;shutdown=true";
				
				// Server start:
				// java -cp C:/java/Tomcat/webapps/remoto/WEB-INF/lib/hsqldb.jar org.hsqldb.Server -database.0 file:C:/java/Tomcat/webapps/remoto/db/remoto -dbname.0 remoto				
				// Can be stored on HKEY_LOCAL_MACHINESOFTWARE\Microsoft\Windows\CurrentVersion\Run
				
				//jdbcUrl = "jdbc:hsqldb:hsql://143.107.162.157/remoto";
				jdbcUrl = "jdbc:hsqldb:hsql://localhost/remoto";
				
				String path = ReMoto.path;
				
				int startIndex = path.indexOf(".meta");
				int endIndex = path.indexOf("remoto");
				String replacement = "";
				String toBeReplaced = path.substring(startIndex, endIndex);
				
				String path2 = path.replace(toBeReplaced, replacement).replace("\\", "/");
				System.out.println(path2);
				
				System.out.println("CONNECTING TO " + jdbcUrl);
				
				String cmd = "java -cp " + path2 + "WebContent/WEB-INF/lib/hsqldb.jar org.hsqldb.Server -database.0 file:" + path2 + "WebContent/db/remoto -dbname.0 remoto";
				Runtime.getRuntime().exec(cmd);
				
				con = DriverManager.getConnection( jdbcUrl, jdbcUser, jdbcPassword ); 
				
			}
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println( e.getMessage() ); 
		} 
		catch (SQLException e) 
		{
			System.out.println( e.getMessage() ); 
		}
		catch(Exception e) 
		{ 
			System.out.println( e.getMessage() ); 
		} 
	}


	public void close()
	{
	}

	
	public void disconnect()
	{
		try 
		{
			if( rs != null ) rs.close();
			if( st != null ) st.close(); 
			if( pst != null ) pst.close(); 
			
			rs = null;
			st = null;
			pst = null;
			
			if( con != null ) con.close(); 
			
			con = null;
			//System.out.println("BasicDAO.disconnect");
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
}
