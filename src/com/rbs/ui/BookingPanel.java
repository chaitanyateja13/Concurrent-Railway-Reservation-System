package com.rbs.ui;

import com.rbs.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.rbs.model.Train;
import com.rbs.service.BookingService;

public class BookingPanel extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final JProgressBar progress = new JProgressBar(0, 3);

    private final PassengerDetailsStep step1 = new PassengerDetailsStep(this::nextFromStep1, this::cancelFlow);
    private final ReviewStep step2 = new ReviewStep(this::nextFromStep2, this::backToStep1);
    private final PaymentStep step3 = new PaymentStep(this::finishSuccess, this::backToStep2);

    private BookingService bookingService;
    private com.rbs.ui.BookingSelection lastSelection;
    private Runnable onBooked;
    // store current fare details shown in PaymentStep
    private double currentBaseFare = 0.0;
    private int currentPaxCount = 1;
    private double currentTotalFare = 0.0;

    public BookingPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY_BG);

        progress.setValue(1);
        progress.setStringPainted(true);
        progress.setString("Passenger Details");

        cards.add(step1, "1");
        cards.add(step2, "2");
        cards.add(step3, "3");

        add(progress, BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        showStep(1);
    }

    /**
     * Pre-select a train and open the booking flow at step 1.
     */
    public BookingPanel(BookingService svc) {
        this();
        this.bookingService = svc;
    }

    public void setOnBooked(Runnable r) { this.onBooked = r; }

    public void selectTrain(com.rbs.ui.BookingSelection sel) {
        Train t = sel.train();
        this.lastSelection = sel;
        // populate step1's selected train and other details
        step1.trainInfo.setText(t.getName() + " (" + t.getNumber() + ") - " + sel.travelClass());
        // generate PNR and fare preview
        String pnr = generatePnr();
        double fare = calcFare(t.getFromStation(), t.getToStation(), sel.travelClass());
        // store in a hidden field in step1 (could be displayed in review later)
        step1.trainInfo.setToolTipText("PNR: " + pnr + " | Fare: " + fare);
        // set boarding options if stations available (try to set selected boarding if matches)
        step1.boarding.setSelectedIndex(0);
        // show selected date somewhere if UI had a date field (not present currently)
        showStep(1);
    }

    private String generatePnr() {
        StringBuilder sb = new StringBuilder();
        java.util.Random r = new java.util.Random();
        for (int i=0;i<10;i++) sb.append((char)('0' + r.nextInt(10)));
        return sb.toString();
    }

    private double calcFare(String from, String to, String travelClass) {
        // very simple demo fare calculation based on class
        double base = 200.0;
        switch (travelClass) {
            case "3A": base = 900.0; break;
            case "2A": base = 1200.0; break;
            case "1A": base = 2200.0; break;
            default: base = 300.0; break;
        }
        return base;
    }

    private void showStep(int i) {
        cardLayout.show(cards, String.valueOf(i));
        progress.setValue(i);
        switch (i) {
            case 1 -> progress.setString("Passenger Details");
            case 2 -> progress.setString("Review Journey");
            case 3 -> progress.setString("Payment");
        }
    }

    private void nextFromStep1(PassengerDetailsStep.Details d) {
        // pass lastSelection (may be null) so review can show from/to
        step2.setDetails(d, this.lastSelection);
        showStep(2);
    }

    private void backToStep1() {
        showStep(1);
    }

    private void nextFromStep2() {
        PassengerDetailsStep.Details d = step2.getDetails();
        double baseFare = 0.0;
        if (lastSelection != null) {
            baseFare = calcFare(lastSelection.train().getFromStation(), lastSelection.train().getToStation(), lastSelection.travelClass());
        } else {
            baseFare = calcFare("","", d == null ? "" : d.paymentMode());
        }
        if (baseFare <= 500.0) baseFare = 501.0;
    int paxCount = d == null ? 1 : d.passengers().size();
    double total = baseFare * Math.max(1, paxCount);
    // store for finishSuccess
    this.currentBaseFare = baseFare;
    this.currentPaxCount = paxCount;
    this.currentTotalFare = total;
        step3.setSummaryAndUpdate(d, this.lastSelection, baseFare, paxCount, total);
        showStep(3);
    }

    private void backToStep2() {
        showStep(2);
    }

    private void finishSuccess() {
        // Build ticket + reservation and persist via BookingService if available
        PassengerDetailsStep.Details d = step2.getDetails();
        String pnr = step1.trainInfo.getToolTipText();
    if (pnr != null && pnr.startsWith("PNR: ")) pnr = pnr.substring(5).split(" ")[0]; else pnr = generatePnr();

    com.rbs.model.Ticket ticket = new com.rbs.model.Ticket();
    // populate ticket from selection and details
    ticket.setUserId(com.rbs.service.Session.getCurrentUser() != null ? com.rbs.service.Session.getCurrentUser().getId() : 0);
    ticket.setTravelClass(lastSelection != null ? lastSelection.travelClass() : d.paymentMode());
    ticket.setCategory(lastSelection != null ? lastSelection.category() : "General");
    ticket.setPassengers(d.passengers());
    ticket.setJourneyDate(lastSelection != null ? lastSelection.date() : java.time.LocalDate.now());
    // use the fare shown in PaymentStep (stored earlier)
    double fare = this.currentTotalFare > 0.0 ? this.currentTotalFare : 0.0;
    if (fare <= 500.0) fare = 501.0;
    ticket.setFare(fare);
    ticket.setPnr(pnr);

        com.rbs.model.Reservation reservation = new com.rbs.model.Reservation();

        boolean persisted = false;
        if (bookingService != null) {
            persisted = bookingService.book(ticket, reservation);
        }

    JOptionPane.showMessageDialog(this, persisted ? "Booking Confirmed (PNR: " + pnr + ")" : "Booking Confirmed (local)");
        String ticketStr = "RBS E-Ticket\n"+
                "PNR: "+pnr+"\n"+
                "Train: "+d.trainInfo()+"\n"+
                "Boarding: "+d.boarding()+"\n"+
                "Passengers: "+d.passengers()+"\n"+
                "Contact: "+d.contactEmail()+" | "+d.contactPhone()+"\n";
        PrintUtil.printTicket(ticketStr);
    if (onBooked != null) onBooked.run();
    showStep(1);
    }

    private void cancelFlow() {
        showStep(1);
    }

    // Step 1: Passenger Details
    static class PassengerDetailsStep extends JPanel {
        record Details(String trainInfo, String boarding, List<String> passengers, String contactEmail, String contactPhone, String paymentMode) {}

        private final JTextField trainInfo = new JTextField("MS RMM EXPRESS (16751)");
        private final JComboBox<String> boarding = new JComboBox<>(new String[]{"TAMBARAM","KUMBAKONAM","CHENNAI EGMORE"});
        private final DefaultListModel<String> paxModel = new DefaultListModel<>();
        private final JList<String> paxList = new JList<>(paxModel);
        private final JTextField paxName = new JTextField();
        private final JSpinner paxAge = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));
        private final JComboBox<String> paxGender = new JComboBox<>(new String[]{"Male","Female","Other"});
        private final JTextField email = new JTextField();
        private final JTextField phone = new JTextField("+91 ");
        private final JRadioButton modeCards = new JRadioButton("Cards/NetBanking/Wallets (\u20B915 + GST)");
        private final JRadioButton modeUpi = new JRadioButton("BHIM/UPI (\u20B910 + GST)");

        public PassengerDetailsStep(java.util.function.Consumer<Details> onNext, Runnable onBack) {
            setLayout(new BorderLayout());
            setBackground(Theme.PRIMARY_BG);

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6,6,6,6);
            c.fill = GridBagConstraints.HORIZONTAL;
            int y=0;
            c.gridx=0;c.gridy=y; form.add(label("Selected Train"), c); c.gridx=1; form.add(trainInfo, c); y++;
            c.gridx=0;c.gridy=y; form.add(label("Boarding Station"), c); c.gridx=1; form.add(boarding, c); y++;

            JPanel paxRow = new JPanel(new GridLayout(1, 0, 6, 0)); paxRow.setOpaque(false);
            paxRow.add(paxName); paxRow.add(paxAge); paxRow.add(paxGender);
            JButton addPax = new JButton("Add Passenger");
            addPax.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String name = paxName.getText().trim();
                    if (name.length() == 0) {
                        JOptionPane.showMessageDialog(PassengerDetailsStep.this, "Please enter passenger name.");
                        return;
                    }
                    paxModel.addElement(name+" | "+paxAge.getValue()+" | "+paxGender.getSelectedItem());
                    paxName.setText("");
                }
            });
            c.gridx=0;c.gridy=y; form.add(label("Passenger"), c); c.gridx=1; form.add(paxRow, c); y++;
            c.gridx=1;c.gridy=y; form.add(addPax, c); y++;
            c.gridx=1;c.gridy=y; form.add(new JScrollPane(paxList), c); y++;

            c.gridx=0;c.gridy=y; form.add(label("Email"), c); c.gridx=1; form.add(email, c); y++;
            c.gridx=0;c.gridy=y; form.add(label("Phone"), c); c.gridx=1; form.add(phone, c); y++;

            ButtonGroup g = new ButtonGroup(); g.add(modeCards); g.add(modeUpi);
            modeUpi.setSelected(true);
            c.gridx=0;c.gridy=y; form.add(label("Payment Mode"), c); c.gridx=1; form.add(modeCards, c); y++;
            c.gridx=1;c.gridy=y; form.add(modeUpi, c); y++;

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
            JButton back = new JButton("Back"); back.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onBack.run(); } });
            JButton next = new JButton("Next"); next.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onNext.accept(buildDetails()); } });
            actions.add(back); actions.add(next);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
        }

        private Details buildDetails() {
            List<String> pax = new ArrayList<>();
            for (int i=0;i<paxModel.size();i++) pax.add(paxModel.get(i));
            String mode = modeUpi.isSelected()?"UPI":"CARDS";
            return new Details(trainInfo.getText(), (String) boarding.getSelectedItem(), pax, email.getText(), phone.getText(), mode);
        }

        private JLabel label(String t) { JLabel l = new JLabel(t); l.setForeground(Theme.TEXT_PRIMARY); return l; }
    }

    // Step 2: Review
    static class ReviewStep extends JPanel {
        private PassengerDetailsStep.Details details;
        private final JEditorPane summary = new JEditorPane();

        public ReviewStep(Runnable onNext, Runnable onBack) {
            setLayout(new BorderLayout());
            setBackground(Theme.ACCENT);
            summary.setContentType("text/html");
            summary.setEditable(false);
            summary.setOpaque(false);
            summary.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            JScrollPane sc = new JScrollPane(summary);
            sc.setBorder(null);
            sc.setOpaque(false);
            sc.getViewport().setOpaque(false);
            add(sc, BorderLayout.CENTER);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
            JButton back = new JButton("Back"); back.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onBack.run(); } });
            JButton next = new JButton("Proceed to Payment"); next.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onNext.run(); } });
            actions.add(back); actions.add(next);
            add(actions, BorderLayout.SOUTH);
        }

        public void setDetails(PassengerDetailsStep.Details d) {
            this.details = d;
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='color:white;font-family:Sans-Serif;padding:18px;background-color:rgb("+Theme.ACCENT.getRed()+","+Theme.ACCENT.getGreen()+","+Theme.ACCENT.getBlue()+");'>");
            sb.append("<div style='text-align:center;max-width:720px;margin:0 auto;'>");
            sb.append("<h2 style='margin:6px 0;color:white;'>Review Journey</h2>");
            sb.append("<div style='font-weight:bold;margin-bottom:8px;'>").append(d.trainInfo()).append("</div>");
            sb.append("<div>Boarding: ").append(d.boarding()).append("</div>");
            sb.append("<div style='margin-top:8px;text-align:center;'>Passengers:<div style='display:inline-block;text-align:left;'><ul style='padding-left:18px;margin:6px 0;'>");
            for (String p : d.passengers()) sb.append("<li style='color:white;'>").append(p).append("</li>");
            sb.append("</ul></div></div>");
            sb.append("<div style='margin-top:8px;'>Contact: ").append(d.contactEmail()).append(" | ").append(d.contactPhone()).append("</div>");
            sb.append("<div style='margin-top:8px;'>Payment Mode: ").append(d.paymentMode()).append("</div>");
            sb.append("</div>");
            sb.append("</body></html>");
            summary.setText(sb.toString());
        }

        public void setDetails(PassengerDetailsStep.Details d, BookingSelection sel) {
            this.details = d;
            String from = sel == null ? "(unknown)" : sel.train().getFromStation();
            String to = sel == null ? "(unknown)" : sel.train().getToStation();
            java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            String dep = sel == null ? "--:--" : sel.train().getDeparture().format(tf);
            String arr = sel == null ? "--:--" : sel.train().getArrival().format(tf);
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='color:white;font-family:Sans-Serif;padding:20px;background-color:rgb("+Theme.ACCENT.getRed()+","+Theme.ACCENT.getGreen()+","+Theme.ACCENT.getBlue()+");'>");
            sb.append("<div style='text-align:center;max-width:800px;margin:0 auto;font-size:14px;'>");
            sb.append("<h2 style='margin:6px 0;color:white;font-size:20px;'>Review Journey</h2>");
            sb.append("<div style='font-weight:bold;margin-bottom:8px;font-size:16px;'>").append(d.trainInfo()).append("</div>");
            sb.append("<div style='margin-bottom:6px;'>From: <b>").append(from).append("</b> (Dep: ").append(dep).append(") &nbsp;&nbsp; To: <b>").append(to).append("</b> (Arr: ").append(arr).append(")</div>");
            sb.append("<div style='margin-top:8px;text-align:center;'>Passengers:<div style='display:inline-block;text-align:left;'><ul style='padding-left:18px;margin:6px 0;font-size:14px;'>");
            for (String p : d.passengers()) sb.append("<li style='color:white;line-height:1.4;'>").append(p).append("</li>");
            sb.append("</ul></div></div>");
            sb.append("<div style='margin-top:8px;font-size:13px;'>Contact: ").append(d.contactEmail()).append(" | ").append(d.contactPhone()).append("</div>");
            sb.append("<div style='margin-top:8px;font-size:13px;'>Payment Mode: ").append(d.paymentMode()).append("</div>");
            sb.append("</div>");
            sb.append("</body></html>");
            summary.setText(sb.toString());
        }

        public PassengerDetailsStep.Details getDetails() { return details; }
    }

    // Step 3: Payment
    static class PaymentStep extends JPanel {
        private final JLabel header = new JLabel();
        private final JTextPane detailsPane = new JTextPane();
        private final JButton payButton = new JButton();

        public PaymentStep(Runnable onSuccess, Runnable onBack) {
            setLayout(new BorderLayout());
            setBackground(Theme.ACCENT.darker());
            header.setForeground(Color.WHITE);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
            header.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            add(header, BorderLayout.NORTH);

            detailsPane.setContentType("text/html");
            detailsPane.setEditable(false);
            detailsPane.setOpaque(false);
            add(detailsPane, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
            JButton back = new JButton("Back"); back.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onBack.run(); } });
            payButton.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { onSuccess.run(); } });
            // style pay button: white text on darker accent for contrast
            payButton.setBackground(Theme.ACCENT.darker());
            payButton.setForeground(Color.WHITE);
            payButton.setOpaque(true);
            payButton.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
            actions.add(back); actions.add(payButton);
            add(actions, BorderLayout.SOUTH);
        }

        public void setSummaryAndUpdate(PassengerDetailsStep.Details d, BookingSelection sel, double baseFare, int paxCount, double totalFare) {
            header.setText("Payment Details");
            String trainInfo = d == null ? "(no details)" : d.trainInfo();
            // boarding info is included via d.boarding() when needed
            String from = sel == null ? "(unknown)" : sel.train().getFromStation();
            String to = sel == null ? "(unknown)" : sel.train().getToStation();
            java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            String dep = sel == null ? "--:--" : sel.train().getDeparture().format(tf);
            String arr = sel == null ? "--:--" : sel.train().getArrival().format(tf);
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='color:white;font-family:Sans-Serif;padding:16px;background-color:rgb("+Theme.ACCENT.darker().getRed()+","+Theme.ACCENT.darker().getGreen()+","+Theme.ACCENT.darker().getBlue()+");'>");
            sb.append("<div style='text-align:center;max-width:680px;margin:0 auto;font-size:14px;'>");
            sb.append("<div style='font-weight:bold;margin-bottom:8px;font-size:16px;'>").append(trainInfo).append("</div>");
            sb.append("<div style='margin-bottom:6px;'>From: <b>").append(from).append("</b> (Dep: ").append(dep).append(") &nbsp;&nbsp; To: <b>").append(to).append("</b> (Arr: ").append(arr).append(")</div>");
            sb.append("<div style='margin-top:12px;'>Base Fare (per passenger): <b>\u20B9").append(String.format("%.2f", baseFare)).append("</b></div>");
            sb.append("<div>Passengers: <b>").append(paxCount).append("</b></div>");
            sb.append("<div style='margin-top:10px;font-size:1.2em;'>Total Fare: <span style='font-weight:bold;'>\u20B9").append(String.format("%.2f", totalFare)).append("</span></div>");
            sb.append("</div>");
            sb.append("</body></html>");
            detailsPane.setText(sb.toString());
            payButton.setText("Pay \u20B9" + String.format("%.2f", totalFare));
        }
    }
}


