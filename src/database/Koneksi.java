package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection connection;

    // Fungsi untuk mendapatkan koneksi ke database
    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try {
                // URL koneksi database, pastikan sesuaikan dengan nama database Anda
                String url = "jdbc:mysql://localhost:3306/recipe_db"; // Ganti "nama_database" dengan nama database Anda
                String username = "root"; // Ganti dengan username Anda
                String password = ""; // Ganti dengan password Anda
                
                // Menghubungkan aplikasi ke database
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Koneksi Berhasil!");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Koneksi ke database gagal: " + e.getMessage());
            }
        }
        return connection;
    }

    // Fungsi untuk menutup koneksi
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Koneksi ditutup.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
