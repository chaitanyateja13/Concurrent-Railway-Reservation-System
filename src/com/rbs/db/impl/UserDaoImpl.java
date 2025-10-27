package com.rbs.db.impl;

import com.rbs.db.Database;
import com.rbs.db.UserDao;
import com.rbs.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.rbs.db.Database;

// Simple in-memory fallback
import java.util.concurrent.atomic.AtomicBoolean;

public class UserDaoImpl implements UserDao {
    
    @Override
    public User findByUsername(String username) {
        if (!Database.isDriverAvailable()) {
            // delegate to in-memory DAO
            InMemoryUserDao mem = SingletonInMemory.getUserDao();
            return mem.findByUsername(username);
        }
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }
    
    @Override
    public boolean create(User user) {
        if (!Database.isDriverAvailable()) {
            InMemoryUserDao mem = SingletonInMemory.getUserDao();
            return mem.create(user);
        }
        String sql = "INSERT INTO users (name, age, gender, phone, address, aadhaar, email, username, password_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getAge());
            stmt.setString(3, user.getGender());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getAadhaar());
            stmt.setString(7, user.getEmail());
            stmt.setString(8, user.getUsername());
            stmt.setString(9, user.getPasswordHash());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
                Database.commit(conn);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    public List<User> findAll() {
        if (!Database.isDriverAvailable()) {
            return SingletonInMemory.getUserDao().findAll();
        }
        String sql = "SELECT * FROM users ORDER BY id";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return users;
    }
    
    public boolean update(User user) {
        if (!Database.isDriverAvailable()) {
            return SingletonInMemory.getUserDao().update(user);
        }
        String sql = "UPDATE users SET name = ?, age = ?, gender = ?, phone = ?, address = ?, aadhaar = ?, email = ?, password_hash = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getAge());
            stmt.setString(3, user.getGender());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getAadhaar());
            stmt.setString(7, user.getEmail());
            stmt.setString(8, user.getPasswordHash());
            stmt.setLong(9, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            Database.commit(conn);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    public boolean delete(long userId) {
        if (!Database.isDriverAvailable()) {
            return SingletonInMemory.getUserDao().delete(userId);
        }
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            Database.commit(conn);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            Database.rollback(conn);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setAge(rs.getInt("age"));
        user.setGender(rs.getString("gender"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setAadhaar(rs.getString("aadhaar"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
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

