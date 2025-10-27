-- RBS Database Schema for MySQL
-- Run this script to create the database and tables

CREATE DATABASE IF NOT EXISTS rbs;
USE rbs;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL CHECK (age >= 18),
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    aadhaar VARCHAR(14) NOT NULL UNIQUE, -- Format: #### #### ####
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Trains table
CREATE TABLE trains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    number VARCHAR(10) NOT NULL UNIQUE,
    from_station VARCHAR(50) NOT NULL,
    to_station VARCHAR(50) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    run_days VARCHAR(20) NOT NULL, -- Comma separated: M,T,W,T,F,S
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tickets table
CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    train_id BIGINT NOT NULL,
    travel_class ENUM('SL', '3A', '2A', '1A') NOT NULL,
    category ENUM('General', 'Tatkal') NOT NULL,
    journey_date DATE NOT NULL,
    passengers TEXT NOT NULL, -- Comma separated passenger names
    fare DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE
);

-- Reservations table
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    status ENUM('CONFIRMED', 'WAITLISTED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

-- Waitlist table
CREATE TABLE waitlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    position INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

-- Seat availability table (for concurrent booking management)
CREATE TABLE seat_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    travel_class ENUM('SL', '3A', '2A', '1A') NOT NULL,
    category ENUM('General', 'Tatkal') NOT NULL,
    journey_date DATE NOT NULL,
    available_seats INT NOT NULL DEFAULT 0,
    total_seats INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY unique_availability (train_id, travel_class, category, journey_date)
);

-- Insert sample data
INSERT INTO trains (name, number, from_station, to_station, departure_time, arrival_time, run_days, start_date, end_date) VALUES
('Bengaluru Express', 'BE01', 'Any', 'Bengaluru', '19:42:00', '04:43:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31'),
('Fast Bengaluru', 'BE02', 'Any', 'Bengaluru', '18:15:00', '02:50:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31'),
('Bengaluru Train Fast Express', 'BE03', 'Any', 'Bengaluru', '21:30:00', '06:00:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31'),
('Chennai Express', 'CE01', 'Any', 'Chennai', '20:05:00', '05:55:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31'),
('Fast Chennai', 'CE02', 'Any', 'Chennai', '17:40:00', '01:20:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31'),
('Bengaluru Train Fast Chennai', 'CE03', 'Any', 'Chennai', '22:10:00', '07:15:00', 'M,T,W,T,F,S', '2024-01-01', '2024-12-31');

-- Insert sample users for testing
INSERT INTO users (name, age, gender, phone, address, aadhaar, email, username, password_hash) VALUES
('John Doe', 25, 'Male', '+91 98765 43210', '123 Main St, Mumbai', '1234 5678 9012', 'john@example.com', 'john123', 'password123'),
('Jane Smith', 30, 'Female', '+91 87654 32109', '456 Oak Ave, Delhi', '2345 6789 0123', 'jane@example.com', 'jane456', 'password456'),
('Admin User', 35, 'Other', '+91 76543 21098', '789 Admin Rd, Bangalore', '3456 7890 1234', 'admin@rbs.com', 'admin', 'admin123');

-- Insert sample seat availability
INSERT INTO seat_availability (train_id, travel_class, category, journey_date, available_seats, total_seats) 
SELECT t.id, 'SL', 'General', CURDATE() + INTERVAL 1 DAY, 50, 50 FROM trains t
UNION ALL
SELECT t.id, '3A', 'General', CURDATE() + INTERVAL 1 DAY, 30, 30 FROM trains t
UNION ALL
SELECT t.id, '2A', 'General', CURDATE() + INTERVAL 1 DAY, 20, 20 FROM trains t
UNION ALL
SELECT t.id, '1A', 'General', CURDATE() + INTERVAL 1 DAY, 10, 10 FROM trains t;

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_trains_stations ON trains(from_station, to_station);
CREATE INDEX idx_tickets_user ON tickets(user_id);
CREATE INDEX idx_tickets_train ON tickets(train_id);
CREATE INDEX idx_reservations_ticket ON reservations(ticket_id);
CREATE INDEX idx_seat_availability_lookup ON seat_availability(train_id, travel_class, category, journey_date);
