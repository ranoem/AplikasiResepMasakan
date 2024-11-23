package database;

import static database.Koneksi.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeDAO {
    private Connection conn;

    public RecipeDAO() {
        try {
            // Mencoba membuat koneksi ke database
            conn = Koneksi.getConnection();
        } catch (SQLException e) {
            // Menangani exception jika terjadi kesalahan koneksi
            System.out.println("Error connecting to the database: " + e.getMessage());
            // Atau bisa menggunakan logger untuk menyimpan log error
        }
    }

    // Menambah data resep baru
    public void addRecipe(String code, String name, String category, String tools, String ingredients, String steps) throws SQLException {
        String sql = "INSERT INTO recipes (code, name, category, tools, ingredients, steps) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, tools);
            stmt.setString(5, ingredients);
            stmt.setString(6, steps);
            stmt.executeUpdate();
        }
    }

    // Mengupdate data resep yang ada
    public void updateRecipe(String code, String name, String category, String tools, String ingredients, String steps) throws SQLException {
        String sql = "UPDATE recipes SET name = ?, category = ?, tools = ?, ingredients = ?, steps = ? WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, tools);
            stmt.setString(4, ingredients);
            stmt.setString(5, steps);
            stmt.setString(6, code);
            stmt.executeUpdate();
        }
    }

    public boolean deleteRecipe(String code) {
        try {
            String sql = "DELETE FROM recipes WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public ResultSet getAllRecipes() {
        try {
            String sql = "SELECT * FROM recipes"; // Ambil semua data resep
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery(); // Kembalikan ResultSet
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isCodeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recipes WHERE code = ?";
        // Menggunakan koneksi yang sudah ada, tidak membuat koneksi baru
        Connection conn = getConnection(); 
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Menyiapkan PreparedStatement
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);

            // Menjalankan query dan mendapatkan hasil
            rs = stmt.executeQuery();

            // Memeriksa apakah ada hasil dan membaca nilai COUNT
            if (rs.next()) {
                return rs.getInt(1) > 0; // Jika count > 0, berarti code sudah ada
            }
        } catch (SQLException e) {
            // Menangani exception, jika ada
            e.printStackTrace();
            throw new SQLException("Error checking if code exists", e);
        }

        return false; // Mengembalikan false jika tidak ada hasil
    }
}
