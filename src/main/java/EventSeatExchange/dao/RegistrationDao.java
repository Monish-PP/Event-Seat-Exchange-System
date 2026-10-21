package EventSeatExchange.dao;

import EventSeatExchange.model.EventRegistration;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RegistrationDao {
    private final DataSource dataSource;

    public RegistrationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(Connection conn, EventRegistration reg) throws SQLException {
        String sql = "INSERT INTO EVENT_REGISTRATIONS (event_id, student_id, registration_time, seat_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reg.getEventId());
            ps.setInt(2, reg.getStudentId());
            ps.setTimestamp(3, reg.getRegistrationTime() != null ? Timestamp.valueOf(reg.getRegistrationTime()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, reg.getSeatStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        reg.setRegistrationId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public int insert(EventRegistration reg) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return insert(conn, reg);
        }
    }

    public Optional<EventRegistration> findById(int registrationId) throws SQLException {
        String sql = "SELECT r.registration_id, r.event_id, r.student_id, r.registration_time, r.seat_status, " +
                     "e.event_name, e.event_date, e.event_time, e.venue, " +
                     "s.student_name, s.email AS student_email, s.department " +
                     "FROM EVENT_REGISTRATIONS r " +
                     "JOIN EVENTS e ON r.event_id = e.event_id " +
                     "JOIN STUDENTS s ON r.student_id = s.student_id " +
                     "WHERE r.registration_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithJoins(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<EventRegistration> findByIdForUpdate(Connection conn, int registrationId) throws SQLException {
        String sql = "SELECT r.registration_id, r.event_id, r.student_id, r.registration_time, r.seat_status, " +
                     "e.event_name, e.event_date, e.event_time, e.venue, " +
                     "s.student_name, s.email AS student_email, s.department " +
                     "FROM EVENT_REGISTRATIONS r " +
                     "JOIN EVENTS e ON r.event_id = e.event_id " +
                     "JOIN STUDENTS s ON r.student_id = s.student_id " +
                     "WHERE r.registration_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithJoins(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<EventRegistration> findByStudentId(int studentId) throws SQLException {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT r.registration_id, r.event_id, r.student_id, r.registration_time, r.seat_status, " +
                     "e.event_name, e.event_date, e.event_time, e.venue, " +
                     "s.student_name, s.email AS student_email, s.department " +
                     "FROM EVENT_REGISTRATIONS r " +
                     "JOIN EVENTS e ON r.event_id = e.event_id " +
                     "JOIN STUDENTS s ON r.student_id = s.student_id " +
                     "WHERE r.student_id = ? ORDER BY r.registration_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoins(rs));
                }
            }
        }
        return list;
    }

    public List<EventRegistration> findAvailableForTransfer() throws SQLException {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT r.registration_id, r.event_id, r.student_id, r.registration_time, r.seat_status, " +
                     "e.event_name, e.event_date, e.event_time, e.venue, " +
                     "s.student_name, s.email AS student_email, s.department " +
                     "FROM EVENT_REGISTRATIONS r " +
                     "JOIN EVENTS e ON r.event_id = e.event_id " +
                     "JOIN STUDENTS s ON r.student_id = s.student_id " +
                     "WHERE r.seat_status = 'AVAILABLE_FOR_TRANSFER' " +
                     "AND e.event_date >= CURRENT_DATE() " +
                     "ORDER BY e.event_date ASC, r.registration_time ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithJoins(rs));
            }
        }
        return list;
    }

    public Optional<EventRegistration> findActiveRegistration(int eventId, int studentId) throws SQLException {
        String sql = "SELECT r.registration_id, r.event_id, r.student_id, r.registration_time, r.seat_status, " +
                     "e.event_name, e.event_date, e.event_time, e.venue, " +
                     "s.student_name, s.email AS student_email, s.department " +
                     "FROM EVENT_REGISTRATIONS r " +
                     "JOIN EVENTS e ON r.event_id = e.event_id " +
                     "JOIN STUDENTS s ON r.student_id = s.student_id " +
                     "WHERE r.event_id = ? AND r.student_id = ? AND r.seat_status IN ('CONFIRMED', 'AVAILABLE_FOR_TRANSFER')";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithJoins(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean updateStatus(Connection conn, int registrationId, String newStatus) throws SQLException {
        String sql = "UPDATE EVENT_REGISTRATIONS SET seat_status = ? WHERE registration_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, registrationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int registrationId, String newStatus) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return updateStatus(conn, registrationId, newStatus);
        }
    }

    private EventRegistration mapRowWithJoins(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("registration_time");
        Date ed = rs.getDate("event_date");
        Time et = rs.getTime("event_time");

        EventRegistration reg = new EventRegistration(
                rs.getInt("registration_id"),
                rs.getInt("event_id"),
                rs.getInt("student_id"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("seat_status")
        );
        reg.setEventName(rs.getString("event_name"));
        reg.setEventDate(ed != null ? ed.toLocalDate() : null);
        reg.setEventTime(et != null ? et.toLocalTime() : null);
        reg.setVenue(rs.getString("venue"));
        reg.setStudentName(rs.getString("student_name"));
        reg.setStudentEmail(rs.getString("student_email"));
        reg.setDepartment(rs.getString("department"));
        return reg;
    }
}
