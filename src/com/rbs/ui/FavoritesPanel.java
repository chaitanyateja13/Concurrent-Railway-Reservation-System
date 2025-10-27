package com.rbs.ui;

import com.rbs.util.Theme;

import javax.swing.*;
import java.awt.*;

public class FavoritesPanel extends JPanel {
    public FavoritesPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_BG);
        JLabel title = new JLabel("Favorites"); title.setForeground(Theme.TEXT_PRIMARY); title.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(); listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); listPanel.setBackground(Theme.PRIMARY_BG);
        String[] items = new String[]{"Bengaluru → Chennai | 3A", "Chennai → Bangalore | SL"};
        for (String it : items) {
            JPanel card = new JPanel(new BorderLayout()); card.setBackground(Theme.CARD_BG); card.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            JLabel lbl = new JLabel(it); lbl.setForeground(Theme.TEXT_PRIMARY);
            JButton re = new JButton("Rebook"); re.setBackground(Theme.ACCENT); re.setForeground(Theme.TEXT_PRIMARY); re.setOpaque(true);
            card.add(lbl, BorderLayout.CENTER); card.add(re, BorderLayout.EAST);
            listPanel.add(card); listPanel.add(Box.createVerticalStrut(8));
        }
        JScrollPane sp = new JScrollPane(listPanel); sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }
}



