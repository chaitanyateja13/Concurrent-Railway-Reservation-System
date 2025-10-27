package com.rbs.ui;

import com.rbs.util.Theme;
import com.rbs.db.impl.ReservationDaoImpl;
import com.rbs.service.Session;
import com.rbs.model.Reservation;
import com.rbs.model.Ticket;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MyBookingsPanel extends JPanel {
    private final JTable table;
    private final ReservationDaoImpl dao = new ReservationDaoImpl();

    public MyBookingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_BG);

        String[] cols = new String[]{"PNR","Train","Date","From","To","Status"};
        Object[][] rows = new Object[][]{{"--","--","--","--","--","--"}};
        table = new JTable(rows, cols);
        table.setFillsViewportHeight(true);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setSelectionBackground(Theme.ACCENT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setBackground(Theme.CARD_BG);
        table.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane sp = new JScrollPane(table);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.PRIMARY_BG);
        JLabel title = new JLabel("My Bookings");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        top.add(title, BorderLayout.WEST);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    public void refresh() {
        if (Session.getCurrentUser() == null) return;
        long uid = Session.getCurrentUser().getId();
        java.util.List<Reservation> res = dao.findByUserId(uid);
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (Reservation r : res) {
            // need ticket PNR and train info - fetch ticket columns via a simple query
            // For now display ticket id as PNR placeholder until ticket join implemented
            rows.add(new Object[]{"PNR-"+r.getTicketId(), "-", "-", "-", "-", r.getStatus().name()});
        }
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(rows.toArray(new Object[0][]), new String[]{"PNR","Train","Date","From","To","Status"});
        table.setModel(m);
    }
}



