package com.rbs.service;

import com.rbs.db.UserDao;
import com.rbs.model.User;
import com.rbs.db.impl.SingletonInMemory;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean register(User user, String passwordPlain) {
        // Placeholder: hash = passwordPlain (replace with hashing)
        user.setPasswordHash(passwordPlain);
        return userDao.create(user);
    }

    public User login(String username, String passwordPlain) {
        // Special-case: allow a default developer login 'jay' / 'jay'
        if ("jay".equals(username) && "jay".equals(passwordPlain)) {
            User u = userDao.findByUsername("jay");
            if (u == null) {
                // create a default user with minimal fields
                u = new User();
                u.setName("Jay");
                u.setUsername("jay");
                u.setEmail("jay@example.com");
                u.setPasswordHash("jay");
                boolean created = userDao.create(u);
                if (!created) return null;
            }
            return u;
        }

        User u = userDao.findByUsername(username);
        if (u == null) {
            // fallback to in-memory DAO (covers cases where different DAO backends are used)
            try {
                u = SingletonInMemory.getUserDao().findByUsername(username);
            } catch (Exception ex) {
                // ignore
            }
        }
        if (u == null) return null;
        return passwordPlain.equals(u.getPasswordHash()) ? u : null;
    }

    /**
     * Change the password for an existing user and persist to DAO.
     * Returns true if update succeeded.
     */
    public boolean changePassword(User user, String newPasswordPlain) {
        if (user == null) return false;
        user.setPasswordHash(newPasswordPlain);
        return userDao.update(user);
    }
}



