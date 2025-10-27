package com.rbs.ui;

import com.rbs.util.Theme;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    public DashboardFrame(Runnable onLogout) {
        setTitle("RBS Dashboard");
        setSize(1280, 800);
        setLocationRelativeTo(null);

    // Left navigation + center cards
    JPanel cards = new JPanel(new CardLayout());
    com.rbs.db.impl.ReservationDaoImpl reservationDao = new com.rbs.db.impl.ReservationDaoImpl();
    com.rbs.service.BookingService bookingService = new com.rbs.service.BookingService(reservationDao);
    BookingPanel bookingPanel = new BookingPanel(bookingService);
    TrainSearchPanel searchPanel = new TrainSearchPanel(sel -> {
        // show booking card and prefill
        CardLayout cl = (CardLayout) cards.getLayout();
        bookingPanel.selectTrain(sel);
        cl.show(cards, "booking");
    });

    CancellationPanel cancelPanel = new CancellationPanel();
    FavoritesPanel favPanel = new FavoritesPanel();
    MyBookingsPanel myBookings = new MyBookingsPanel();


    cards.add(searchPanel, "search");
    cards.add(bookingPanel, "booking");
    cards.add(cancelPanel, "cancel");
    cards.add(favPanel, "favorites");
    cards.add(myBookings, "mybookings");

    // refresh MyBookingsPanel after a confirmed booking
    bookingPanel.setOnBooked(() -> { myBookings.refresh(); });

        JButton logout = new JButton("Logout");
        logout.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { dispose(); onLogout.run(); } });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.SURFACE_BG);
        top.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel title = new JLabel("Dashboard");
        title.setForeground(Theme.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        // Left navigation
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.SURFACE_BG);
        nav.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        String[] names = new String[]{"Search","Booking","Cancel/Refund","Favorites","My Bookings"};
        CardLayout cl = (CardLayout) cards.getLayout();
        for (String n : names) {
            JButton b = new JButton(n);
            b.setMaximumSize(new Dimension(180,36));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { cl.show(cards, n.equals("Search")?"search": n.equals("Booking")?"booking": n.equals("Cancel/Refund")?"cancel": n.equals("Favorites")?"favorites": "mybookings"); } });
            nav.add(b); nav.add(Box.createVerticalStrut(8));
        }

        JPanel root = new JPanel(new BorderLayout());
        root.add(top, BorderLayout.NORTH);
        root.add(nav, BorderLayout.WEST);
        root.add(cards, BorderLayout.CENTER);
        setContentPane(root);
    }
}


