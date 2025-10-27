package com.rbs.ui;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public final class PrintUtil {
    private PrintUtil() {}

    public static void printTicket(String ticketText) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("RBS E-Ticket");
        job.setPrintable(new TextPrintable(ticketText));
        try {
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException ignored) { }
    }

    private static class TextPrintable implements Printable {
        private final String text;
        TextPrintable(String text) { this.text = text; }
        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            int x = (int) pf.getImageableX() + 20;
            int y = (int) pf.getImageableY() + 20;
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            for (String line : text.split("\n")) {
                g2.drawString(line, x, y);
                y += 14;
            }
            return PAGE_EXISTS;
        }
    }
}



