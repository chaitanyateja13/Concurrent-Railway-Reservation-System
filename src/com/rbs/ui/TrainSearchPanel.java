package com.rbs.ui;

import com.rbs.model.Train;
import com.rbs.service.TrainService;
import com.rbs.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TrainSearchPanel extends JPanel {
    private final JComboBox<String> fromField = new JComboBox<>();
    private final JComboBox<String> toField = new JComboBox<>();
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> classBox = new JComboBox<>(new String[]{"Any Class","Sleeper (SL)","AC 3 Tier (3A)","AC 2 Tier (2A)","AC First (1A)"});
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"General","Tatkal"});
    private final JComboBox<String> sortBox = new JComboBox<>(new String[]{"Date","Time","Duration"});
    private final JPanel results = new JPanel();
    private final java.util.function.Consumer<BookingSelection> onBook;

    private final TrainService trainService = new TrainService(new com.rbs.db.TrainDao() {
        @Override public java.util.List<com.rbs.model.Train> search(String from, String to, java.time.LocalDate date, String travelClass, String category) { return java.util.List.of(); }
    });

    public TrainSearchPanel() { this(null); }

    public TrainSearchPanel(java.util.function.Consumer<BookingSelection> onBook) {
        this.onBook = onBook;
        setLayout(new BorderLayout(0,0));
        setBackground(Theme.PRIMARY_BG);
        populateStationDropdowns();
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildResults(), BorderLayout.CENTER);
    }

    private void populateStationDropdowns() {
        // use demo trains to seed station lists for dropdowns until DB option selected
        java.util.Set<String> froms = new java.util.TreeSet<>();
        java.util.Set<String> tos = new java.util.TreeSet<>();
        for (Train t : TrainService.DemoTrains.bengaluru()) { froms.add(t.getFromStation()); tos.add(t.getToStation()); }
        for (Train t : TrainService.DemoTrains.chennai()) { froms.add(t.getFromStation()); tos.add(t.getToStation()); }
        fromField.removeAllItems(); toField.removeAllItems();
        // Add Chennai and Kochi as primary 'From' suggestions, then other stations
        fromField.addItem("Chennai");
        fromField.addItem("Kochi");
        for (String f : froms) {
            if (!"Chennai".equals(f) && !"Kochi".equals(f)) fromField.addItem(f);
        }

        // 'To' has an Any option to allow broad searches
        toField.addItem("Any");
        for (String t : tos) toField.addItem(t);

        // sensible defaults
        fromField.setSelectedIndex(0); // Chennai by default
        toField.setSelectedIndex(0);   // Any by default
        classBox.setSelectedIndex(0);  // default to Any Class
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBorder(new EmptyBorder(12, 12, 12, 12));
        bar.setBackground(Theme.SURFACE_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; c.weightx = 0.0; bar.add(label("From"), c);
    c.gridx=1; c.gridy=0; c.weightx = 0.4; bar.add(fromField, c);
        c.gridx=2; c.gridy=0; c.weightx = 0.0; bar.add(label("To"), c);
    c.gridx=3; c.gridy=0; c.weightx = 0.4; bar.add(toField, c);
        c.gridx=4; c.gridy=0; c.weightx = 0.0; bar.add(label("Date"), c);
        c.gridx=5; c.gridy=0; c.weightx = 0.2; bar.add(dateSpinner, c);

        c.gridx=0; c.gridy=1; c.weightx = 0.0; bar.add(label("Class"), c);
        c.gridx=1; c.gridy=1; c.weightx = 0.4; bar.add(classBox, c);
        c.gridx=2; c.gridy=1; c.weightx = 0.0; bar.add(label("Category"), c);
        c.gridx=3; c.gridy=1; c.weightx = 0.4; bar.add(categoryBox, c);
        c.gridx=4; c.gridy=1; c.weightx = 0.0; bar.add(label("Sort By"), c);
        c.gridx=5; c.gridy=1; c.weightx = 0.2; bar.add(sortBox, c);

        JButton search = new JButton("Search");
    search.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { doSearch(); } });
        c.gridx=5; c.gridy=2; c.anchor = GridBagConstraints.EAST; bar.add(search, c);
        return bar;
    }

    private JScrollPane buildResults() {
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(Theme.PRIMARY_BG);
        JScrollPane sp = new JScrollPane(results);
        sp.setBorder(null);
        return sp;
    }

    private void doSearch() {
    String from = fromField.getSelectedItem() == null ? "" : (String) fromField.getSelectedItem();
    String to = toField.getSelectedItem() == null ? "" : (String) toField.getSelectedItem();
    LocalDate date = LocalDate.now();
        String clazz;
        if (classBox.getSelectedItem() == null || "Any Class".equals(classBox.getSelectedItem())) {
            clazz = ""; // empty means any class
        } else {
            clazz = switch (classBox.getSelectedIndex()) {
                case 1 -> "SL"; case 2 -> "3A"; case 3 -> "2A"; default -> "1A";
            };
        }
        String cat = (String) categoryBox.getSelectedItem();
        TrainService.SortBy sort = switch (sortBox.getSelectedIndex()) {
            case 1 -> TrainService.SortBy.TIME;
            case 2 -> TrainService.SortBy.DURATION;
            default -> TrainService.SortBy.DATE;
        };
            // Map 'Any' stations to empty string for the search implementation
            if ("Any".equals(from)) from = "";
            if ("Any".equals(to)) to = "";
            List<Train> list = trainService.search(from, to, date, clazz, cat, sort);
        renderResults(list);
    }

    private void renderResults(List<Train> list) {
        results.removeAll();
        if (list == null || list.isEmpty()) {
            results.add(emptyCard("No trains found."));
        } else {
            for (Train t : list) {
                results.add(trainCard(t));
                results.add(Box.createVerticalStrut(12));
            }
        }
        results.revalidate();
        results.repaint();
    }

    private JPanel trainCard(Train t) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_BG);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel(t.getName()+" ("+t.getNumber()+")");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel runs = new JLabel("Runs On: "+String.join("", t.getRunDays()));
        runs.setForeground(Theme.TEXT_SECONDARY);

        JLabel sched = new JLabel(String.format("%s | %s    →    %s | %s",
                t.getDeparture(), t.getFromStation(), t.getArrival(), t.getToStation()));
        sched.setForeground(Theme.TEXT_PRIMARY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(runs);
        left.add(Box.createVerticalStrut(6));
        left.add(sched);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JPanel classesPanel = new JPanel(new GridLayout(2,2,8,8));
        classesPanel.setOpaque(false);
        String[] classes = {"Sleeper (SL)", "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)"};
        ButtonGroup bg = new ButtonGroup();
        java.util.Map<JToggleButton,String> toggleMap = new java.util.HashMap<>();
        for (String cls : classes) {
            JToggleButton tb = new JToggleButton(cls);
            tb.setBackground(Theme.CARD_BG);
            tb.setForeground(Theme.TEXT_PRIMARY);
            tb.setFocusPainted(false);
            tb.setBorder(BorderFactory.createLineBorder(Theme.ACCENT));
            bg.add(tb);
            toggleMap.put(tb, switch (cls) {
                case "Sleeper (SL)" -> "SL";
                case "AC 3 Tier (3A)" -> "3A";
                case "AC 2 Tier (2A)" -> "2A";
                default -> "1A";
            });
            classesPanel.add(tb);
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false);
        JButton bookNow = new JButton("Book Now");
        bookNow.setEnabled(false);
        bottom.add(bookNow);
        right.add(classesPanel, BorderLayout.CENTER);
        right.add(bottom, BorderLayout.SOUTH);

        // enable book when a class is selected
        for (JToggleButton tb : toggleMap.keySet()) {
            tb.addActionListener(ae -> {
                bookNow.setEnabled(bg.getSelection() != null);
            });
        }

        bookNow.addActionListener(ae -> {
            String selectedClass = null;
            for (JToggleButton tb : toggleMap.keySet()) if (tb.isSelected()) selectedClass = toggleMap.get(tb);
            if (selectedClass == null) return;
            if (onBook != null) {
                String category = (String) categoryBox.getSelectedItem();
                java.time.LocalDate date = LocalDate.now();
                BookingSelection sel = new BookingSelection(t, selectedClass, category, date);
                onBook.accept(sel);
            }
        });

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        JLabel note = new JLabel("Please check NTES website or app for actual time before boarding.");
        note.setForeground(Theme.TEXT_SECONDARY);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel emptyCard(String msg) {
        JPanel p = new JPanel();
        p.setBackground(Theme.CARD_BG);
        p.setBorder(new EmptyBorder(24,24,24,24));
        JLabel l = new JLabel(msg);
        l.setForeground(Theme.TEXT_SECONDARY);
        p.add(l);
        return p;
    }

    private JLabel label(String t) { JLabel l = new JLabel(t); l.setForeground(Theme.TEXT_PRIMARY); return l; }
}



