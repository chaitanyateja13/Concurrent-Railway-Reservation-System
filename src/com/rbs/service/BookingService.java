package com.rbs.service;

import com.rbs.db.ReservationDao;
import com.rbs.model.Reservation;
import com.rbs.model.Ticket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BookingService {
    private final ReservationDao reservationDao;

    // Concurrent seat inventory per train-class-category-date key
    private final Map<String, Integer> availableSeats = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public BookingService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public void seedAvailability(String key, int seats) {
        availableSeats.put(key, seats);
    }

    private ReentrantLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public boolean book(Ticket ticket, Reservation reservation) {
        String key = inventoryKey(ticket);
        ReentrantLock lock = getLock(key);
        lock.lock();
        try {
            int avail = availableSeats.getOrDefault(key, 0);
            if (avail > 0) {
                availableSeats.put(key, avail - 1);
                reservation.setStatus(Reservation.Status.CONFIRMED);
            } else {
                reservation.setStatus(Reservation.Status.WAITLISTED);
            }
            return reservationDao.createReservation(ticket, reservation);
        } finally {
            lock.unlock();
        }
    }

    public boolean cancel(long reservationId, Ticket ticket) {
        String key = inventoryKey(ticket);
        ReentrantLock lock = getLock(key);
        lock.lock();
        try {
            boolean ok = reservationDao.cancelReservation(reservationId);
            if (ok) {
                availableSeats.merge(key, 1, Integer::sum);
            }
            return ok;
        } finally {
            lock.unlock();
        }
    }

    private String inventoryKey(Ticket t) {
        return t.getTrainId()+"|"+t.getTravelClass()+"|"+t.getCategory()+"|"+t.getJourneyDate();
    }
}



