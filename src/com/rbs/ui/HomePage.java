package com.rbs.ui;

import com.rbs.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class HomePage extends JPanel {
    private float fade = 0f;

    public HomePage(Runnable onLogin, Runnable onAbout) {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_BG);

        JPanel nav = buildNav(onLogin, onAbout);
        add(nav, BorderLayout.NORTH);

        JPanel hero = buildHero();
        add(hero, BorderLayout.CENTER);

        Timer timer = new Timer(16, e -> {
            fade = Math.min(1f, fade + 0.02f);
            hero.repaint();
        });
        timer.setRepeats(true);
        timer.start();
    }

    private JPanel buildNav(Runnable onLogin, Runnable onAbout) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        nav.setBackground(Theme.SURFACE_BG);

        JLabel brand = new JLabel("RBS");
        brand.setForeground(Theme.TEXT_PRIMARY);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 20f));
        nav.add(brand, BorderLayout.WEST);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        links.setOpaque(false);
        JButton home = linkButton("Home", null);
        JButton login = linkButton("Login", onLogin);
        JButton register = linkButton("Register", onLogin);
        JButton about = linkButton("About", onAbout);
        links.add(home); links.add(login); links.add(register); links.add(about);
        nav.add(links, BorderLayout.EAST);
        return nav;
    }

    private JButton linkButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setForeground(Theme.TEXT_PRIMARY);
        b.setBackground(Theme.ACCENT);
        b.setFocusPainted(false);
        b.addActionListener(e -> { if (action != null) action.run(); });
        // subtle hover
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(Theme.ACCENT_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(Theme.ACCENT); }
        });
        return b;
    }

    private JPanel buildHero() {
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_BG, getWidth(), getHeight(), Theme.CARD_BG);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, fade)));
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(getFont().deriveFont(Font.BOLD, 34f));
                g2.drawString("Welcome to RBS — Book train tickets faster", 64, 140);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 18f));
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString("Plan trips, choose seats, and manage bookings — all in one place.", 64, 180);
            }
        };

        panel.setLayout(null);
        JButton cta = new JButton("Search Trains");
        cta.setBackground(Theme.ACCENT);
        cta.setForeground(Theme.TEXT_PRIMARY);
        cta.setFocusPainted(false);
        cta.setBounds(64, 220, 160, 40);
        panel.add(cta);

        // slide-in effect for CTA
        final int startX = -200; final int targetX = 64;
        cta.setLocation(startX, 220);
        Timer slide = new Timer(12, null);
        slide.addActionListener(ev -> {
            Point p = cta.getLocation();
            int nx = Math.min(targetX, p.x + 18);
            cta.setLocation(nx, p.y);
            if (nx >= targetX) slide.stop();
        });
        slide.start();

        // hover enlarge
        cta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { cta.setBackground(Theme.ACCENT_DARK); cta.setLocation(cta.getX(), cta.getY()-2); }
            public void mouseExited(java.awt.event.MouseEvent e) { cta.setBackground(Theme.ACCENT); cta.setLocation(cta.getX(), cta.getY()+2); }
            public void mouseClicked(java.awt.event.MouseEvent e) { /* trigger search - user flow will handle */ }
        });

        return panel;
    }
}



