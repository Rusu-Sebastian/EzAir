package com.proiect;

import java.sql.*;

public class functii {

    public static void initializareBazaDeDate(){
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ezairusers", "root", "password");

            if (connection != null) {
            System.out.println("Connected to the database successfully!");
            } else {
            System.out.println("Failed to connect to the database.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection to the database failed.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            }
        }
    }

    public static Boolean verificareDateCont(String username, String password) {

        return true;
    }
}
