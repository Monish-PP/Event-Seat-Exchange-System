package EventSeatExchange.dao;

import EventSeatExchange.model.Student;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentDao {
    private final DataSource dataSource;

    public StudentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(Student student) throws SQLException {
        String sql = "INSERT INTO STUDENTS (student_name, email, department, year_of_study, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getDepartment());
            ps.setInt(4, student.getYearOfStudy());
            ps.setString(5, student.getPassword() != null && !student.getPassword().isEmpty() ? student.getPassword() : "student123");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        student.setStudentId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public Optional<Student> findById(int studentId) throws SQLException {
        String sql = "SELECT student_id, student_name, email, department, year_of_study, password FROM STUDENTS WHERE student_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Student> findByEmail(String email) throws SQLException {
        String sql = "SELECT student_id, student_name, email, department, year_of_study, password FROM STUDENTS WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, student_name, email, department, year_of_study, password FROM STUDENTS ORDER BY student_id ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        String pass = "student123";
        try {
            pass = rs.getString("password");
            if (pass == null || pass.isEmpty()) {
                pass = "student123";
            }
        } catch (Exception ignored) {}

        return new Student(
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("email"),
                rs.getString("department"),
                rs.getInt("year_of_study"),
                pass
        );
    }
}
