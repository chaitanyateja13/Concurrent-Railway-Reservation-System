package com.rbs.ui;

import com.rbs.util.Theme;

import com.rbs.db.impl.ReservationDaoImpl;
import com.rbs.model.Reservation;
import javax.swing.*;
import java.awt.*;

public class CancellationPanel extends JPanel {
    public CancellationPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PRIMARY_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

    JTextField pnr = new JTextField();
    pnr.setPreferredSize(new Dimension(240,26));
    JButton cancel = new JButton("Request Cancellation");
    JButton refund = new JButton("Request Refund");

    // style
    cancel.setBackground(Theme.ACCENT_DARK); cancel.setForeground(Theme.TEXT_PRIMARY); cancel.setOpaque(true);
    refund.setBackground(Theme.CARD_BG); refund.setForeground(Theme.TEXT_PRIMARY); refund.setOpaque(true);

        c.gridx=0; c.gridy=0; form.add(new JLabel("PNR"), c);
        c.gridx=1; form.add(pnr, c);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
    actions.add(refund); actions.add(cancel);
    c.gridx=1; c.gridy=1; c.anchor = GridBagConstraints.EAST; form.add(actions, c);

        JLabel title = new JLabel("Cancellation & Refund"); title.setForeground(Theme.TEXT_PRIMARY); title.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);

        ReservationDaoImpl dao = new ReservationDaoImpl();
        cancel.addActionListener(e -> {
            String val = pnr.getText().trim();
            if (val.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter PNR"); return; }
            Reservation r = dao.findByPnr(val);
            if (r == null) { JOptionPane.showMessageDialog(this, "PNR not found"); return; }
            boolean ok = dao.cancelReservation(r.getId());
            JOptionPane.showMessageDialog(this, ok?"Cancellation successful":"Cancellation failed");
        });

        refund.addActionListener(e -> {
            String val = pnr.getText().trim();
            if (val.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter PNR"); return; }
            Reservation r = dao.findByPnr(val);
            if (r == null) { JOptionPane.showMessageDialog(this, "PNR not found"); return; }
            boolean ok = dao.refundReservation(r.getId());
            JOptionPane.showMessageDialog(this, ok?"Refund successful":"Refund failed");
        });
    }
}



