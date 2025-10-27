package com.rbs.ui;

import com.rbs.model.Train;
import java.time.LocalDate;

public record BookingSelection(Train train, String travelClass, String category, LocalDate date) {}
