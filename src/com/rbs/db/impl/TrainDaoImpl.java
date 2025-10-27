package com.rbs.db.impl;

import com.rbs.db.Database;
import com.rbs.db.TrainDao;
import com.rbs.model.Train;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrainDaoImpl implements TrainDao {
    
    @Override
    public List<Train> search(String from, String to, LocalDate date, String travelClass, String category) {
        String sql = "SELECT * FROM trains WHERE from_station LIKE ? AND to_station LIKE ? AND ? BETWEEN start_date AND end_date";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Train> trains = new ArrayList<>();
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + from + "%");
            stmt.setString(2, "%" + to + "%");
            stmt.setDate(3, Date.valueOf(date));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                trains.add(mapResultSetToTrain(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching trains: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return trains;
    }
    
    public List<Train> findAll() {
        String sql = "SELECT * FROM trains ORDER BY name";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Train> trains = new ArrayList<>();
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                trains.add(mapResultSetToTrain(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all trains: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return trains;
    }
    
    public boolean create(Train train) {
        String sql = "INSERT INTO trains (name, number, from_station, to_station, departure_time, arrival_time, run_days) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, train.getName());
            stmt.setString(2, train.getNumber());
            stmt.setString(3, train.getFromStation());
            stmt.setString(4, train.getToStation());
            stmt.setTime(5, Time.valueOf(train.getDeparture()));
            stmt.setTime(6, Time.valueOf(train.getArrival()));
            stmt.setString(7, String.join(",", train.getRunDays()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    train.setId(generatedKeys.getLong(1));
                }
                Database.commit(conn);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating train: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    public boolean update(Train train) {
        String sql = "UPDATE trains SET name = ?, number = ?, from_station = ?, to_station = ?, departure_time = ?, arrival_time = ?, run_days = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, train.getName());
            stmt.setString(2, train.getNumber());
            stmt.setString(3, train.getFromStation());
            stmt.setString(4, train.getToStation());
            stmt.setTime(5, Time.valueOf(train.getDeparture()));
            stmt.setTime(6, Time.valueOf(train.getArrival()));
            stmt.setString(7, String.join(",", train.getRunDays()));
            stmt.setLong(8, train.getId());
            
            int rowsAffected = stmt.executeUpdate();
            Database.commit(conn);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating train: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    public boolean delete(long trainId) {
        String sql = "DELETE FROM trains WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, trainId);
            
            int rowsAffected = stmt.executeUpdate();
            Database.commit(conn);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting train: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    private Train mapResultSetToTrain(ResultSet rs) throws SQLException {
        Train train = new Train();
        train.setId(rs.getLong("id"));
        train.setName(rs.getString("name"));
        train.setNumber(rs.getString("number"));
        train.setFromStation(rs.getString("from_station"));
        train.setToStation(rs.getString("to_station"));
        train.setDeparture(rs.getTime("departure_time").toLocalTime());
        train.setArrival(rs.getTime("arrival_time").toLocalTime());
        
        String runDaysStr = rs.getString("run_days");
        if (runDaysStr != null) {
            train.setRunDays(List.of(runDaysStr.split(",")));
        }
        
        return train;
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
