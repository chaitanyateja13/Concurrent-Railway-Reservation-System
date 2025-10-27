package com.rbs.ui;

import com.rbs.util.Theme;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    public AppFrame() {
        setTitle("Concurrent Railway Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);

        root.setBackground(Theme.PRIMARY_BG);
        setContentPane(root);

        HomePage home = new HomePage(this::openAuth, this::openAbout);
        root.add(home, "home");

        AuthDialog auth = new AuthDialog(this, this::openDashboard);
        // Dialog shown on demand

        cardLayout.show(root, "home");
    }

    private void openAuth() {
        AuthDialog dialog = new AuthDialog(this, this::openDashboard);
        dialog.setVisible(true);
    }

    private void openAbout() {
    JOptionPane.showMessageDialog(this,
        "Concurrent Railway Reservation System\n" +
            "Desktop client (Java Swing) — uses MySQL for persistence.\n" +
            "Make sure MySQL is running and Database credentials are configured in src/com/rbs/db/Database.java",
        "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openDashboard() {
        DashboardFrame dashboard = new DashboardFrame(() -> {
            // on logout
            cardLayout.show(getContentPane(), "home");
            setVisible(true);
        });
        dashboard.setVisible(true);
        setVisible(false);
    }
}



