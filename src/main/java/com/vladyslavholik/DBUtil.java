package com.vladyslavholik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBUtil {
    public static Map<Integer, String> products = new HashMap<>();

    static {
        products.put(1, "Yellow T-Shirt");
        products.put(2, "Blue Jeans");
        products.put(3, "Black Pants");
        products.put(4, "White Skirt");
        products.put(5, "Green Pullover");
    }

    public static Connection initialize() throws SQLException {
        var databaseURL = "jdbc:postgresql://localhost/products_db";
        var user = "application";
        var password = "app1234password";
        Connection connection = DriverManager.getConnection(databaseURL, user, password);

        connection.createStatement().execute("DROP TABLE IF EXISTS Products;");
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS Products (id integer PRIMARY KEY, product  varchar(256) NOT NULL, soldItems integer NOT NULL);");

        PreparedStatement insert = connection.prepareStatement("INSERT INTO Products (id, product, soldItems) VALUES (?, ?, ?)");
        for (Integer id : products.keySet()) {
            insert.setInt(1, id);
            insert.setString(2, products.get(id));
            insert.setInt(3, 0);
            insert.addBatch();
        }
        insert.executeBatch();

        return connection;
    }
}
