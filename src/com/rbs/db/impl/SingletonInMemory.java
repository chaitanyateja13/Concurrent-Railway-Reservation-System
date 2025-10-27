package com.rbs.db.impl;

/**
 * Provides singleton in-memory DAO instances so multiple callers share state.
 */
public final class SingletonInMemory {
    private static final InMemoryUserDao USER_DAO = new InMemoryUserDao();
    private static final InMemoryReservationDao RESERVATION_DAO = new InMemoryReservationDao();

    public static InMemoryUserDao getUserDao() {
        return USER_DAO;
    }

    public static InMemoryReservationDao getReservationDao() {
        return RESERVATION_DAO;
    }
}
