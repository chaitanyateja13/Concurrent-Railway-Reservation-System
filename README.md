# Concurrent Railway Reservation System (Java Swing + MySQL)
## Overview
A desktop IRCTC-like reservation portal built with pure Java Swing and MySQL integration. Features clean, modern UI with blue-gray theme, smooth transitions, and modular OOP design with concurrent booking support.

## Requirements
- **JDK 17+** (recommended)
- **MySQL 8.0+** with MySQL Connector/J
- **MySQL Connector/J** (mysql-connector-java-8.0.33.jar or later)

## Setup Instructions

### 1. Database Setup
```sql
-- Run the provided schema file
mysql -u root -p < database_schema.sql
```

### 2. MySQL Connector Setup
Download MySQL Connector/J from [MySQL Downloads](https://dev.mysql.com/downloads/connector/j/) and place the JAR file inside a `lib/` folder at the project root (for example `lib/mysql-connector-java-8.0.33.jar`). The helper scripts pick up jars from `lib/` automatically.

### 3. Database Configuration
Edit `src/com/rbs/db/Database.java` and update the USERNAME and PASSWORD constants:
```java
private static final String USERNAME = "root"; // set your MySQL username
private static final String PASSWORD = ""; // set your MySQL password
```

### 4. Compile and Run
```bash
# Compile with MySQL connector
javac -cp "mysql-connector-java-8.0.33.jar" -d out -sourcepath src src/com/rbs/App.java,

# Run
java -cp "out:mysql-connector-java-8.0.33.jar" com.rbs.App,
```

## Features Implemented

### ✅ Core Features
- **User Authentication**: Login/Register with validation
- **Train Search**: Search with Bengaluru/Chennai special rules
- **3-Step Booking Flow**: Passenger Details → Review → Payment
- **Concurrent Booking**: Thread-safe seat inventory management
- **E-Ticket Printing**: PDF-style ticket generation
- **Admin Panel**: Train and user management placeholders

### ✅ Database Integration
- **MySQL Schema**: Complete database with proper relationships
- **DAO Pattern**: Clean separation with implementation classes
- **Transaction Management**: Proper commit/rollback handling
- **Connection Pooling**: Efficient database connection management

### ✅ UI Components
- **Modern Theme**: Blue-gray professional design
- **Responsive Layout**: Clean, organized interface
- **Form Validation**: Comprehensive input validation
- **Progress Indicators**: Step-by-step booking flow
- **Smooth Animations**: Fade transitions and visual effects

## Project Structure
```
src/com/rbs/
├── App.java                 # Entry point with Nimbus LAF
├── ui/                      # Swing UI components
│   ├── AppFrame.java        # Main application frame
│   ├── HomePage.java        # Landing page with navigation
│   ├── AuthDialog.java      # Login/Register dialog
│   ├── DashboardFrame.java  # Main dashboard with tabs
│   ├── TrainSearchPanel.java # Train search and results
│   ├── BookingPanel.java    # 3-step booking flow
│   └── PrintUtil.java       # E-ticket printing
├── model/                   # Data models
│   ├── User.java           # User entity
│   ├── Train.java          # Train entity
│   ├── Ticket.java         # Ticket entity
│   ├── Reservation.java    # Reservation entity
│   └── Waitlist.java       # Waitlist entity
├── service/                 # Business logic
│   ├── AuthService.java    # Authentication service
│   ├── TrainService.java   # Train search with special rules
│   └── BookingService.java # Concurrent booking management
├── db/                      # Database layer
│   ├── Database.java       # MySQL connection management
│   ├── UserDao.java        # User DAO interface
│   ├── TrainDao.java       # Train DAO interface
│   ├── ReservationDao.java # Reservation DAO interface
│   └── impl/               # DAO implementations
│       ├── UserDaoImpl.java
│       ├── TrainDaoImpl.java
│       └── ReservationDaoImpl.java
└── util/                    # Utilities
    ├── Theme.java          # UI theme and styling
    └── Validators.java     # Input validation helpers
```

## Special Features

### Train Search Rules
- **Bengaluru Routes**: Shows 3 specific trains (Bengaluru Express, Fast Bengaluru, Bengaluru Train Fast Express)
- **Chennai Routes**: Shows 3 specific trains (Chennai Express, Fast Chennai, Bengaluru Train Fast Chennai)
- **Sorting**: By Date, Time, or Duration
- **Class Selection**: SL, 3A, 2A, 1A with availability

### Concurrent Booking
- **Thread-Safe Inventory**: Uses ReentrantLock for seat management
- **Waitlist Support**: Automatic waitlist when seats unavailable
- **Transaction Safety**: Proper database transaction handling

### Validation Features
- **Phone Format**: +91 12345 67890 with auto-formatting
- **Aadhaar Format**: #### #### #### with spacing
- **Age Validation**: Must be 18+ for registration
- **Email Validation**: Standard email format checking

## Database Schema
The system uses 6 main tables:
- `users` - User accounts and profiles
- `trains` - Train information and schedules
- `tickets` - Booking tickets
- `reservations` - Reservation status and tracking
- `waitlist` - Waitlist management
- `seat_availability` - Real-time seat inventory

## Notes
- No external libraries beyond Swing/AWT, JDBC, and MySQL Connector
- Animations implemented with Swing Timers
- All validations centralized in `Validators` class
- Professional IRCTC-inspired design with modern UX





