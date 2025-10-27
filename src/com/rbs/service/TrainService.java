package com.rbs.service;

import com.rbs.db.TrainDao;
import com.rbs.model.Train;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TrainService {
    private final TrainDao trainDao;

    public enum SortBy { DATE, TIME, DURATION }

    public TrainService(TrainDao trainDao) {
        this.trainDao = trainDao;
    }

    public List<Train> search(String from, String to, LocalDate date, String travelClass, String category, SortBy sortBy) {
        if (to != null) {
            String target = to.toLowerCase(Locale.ROOT).trim();
            if (target.contains("bengaluru") || target.contains("bangalore")) {
                return mockBengaluru(date, sortBy);
            }
            if (target.contains("chennai")) {
                return mockChennai(date, sortBy);
            }
        }
        List<Train> res = trainDao.search(from, to, date, travelClass, category);
        sort(res, sortBy);
        return res;
    }

    private List<Train> mockBengaluru(LocalDate date, SortBy sortBy) {
        List<Train> list = DemoTrains.bengaluru();
        sort(list, sortBy);
        return list;
    }

    private List<Train> mockChennai(LocalDate date, SortBy sortBy) {
        List<Train> list = DemoTrains.chennai();
        sort(list, sortBy);
        return list;
    }

    private void sort(List<Train> list, SortBy by) {
        if (list == null) return;
        if (by == null) return;
        switch (by) {
            case TIME -> list.sort(Comparator.comparing(Train::getDeparture));
            case DURATION -> list.sort(Comparator.comparing(t -> java.time.Duration.between(t.getDeparture(), t.getArrival())));
            default -> {
                // DATE sort irrelevant per-train; leave as-is
            }
        }
    }

    // Local demo data until MySQL wired
    public static class DemoTrains {
        public static List<Train> bengaluru() {
            List<Train> l = new ArrayList<>();
            l.add(build("Bengaluru Express", "BE01", "Any", "Bengaluru", 19, 42, 4, 43));
            l.add(build("Fast Bengaluru", "BE02", "Any", "Bengaluru", 18, 15, 2, 50));
            l.add(build("Bengaluru Train Fast Express", "BE03", "Any", "Bengaluru", 21, 30, 6, 0));
            return l;
        }

        public static List<Train> chennai() {
            List<Train> l = new ArrayList<>();
            l.add(build("Chennai Express", "CE01", "Any", "Chennai", 20, 5, 5, 55));
            l.add(build("Fast Chennai", "CE02", "Any", "Chennai", 17, 40, 1, 20));
            l.add(build("Bengaluru Train Fast Chennai", "CE03", "Any", "Chennai", 22, 10, 7, 15));
            return l;
        }

        private static Train build(String name, String num, String from, String to, int dh, int dm, int ah, int am) {
            Train t = new Train();
            t.setName(name);
            t.setNumber(num);
            t.setFromStation(from);
            t.setToStation(to);
            t.setDeparture(java.time.LocalTime.of(dh, dm));
            t.setArrival(java.time.LocalTime.of(ah, am));
            t.setRunDays(java.util.List.of("M","T","W","T","F","S"));
            return t;
        }
    }
}



