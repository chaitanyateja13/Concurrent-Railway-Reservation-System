package com.rbs.db;

import com.rbs.model.Train;
import java.time.LocalDate;
import java.util.List;

public interface TrainDao {
    List<Train> search(String from, String to, LocalDate date, String travelClass, String category);
}



