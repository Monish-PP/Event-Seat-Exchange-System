package EventSeatExchange.dao;

import EventSeatExchange.model.SeatTransferHistory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransferHistoryDao {
    private final DataSource dataSource;

    public TransferHistoryDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(Connection conn, SeatTransferHistory history) throws SQLException {
        String sql = "INSERT INTO SEAT_TRANSFER_HISTORY (transfer_id, previous_student_id, new_student_id, transfer_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, history.getTransferId());
            ps.setInt(2, history.getPreviousStudentId());
            ps.setInt(3, history.getNewStudentId());
            ps.setTimestamp(4, history.getTransferTime() != null ? Timestamp.valueOf(history.getTransferTime()) : Timestamp.valueOf(LocalDateTime.now()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        history.setHistoryId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public List<SeatTransferHistory> findAll() throws SQLException {
        List<SeatTransferHistory> list = new ArrayList<>();
        String sql = baseJoinQuery() + " ORDER BY h.transfer_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithJoins(rs));
            }
        }
        return list;
    }

    public List<SeatTransferHistory> findByStudentId(int studentId) throws SQLException {
        List<SeatTransferHistory> list = new ArrayList<>();
        String sql = baseJoinQuery() + " WHERE h.previous_student_id = ? OR h.new_student_id = ? ORDER BY h.transfer_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoins(rs));
                }
            }
        }
        return list;
    }

    private String baseJoinQuery() {
        return "SELECT h.history_id, h.transfer_id, h.previous_student_id, h.new_student_id, h.transfer_time, " +
               "sp.student_name AS prev_name, sp.email AS prev_email, sp.department AS prev_dept, " +
               "sn.student_name AS new_name, sn.email AS new_email, sn.department AS new_dept, " +
               "e.event_id, e.event_name, e.event_date, e.venue " +
               "FROM SEAT_TRANSFER_HISTORY h " +
               "JOIN STUDENTS sp ON h.previous_student_id = sp.student_id " +
               "JOIN STUDENTS sn ON h.new_student_id = sn.student_id " +
               "JOIN SEAT_TRANSFER_REQUESTS tr ON h.transfer_id = tr.transfer_id " +
               "JOIN EVENT_REGISTRATIONS r ON tr.registration_id = r.registration_id " +
               "JOIN EVENTS e ON r.event_id = e.event_id";
    }

    private SeatTransferHistory mapRowWithJoins(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("transfer_time");
        Date ed = rs.getDate("event_date");

        SeatTransferHistory h = new SeatTransferHistory(
                rs.getInt("history_id"),
                rs.getInt("transfer_id"),
                rs.getInt("previous_student_id"),
                rs.getInt("new_student_id"),
                ts != null ? ts.toLocalDateTime() : null
        );
        h.setPreviousStudentName(rs.getString("prev_name"));
        h.setPreviousStudentEmail(rs.getString("prev_email"));
        h.setPreviousDepartment(rs.getString("prev_dept"));
        h.setNewStudentName(rs.getString("new_name"));
        h.setNewStudentEmail(rs.getString("new_email"));
        h.setNewDepartment(rs.getString("new_dept"));
        h.setEventId(rs.getInt("event_id"));
        h.setEventName(rs.getString("event_name"));
        h.setEventDate(ed != null ? ed.toLocalDate() : null);
        h.setVenue(rs.getString("venue"));
        return h;
    }
}
