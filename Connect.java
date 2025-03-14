import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;

public class Connect {
    private static final String URL = "jdbc:mysql://localhost:3306/sakila"; // path of DB
    private static final String USER = "root"; // username
    private static final String PASSWORD = "password"; // password

    public static ArrayList<String[]> executeQuery(String query) {
        ArrayList<String[]> results = new ArrayList<>(); // arraylist to hold the results of query.
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD); // step 1: establish connection
             Statement statement = connection.createStatement(); // step 2: create statement
             ResultSet rs = statement.executeQuery(query)) { // step 3: execute the query
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (form.model.getColumnCount() == 0) {
                for (int i = 1; i <= columnCount; i++) {
                    form.model.addColumn(metaData.getColumnName(i));
                }
            }
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return results;
    }

    public static void updateDatabase(String actorName, String[] columns, String[] newValues) {
        if (columns.length != newValues.length) {
            System.out.println("Error: Column count does not match value count.");
            return;
        }
        StringBuilder queryBuilder = new StringBuilder("UPDATE sakila.contacts SET ");
        for (int i = 0; i < columns.length; i++) {
            queryBuilder.append(columns[i]).append(" = ?");
            if (i < columns.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append("WHERE id = ?");
        String query = queryBuilder.toString(); 
        Connection connection = null;
        PreparedStatement pstmt = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false); // Start transaction
            pstmt = connection.prepareStatement(query);
            // Set the new values dynamically
            for (int i = 0; i < newValues.length; i++) {
                pstmt.setString(i + 1, newValues[i]);
            }
            pstmt.setString(newValues.length + 1, actorName); // Set the WHERE condition
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit(); // Commit only if update was successful
                System.out.println("Update committed successfully for contact: " + actorName);
            } else {
                connection.rollback(); // Rollback if no rows were affected
                System.out.println("Update failed. Transaction rolled back.");
            }


        } catch (SQLException ex) {
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback on error
                }
                System.out.println("SQL Error: " + ex.getMessage());
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    public static void addContact(String name, String phone, String email, String address) {
        String query = "INSERT INTO contacts (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, address);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("New contact added: " + name);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static void deleteRow(int id) {
        String query = "DELETE FROM contacts WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Successfully deleted contact ID: " + id);
            } else {
                System.out.println("No contact found with ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }
}
