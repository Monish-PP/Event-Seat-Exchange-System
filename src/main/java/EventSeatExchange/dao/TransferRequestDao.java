package EventSeatExchange.dao;

import EventSeatExchange.model.SeatTransferRequest;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransferRequestDao {
    private final DataSource dataSource;

    public TransferRequestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(Connection conn, SeatTransferRequest req) throws SQLException {
        String sql = "INSERT INTO SEAT_TRANSFER_REQUESTS (registration_id, requested_by, request_time, transfer_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getRegistrationId());
            ps.setInt(2, req.getRequestedBy());
            ps.setTimestamp(3, req.getRequestTime() != null ? Timestamp.valueOf(req.getRequestTime()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, req.getTransferStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        req.setTransferId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public int insert(SeatTransferRequest req) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return insert(conn, req);
        }
    }

    public Optional<SeatTransferRequest> findById(int transferId) throws SQLException {
        String sql = baseJoinQuery() + " WHERE tr.transfer_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithJoins(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<SeatTransferRequest> findByIdForUpdate(Connection conn, int transferId) throws SQLException {
        String sql = baseJoinQuery() + " WHERE tr.transfer_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithJoins(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<SeatTransferRequest> findByRegistrationId(int registrationId) throws SQLException {
        List<SeatTransferRequest> list = new ArrayList<>();
        String sql = baseJoinQuery() + " WHERE tr.registration_id = ? ORDER BY tr.request_time ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoins(rs));
                }
            }
        }
        return list;
    }

    public List<SeatTransferRequest> findIncomingRequestsForOwner(int ownerStudentId) throws SQLException {
        List<SeatTransferRequest> list = new ArrayList<>();
        String sql = baseJoinQuery() + " WHERE r.student_id = ? ORDER BY tr.request_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoins(rs));
                }
            }
        }
        return list;
    }

    public List<SeatTransferRequest> findOutgoingRequestsByRequester(int requestedByStudentId) throws SQLException {
        List<SeatTransferRequest> list = new ArrayList<>();
        String sql = baseJoinQuery() + " WHERE tr.requested_by = ? ORDER BY tr.request_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestedByStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoins(rs));
                }
            }
        }
        return list;
    }

    public boolean updateStatus(Connection conn, int transferId, String newStatus) throws SQLException {
        String sql = "UPDATE SEAT_TRANSFER_REQUESTS SET transfer_status = ? WHERE transfer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, transferId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int transferId, String newStatus) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return updateStatus(conn, transferId, newStatus);
        }
    }

    public int rejectOtherPendingRequests(Connection conn, int registrationId, int approvedTransferId) throws SQLException {
        String sql = "UPDATE SEAT_TRANSFER_REQUESTS SET transfer_status = 'REJECTED' " +
                     "WHERE registration_id = ? AND transfer_id <> ? AND transfer_status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            ps.setInt(2, approvedTransferId);
            return ps.executeUpdate();
        }
    }

    public boolean existsPendingRequest(int registrationId, int requestedBy) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SEAT_TRANSFER_REQUESTS WHERE registration_id = ? AND requested_by = ? AND transfer_status = 'PENDING'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            ps.setInt(2, requestedBy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String baseJoinQuery() {
        return "SELECT tr.transfer_id, tr.registration_id, tr.requested_by, tr.request_time, tr.transfer_status, " +
               "s_req.student_name AS requester_name, s_req.email AS requester_email, s_req.department AS requester_dept, " +
               "r.student_id AS current_owner_id, s_owner.student_name AS current_owner_name, s_owner.email AS current_owner_email, " +
               "e.event_id, e.event_name, e.event_date, e.venue " +
               "FROM SEAT_TRANSFER_REQUESTS tr " +
               "JOIN EVENT_REGISTRATIONS r ON tr.registration_id = r.registration_id " +
               "JOIN STUDENTS s_req ON tr.requested_by = s_req.student_id " +
               "JOIN STUDENTS s_owner ON r.student_id = s_owner.student_id " +
               "JOIN EVENTS e ON r.event_id = e.event_id";
    }

    private SeatTransferRequest mapRowWithJoins(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("request_time");
        Date ed = rs.getDate("event_date");

        SeatTransferRequest req = new SeatTransferRequest(
                rs.getInt("transfer_id"),
                rs.getInt("registration_id"),
                rs.getInt("requested_by"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("transfer_status")
        );
        req.setRequesterName(rs.getString("requester_name"));
        req.setRequesterEmail(rs.getString("requester_email"));
        req.setRequesterDepartment(rs.getString("requester_dept"));
        req.setCurrentOwnerId(rs.getInt("current_owner_id"));
        req.setCurrentOwnerName(rs.getString("current_owner_name"));
        req.setCurrentOwnerEmail(rs.getString("current_owner_email"));
        req.setEventId(rs.getInt("event_id"));
        req.setEventName(rs.getString("event_name"));
        req.setEventDate(ed != null ? ed.toLocalDate() : null);
        req.setVenue(rs.getString("venue"));
        return req;
    }
}
