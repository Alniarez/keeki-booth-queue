package de.alniarez.store;

import java.sql.*;
import java.util.*;

public class BookingStore {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    private final Database db;

    public BookingStore(Database db) {
        this.db = db;
    }

    public synchronized Slot book(String date, String time, String name, int maxSlots) {
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement count = conn.prepareStatement(
                    "SELECT COUNT(*) FROM bookings WHERE date = ? AND time = ?")) {
                count.setString(1, date);
                count.setString(2, time);
                ResultSet rs = count.executeQuery();
                rs.next();
                if (rs.getInt(1) >= maxSlots) return null;
            }

            String code = generateUniqueCode(conn);

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO bookings (date, time, name, code) VALUES (?, ?, ?, ?)")) {
                insert.setString(1, date);
                insert.setString(2, time);
                insert.setString(3, name);
                insert.setString(4, code);
                insert.executeUpdate();
            }

            return new Slot(name, code);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to book slot", e);
        }
    }

    public List<Slot> getBookings(String date, String time) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT name, code FROM bookings WHERE date = ? AND time = ? ORDER BY rownum()")) {
            stmt.setString(1, date);
            stmt.setString(2, time);
            ResultSet rs = stmt.executeQuery();
            List<Slot> slots = new ArrayList<>();
            while (rs.next()) {
                slots.add(new Slot(rs.getString("name"), rs.getString("code")));
            }
            return List.copyOf(slots);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get bookings", e);
        }
    }

    public boolean delete(String date, String time, String code) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM bookings WHERE date = ? AND time = ? AND code = ?")) {
            stmt.setString(1, date);
            stmt.setString(2, time);
            stmt.setString(3, code);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete booking", e);
        }
    }

    private String generateUniqueCode(Connection conn) throws SQLException {
        Set<String> existing = new HashSet<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT code FROM bookings")) {
            while (rs.next()) existing.add(rs.getString(1));
        }
        String code;
        do {
            StringBuilder sb = new StringBuilder(5);
            for (int i = 0; i < 5; i++) {
                sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (existing.contains(code));
        return code;
    }
}
