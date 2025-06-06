package quiz;

import java.sql.*;

public class QuizDatabase {
    static final String JDBC_URL = "jdbc:mysql://localhost:3306/quiz_app";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "*Palak2004"; // your MySQL password

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Connected to database successfully!");
            return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Connection failed.");
            e.printStackTrace();
        }
        return null;
    }

    // Save quiz result and update rank
    public static void saveParticipantResult(String name, int score) {
        insertParticipant(name, score);
        assignRanks();
        System.out.println("🎉 Result saved and leaderboard updated for: " + name);
    }

    // Insert participant into database
    public static void insertParticipant(String name, int score) {
        String sql = "INSERT INTO participants (name, score) VALUES (?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Participant inserted: " + name + " with score: " + score);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error inserting participant.");
            e.printStackTrace();
        }
    }

    // Assign rank using SQL window function
    public static void assignRanks() {
        String updateSql = "UPDATE participants p " +
                "JOIN (SELECT id, RANK() OVER (ORDER BY score DESC) AS new_rank FROM participants) ranked " +
                "ON p.id = ranked.id " +
                "SET p.user_rank = ranked.new_rank";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(updateSql);
            System.out.println("🏆 Ranks updated for " + rows + " participants.");
        } catch (SQLException e) {
            System.out.println("❌ Error updating ranks.");
            e.printStackTrace();
        }
    }

    // Display leaderboard
    public static void displayLeaderboard() {
        String sql = "SELECT name, score, user_rank FROM participants ORDER BY user_rank ASC";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n=== 🏅 Leaderboard ===");
            while (rs.next()) {
                System.out.println("Rank " + rs.getInt("user_rank") + ": " + rs.getString("name") + " - " + rs.getInt("score") + " pts");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error displaying leaderboard.");
            e.printStackTrace();
        }
    }

    // MAIN method for demo/testing
    public static void main(String[] args) {
        // Simulate users finishing the quiz
        saveParticipantResult("Avinash", 14);
        saveParticipantResult("Ravi", 11);
        saveParticipantResult("Ayesha", 9);

        // Optional: Show leaderboard
        displayLeaderboard();
    }
}
