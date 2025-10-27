package com.rbs.db.impl;

import com.rbs.db.Database;
import com.rbs.db.ReservationDao;
import com.rbs.model.Reservation;
import com.rbs.model.Ticket;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoImpl implements ReservationDao {
    
    @Override
    public boolean createReservation(Ticket ticket, Reservation reservation) {
        if (!com.rbs.db.Database.isDriverAvailable()) {
            return SingletonInMemory.getReservationDao().createReservation(ticket, reservation);
        }
        Connection conn = null;
        PreparedStatement ticketStmt = null;
        PreparedStatement reservationStmt = null;
        
        try {
            conn = Database.getConnection();
            
            // Insert ticket first
            String ticketSql = "INSERT INTO tickets (user_id, train_id, pnr, travel_class, category, journey_date, passengers, fare) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            ticketStmt = conn.prepareStatement(ticketSql, Statement.RETURN_GENERATED_KEYS);
            
            ticketStmt.setLong(1, ticket.getUserId());
            ticketStmt.setLong(2, ticket.getTrainId());
            ticketStmt.setString(3, ticket.getPnr());
            ticketStmt.setString(4, ticket.getTravelClass());
            ticketStmt.setString(5, ticket.getCategory());
            ticketStmt.setDate(6, Date.valueOf(ticket.getJourneyDate()));
            ticketStmt.setString(7, String.join(",", ticket.getPassengers()));
            ticketStmt.setDouble(8, ticket.getFare());
            
            int ticketRows = ticketStmt.executeUpdate();
            if (ticketRows == 0) {
                Database.rollback(conn);
                return false;
            }
            
            // Get generated ticket ID
            ResultSet ticketKeys = ticketStmt.getGeneratedKeys();
            if (ticketKeys.next()) {
                ticket.setId(ticketKeys.getLong(1));
            }
            
            // Insert reservation
            String reservationSql = "INSERT INTO reservations (ticket_id, status, created_at) VALUES (?, ?, ?)";
            reservationStmt = conn.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS);
            
            reservationStmt.setLong(1, ticket.getId());
            reservationStmt.setString(2, reservation.getStatus().name());
            reservationStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            int reservationRows = reservationStmt.executeUpdate();
            if (reservationRows > 0) {
                ResultSet reservationKeys = reservationStmt.getGeneratedKeys();
                if (reservationKeys.next()) {
                    reservation.setId(reservationKeys.getLong(1));
                }
                Database.commit(conn);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, ticketStmt, null);
            if (reservationStmt != null) {
                try { reservationStmt.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
        return false;
    }
    
    @Override
    public boolean cancelReservation(long reservationId) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, reservationId);
            
            int rowsAffected = stmt.executeUpdate();
            Database.commit(conn);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling reservation: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    public List<Reservation> findByUserId(long userId) {
        if (!com.rbs.db.Database.isDriverAvailable()) {
            return SingletonInMemory.getReservationDao().findByUserId(userId);
        }
    String sql = "SELECT r.*, t.*, tr.name, tr.from_station, tr.to_station FROM reservations r JOIN tickets t ON r.ticket_id = t.id JOIN trains tr ON t.train_id = tr.id WHERE t.user_id = ? ORDER BY r.created_at DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding reservations by user: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return reservations;
    }

    @Override
    public Reservation findByPnr(String pnr) {
        if (!com.rbs.db.Database.isDriverAvailable()) {
            return SingletonInMemory.getReservationDao().findByPnr(pnr);
        }
        String sql = "SELECT r.*, t.* FROM reservations r JOIN tickets t ON r.ticket_id = t.id WHERE t.pnr = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, pnr);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Reservation r = mapResultSetToReservation(rs);
                // attach ticket id
                r.setTicketId(rs.getLong("ticket_id"));
                return r;
            }
        } catch (SQLException e) {
            System.err.println("Error finding reservation by PNR: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    @Override
    public boolean refundReservation(long reservationId) {
        if (!com.rbs.db.Database.isDriverAvailable()) {
            return SingletonInMemory.getReservationDao().refundReservation(reservationId);
        }
        String sql = "UPDATE reservations SET status = 'REFUNDED' WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, reservationId);
            int rows = stmt.executeUpdate();
            Database.commit(conn);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error refunding reservation: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setTicketId(rs.getLong("ticket_id"));
        reservation.setStatus(Reservation.Status.valueOf(rs.getString("status")));
        reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        // optional joined fields
        try {
            reservation.setPnr(rs.getString("pnr"));
        } catch (SQLException ignored) {}
        try { reservation.setTrainName(rs.getString("name")); } catch (SQLException ignored) {}
        try { java.sql.Date jd = rs.getDate("journey_date"); if (jd != null) reservation.setJourneyDate(jd.toLocalDate()); } catch (SQLException ignored) {}
        try { reservation.setFromStation(rs.getString("from_station")); } catch (SQLException ignored) {}
        try { reservation.setToStation(rs.getString("to_station")); } catch (SQLException ignored) {}
        return reservation;
    }
    
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (conn != null) {
            Database.closeConnection(conn);
        }
    }
}
