package com.rbs.ui;

import com.rbs.util.Theme;
import com.rbs.util.Validators;
import com.rbs.db.impl.UserDaoImpl;
import com.rbs.service.AuthService;
import com.rbs.service.Session;
import com.rbs.model.User;

import javax.swing.*;
import java.awt.*;

public class AuthDialog extends JDialog {
    public AuthDialog(Frame owner, Runnable onSuccess) {
        super(owner, "Login / Register", true);
        setSize(640, 520);
        setLocationRelativeTo(owner);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLogin(onSuccess));
        tabs.addTab("Register", buildRegister(onSuccess));
        setContentPane(tabs);
    }

    private JPanel buildLogin(Runnable onSuccess) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.PRIMARY_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton login = new JButton("Login");

        // Make fields a reasonable width
        username.setPreferredSize(new Dimension(260, 28));
        password.setPreferredSize(new Dimension(260, 28));
        login.setPreferredSize(new Dimension(120, 30));
        login.setToolTipText("Click to sign in");

        c.gridx=0;c.gridy=0; p.add(label("Username"), c);
        c.gridx=1;c.gridy=0; p.add(username, c);
        c.gridx=0;c.gridy=1; p.add(label("Password"), c);
        c.gridx=1;c.gridy=1; p.add(password, c);
    // Align login button to the right
    c.gridx=1;c.gridy=2; c.anchor = GridBagConstraints.EAST; p.add(login, c);

        login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent _e) {
                if (username.getText().isBlank() || password.getPassword().length==0) {
                    JOptionPane.showMessageDialog(AuthDialog.this, "Enter credentials.");
                    return;
                }
                AuthService svc = new AuthService(new UserDaoImpl());
                User u = svc.login(username.getText().trim(), new String(password.getPassword()));
                if (u == null) { JOptionPane.showMessageDialog(AuthDialog.this, "Invalid username or password"); return; }
                Session.setCurrentUser(u);
                onSuccess.run();
                dispose();
            }
        });

        return p;
    }

    private JPanel buildRegister(Runnable onSuccess) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.PRIMARY_BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField name = new JTextField();
        JSpinner age = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male","Female","Other"});
        JTextField phone = new JTextField("+91 ");
        JTextField address = new JTextField();
        JTextField aadhaar = new JTextField();
        JTextField email = new JTextField();
        JTextField username = new JTextField();
        JPasswordField pass = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
    JButton register = new JButton("Register");

    // Preferred sizes
    int fieldW = 260, fieldH = 26;
    name.setPreferredSize(new Dimension(fieldW, fieldH));
    phone.setPreferredSize(new Dimension(fieldW, fieldH));
    address.setPreferredSize(new Dimension(fieldW, fieldH));
    aadhaar.setPreferredSize(new Dimension(fieldW, fieldH));
    email.setPreferredSize(new Dimension(fieldW, fieldH));
    username.setPreferredSize(new Dimension(fieldW, fieldH));
    pass.setPreferredSize(new Dimension(fieldW, fieldH));
    confirm.setPreferredSize(new Dimension(fieldW, fieldH));
    register.setPreferredSize(new Dimension(120, 30));
    register.setToolTipText("Create a new account");

        int y=0;
        c.gridx=0;c.gridy=y; p.add(label("Name"), c); c.gridx=1; p.add(name, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Age"), c); c.gridx=1; p.add(age, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Gender"), c); c.gridx=1; p.add(gender, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Phone"), c); c.gridx=1; p.add(phone, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Address"), c); c.gridx=1; p.add(address, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Aadhaar"), c); c.gridx=1; p.add(aadhaar, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Email"), c); c.gridx=1; p.add(email, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Username"), c); c.gridx=1; p.add(username, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Password"), c); c.gridx=1; p.add(pass, c); y++;
        c.gridx=0;c.gridy=y; p.add(label("Confirm Password"), c); c.gridx=1; p.add(confirm, c); y++;
    // Align register button to right
    c.gridx=1;c.gridy=y; c.anchor = GridBagConstraints.EAST; p.add(register, c); y++;

        register.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent _e) {
            String phoneVal = phone.getText().trim();
            String aadhaarVal = aadhaar.getText().trim();
            if (!Validators.isNameValid(name.getText())) { alert("Name must be 3–16 chars"); return; }
            if (!Validators.isAdult((Integer)age.getValue())) { alert("Age must be 18 or older"); return; }
            if (!Validators.isPhoneValid(phoneVal)) { alert("Phone format: +91 12345 67890"); return; }
            if (!Validators.isAadhaarValid(aadhaarVal)) { alert("Aadhaar: #### #### ####"); return; }
            if (!Validators.isEmailValid(email.getText())) { alert("Invalid email"); return; }
            if (!new String(pass.getPassword()).equals(new String(confirm.getPassword()))) { alert("Passwords do not match"); return; }
            // Persist user
            User u = new User();
            u.setName(name.getText().trim());
            u.setAge((Integer)age.getValue());
            u.setGender((String)gender.getSelectedItem());
            u.setPhone(phoneVal);
            u.setAddress(address.getText());
            u.setAadhaar(aadhaarVal);
            u.setEmail(email.getText());
            u.setUsername(username.getText().trim());
            AuthService svc = new AuthService(new UserDaoImpl());
            boolean ok = svc.register(u, new String(pass.getPassword()));
            if (!ok) { alert("Registration failed (username may already exist)"); return; }
            Session.setCurrentUser(u);
            onSuccess.run();
            dispose();
            }
        });

        return p;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private void alert(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}



