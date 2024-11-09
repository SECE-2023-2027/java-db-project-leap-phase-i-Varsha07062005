package BankingApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/banking_app";
	private static final String USER = "root";
	private static final String PASSWORD = "Achu@007";
	
	public static Connection getConnection() throws SQLException{
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
	
	public static void testConnection() {
		try(Connection con = getConnection()){
			if(con != null) {
				System.out.println("Database connected successfully");
			}
			else {
				System.out.println("Database connection failed");
			}
		}
		catch(SQLException e) {
			System.out.println("Database connection failed");
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		testConnection();
	}
}