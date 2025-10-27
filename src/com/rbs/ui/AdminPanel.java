package com.rbs.ui;

import com.rbs.util.Theme;
import com.rbs.db.impl.UserDaoImpl;
import com.rbs.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private final UserDaoImpl userDao = new UserDaoImpl();

    public AdminPanel() {
        setLayout(new BorderLayout()); setBackground(Theme.PRIMARY_BG);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Trains", crudPanel("Train"));
        tabs.addTab("Users", usersPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // Keep placeholder for trains for now
    private JPanel crudPanel(String entity) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Theme.PRIMARY_BG);
        JPanel form = new JPanel(new GridLayout(0,2,8,8)); form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(label(entity+" ID")); form.add(new JTextField());
        form.add(label("Name")); form.add(new JTextField());
        form.add(label("Meta")); form.add(new JTextField());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        actions.add(new JButton("Add")); actions.add(new JButton("Update")); actions.add(new JButton("Remove"));
        p.add(form, BorderLayout.CENTER); p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private JPanel usersPanel() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Theme.PRIMARY_BG);

        DefaultListModel<User> listModel = new DefaultListModel<>();
        JList<User> userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User u) setText(u.getUsername() + " — " + u.getName());
                return this;
            }
        });

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL; c.gridx = 0; c.gridy = 0;

        JTextField idField = new JTextField(); idField.setEnabled(false);
        JTextField name = new JTextField();
        JSpinner age = new JSpinner(new SpinnerNumberModel(18,0,120,1));
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male","Female","Other"});
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        JTextField aadhaar = new JTextField();
        JTextField email = new JTextField();
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        c.gridx=0; c.gridy=0; form.add(label("ID"), c); c.gridx=1; form.add(idField, c); c.gridy++;
        c.gridx=0; form.add(label("Name"), c); c.gridx=1; form.add(name, c); c.gridy++;
        c.gridx=0; form.add(label("Age"), c); c.gridx=1; form.add(age, c); c.gridy++;
        c.gridx=0; form.add(label("Gender"), c); c.gridx=1; form.add(gender, c); c.gridy++;
        c.gridx=0; form.add(label("Phone"), c); c.gridx=1; form.add(phone, c); c.gridy++;
        c.gridx=0; form.add(label("Address"), c); c.gridx=1; form.add(address, c); c.gridy++;
        c.gridx=0; form.add(label("Aadhaar"), c); c.gridx=1; form.add(aadhaar, c); c.gridy++;
        c.gridx=0; form.add(label("Email"), c); c.gridx=1; form.add(email, c); c.gridy++;
        c.gridx=0; form.add(label("Username"), c); c.gridx=1; form.add(username, c); c.gridy++;
        c.gridx=0; form.add(label("Password (set new)"), c); c.gridx=1; form.add(password, c); c.gridy++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton removeBtn = new JButton("Remove");
        actions.add(addBtn); actions.add(updateBtn); actions.add(removeBtn);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(userList), form);
        split.setDividerLocation(220);
        p.add(split, BorderLayout.CENTER);
        p.add(actions, BorderLayout.SOUTH);

        // Load users
        Runnable reload = () -> {
            listModel.clear();
            List<User> users = userDao.findAll();
            for (User u : users) listModel.addElement(u);
        };
        reload.run();

        userList.addListSelectionListener(e -> {
            User u = userList.getSelectedValue();
            if (u == null) {
                idField.setText(""); name.setText(""); age.setValue(18); gender.setSelectedIndex(0); phone.setText(""); address.setText(""); aadhaar.setText(""); email.setText(""); username.setText(""); password.setText("");
                return;
            }
            idField.setText(String.valueOf(u.getId()));
            name.setText(u.getName()); age.setValue(u.getAge()); gender.setSelectedItem(u.getGender()); phone.setText(u.getPhone()); address.setText(u.getAddress()); aadhaar.setText(u.getAadhaar()); email.setText(u.getEmail()); username.setText(u.getUsername()); password.setText("");
        });

        addBtn.addActionListener(ae -> {
            User u = new User();
            u.setName(name.getText().trim()); u.setAge((Integer)age.getValue()); u.setGender((String)gender.getSelectedItem()); u.setPhone(phone.getText().trim()); u.setAddress(address.getText().trim()); u.setAadhaar(aadhaar.getText().trim()); u.setEmail(email.getText().trim()); u.setUsername(username.getText().trim());
            String pwd = new String(password.getPassword()); if (!pwd.isBlank()) u.setPasswordHash(pwd);
            boolean ok = userDao.create(u);
            JOptionPane.showMessageDialog(this, ok ? "User created." : "Failed to create user.");
            reload.run();
        });

        updateBtn.addActionListener(ae -> {
            User sel = userList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
            sel.setName(name.getText().trim()); sel.setAge((Integer)age.getValue()); sel.setGender((String)gender.getSelectedItem()); sel.setPhone(phone.getText().trim()); sel.setAddress(address.getText().trim()); sel.setAadhaar(aadhaar.getText().trim()); sel.setEmail(email.getText().trim());
            String pwd = new String(password.getPassword()); if (!pwd.isBlank()) sel.setPasswordHash(pwd);
            boolean ok = userDao.update(sel);
            JOptionPane.showMessageDialog(this, ok ? "User updated." : "Failed to update user.");
            reload.run();
        });

        removeBtn.addActionListener(ae -> {
            User sel = userList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
            boolean ok = userDao.delete(sel.getId());
            JOptionPane.showMessageDialog(this, ok ? "User removed." : "Failed to remove user.");
            reload.run();
        });

        return p;
    }

    private JLabel label(String t) { JLabel l = new JLabel(t); l.setForeground(Theme.TEXT_PRIMARY); return l; }
}



