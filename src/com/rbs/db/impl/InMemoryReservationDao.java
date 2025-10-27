package com.rbs.db.impl;

import com.rbs.db.ReservationDao;
import com.rbs.model.Reservation;
import com.rbs.model.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Very small in-memory ReservationDao to allow running the app without a DB.
 */
public class InMemoryReservationDao implements ReservationDao {
    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<Long, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicLong resIdGen = new AtomicLong(2000);
    private final AtomicLong ticketIdGen = new AtomicLong(5000);

    @Override
    public boolean createReservation(Ticket ticket, Reservation reservation) {
        long tid = ticketIdGen.getAndIncrement();
        ticket.setId(tid);
        tickets.put(tid, ticket);

        long rid = resIdGen.getAndIncrement();
        reservation.setId(rid);
        reservation.setTicketId(tid);
        reservations.put(rid, reservation);
        return true;
    }

    @Override
    public boolean cancelReservation(long reservationId) {
        Reservation r = reservations.get(reservationId);
        if (r == null) return false;
        r.setStatus(Reservation.Status.CANCELLED);
        return true;
    }

    public List<Reservation> findByUserId(long userId) {
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            Ticket t = tickets.get(r.getTicketId());
            if (t != null && t.getUserId() == userId) {
                // attach some ticket info
                r.setPnr(t.getPnr());
                r.setTrainName("(in-memory) Train #"+t.getTrainId());
                r.setJourneyDate(t.getJourneyDate());
                out.add(r);
            }
        }
        return out;
    }

    @Override
    public Reservation findByPnr(String pnr) {
        for (Reservation r : reservations.values()) {
            Ticket t = tickets.get(r.getTicketId());
            if (t != null && pnr.equals(t.getPnr())) {
                r.setPnr(pnr);
                r.setTrainName("(in-memory) Train #"+t.getTrainId());
                r.setJourneyDate(t.getJourneyDate());
                return r;
            }
        }
        return null;
    }

    @Override
    public boolean refundReservation(long reservationId) {
        Reservation r = reservations.get(reservationId);
        if (r == null) return false;
        r.setStatus(Reservation.Status.REFUNDED);
        return true;
    }
}
