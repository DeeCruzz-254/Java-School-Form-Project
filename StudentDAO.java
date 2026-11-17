import java.sql.*;

/**
 * Data Access Object: all MySQL (JDBC) code lives here.
 * If the database is unreachable, isConnected() returns false and the
 * application keeps working in memory only.
 */
public class StudentDAO {

    // ---- EDIT THESE THREE LINES TO MATCH YOUR MySQL SETUP ----
    private static final String URL =
            "jdbc:mysql://localhost:3307/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "student";
    private static final String PASSWORD = "StudentPass123";
    // -----------------------------------------------------------

    private Connection conn;
    private String lastError = "";

    public StudentDAO() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            conn = null;
            lastError = e.getMessage();
        }
    }

    public boolean isConnected() {
        return conn != null;
    }

    public String getLastError() {
        return lastError;
    }

    /** Inserts a student. Returns true on success. */
    public boolean save(Student s) {
        String sql = "INSERT INTO students (name, admission_no, course, gender) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAdmissionNo());
            ps.setString(3, s.getCourse());
            ps.setString(4, s.getGender());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    /** True if the admission number is already stored. */
    public boolean exists(String admissionNo) {
        if (!isConnected()) return false;
        String sql = "SELECT 1 FROM students WHERE admission_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admissionNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    /** Number of students stored in the database (-1 on error). */
    public int count() {
        if (!isConnected()) return -1;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return -1;
        }
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) { }
    }
}
