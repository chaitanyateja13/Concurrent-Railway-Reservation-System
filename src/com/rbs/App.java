package com.rbs;

import com.rbs.ui.AppFrame;
import com.rbs.util.Theme;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.applyNimbusLookAndFeel();
            Theme.initGlobalFont();
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}



