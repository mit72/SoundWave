package com.example.final13;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection {
    private static final String URL = "jdbc:oracle:thin:@193.2.139.22:1521:ers";
    private static final String USER = "filejm";
    private static final String PASSWORD = "filejm";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database connection failed!");
            return null;
        }
    }

    public void preveriConnection(){
        Connection conn = OracleConnection.getConnection();
        if (conn != null) {
            System.out.println("Wow deluje!!!!!!!!!!!!!");
        } else {
            System.out.println("Ne dela :)");
        }
    }
}

