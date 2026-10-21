package EventSeatExchange.dao;

import EventSeatExchange.model.Event;
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
public class EventDao {
    private final DataSource dataSource;

    public EventDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insert(Event event) throws SQLException {
        String sql = "INSERT INTO EVENTS (event_name, event_date, event_time, venue, total_seats, available_seats, registration_deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getEventName());
            ps.setDate(2, Date.valueOf(event.getEventDate()));
            ps.setTime(3, Time.valueOf(event.getEventTime()));
            ps.setString(4, event.getVenue());
            ps.setInt(5, event.getTotalSeats());
            ps.setInt(6, event.getAvailableSeats());
            ps.setTimestamp(7, Timestamp.valueOf(event.getRegistrationDeadline()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        event.setEventId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }

    public Optional<Event> findById(int eventId) throws SQLException {
        String sql = "SELECT event_id, event_name, event_date, event_time, venue, total_seats, available_seats, registration_deadline " +
                     "FROM EVENTS WHERE event_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Event> findAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, event_name, event_date, event_time, venue, total_seats, available_seats, registration_deadline " +
                     "FROM EVENTS ORDER BY event_date ASC, event_time ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    public List<Event> findAllUpcoming() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT event_id, event_name, event_date, event_time, venue, total_seats, available_seats, registration_deadline " +
                     "FROM EVENTS WHERE event_date >= CURRENT_DATE() ORDER BY event_date ASC, event_time ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    public boolean decrementAvailableSeats(Connection conn, int eventId) throws SQLException {
        String sql = "UPDATE EVENTS SET available_seats = available_seats - 1 WHERE event_id = ? AND available_seats > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean incrementAvailableSeats(Connection conn, int eventId) throws SQLException {
        String sql = "UPDATE EVENTS SET available_seats = available_seats + 1 WHERE event_id = ? AND available_seats < total_seats";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Event event) throws SQLException {
        String sql = "UPDATE EVENTS SET event_name = ?, event_date = ?, event_time = ?, venue = ?, " +
                     "total_seats = ?, available_seats = ?, registration_deadline = ? WHERE event_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setDate(2, Date.valueOf(event.getEventDate()));
            ps.setTime(3, Time.valueOf(event.getEventTime()));
            ps.setString(4, event.getVenue());
            ps.setInt(5, event.getTotalSeats());
            ps.setInt(6, event.getAvailableSeats());
            ps.setTimestamp(7, Timestamp.valueOf(event.getRegistrationDeadline()));
            ps.setInt(8, event.getEventId());
            return ps.executeUpdate() > 0;
        }
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Date d = rs.getDate("event_date");
        Time t = rs.getTime("event_time");
        Timestamp ts = rs.getTimestamp("registration_deadline");

        LocalDate eventDate = d != null ? d.toLocalDate() : null;
        LocalTime eventTime = t != null ? t.toLocalTime() : null;
        LocalDateTime deadline = ts != null ? ts.toLocalDateTime() : null;

        return new Event(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                eventDate,
                eventTime,
                rs.getString("venue"),
                rs.getInt("total_seats"),
                rs.getInt("available_seats"),
                deadline
        );
    }
}
