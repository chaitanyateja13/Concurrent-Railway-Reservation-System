package com.rbs.db.impl;

import com.rbs.db.UserDao;
import com.rbs.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight in-memory UserDao used when JDBC driver / DB is not available.
 */
public class InMemoryUserDao implements UserDao {
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    @Override
    public User findByUsername(String username) {
        return usersByUsername.get(username);
    }

    @Override
    public boolean create(User user) {
        long id = idGen.getAndIncrement();
        user.setId(id);
        usersById.put(id, user);
        usersByUsername.put(user.getUsername(), user);
        return true;
    }

    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    public boolean update(User user) {
        if (!usersById.containsKey(user.getId())) return false;
        usersById.put(user.getId(), user);
        usersByUsername.put(user.getUsername(), user);
        return true;
    }

    public boolean delete(long userId) {
        User u = usersById.remove(userId);
        if (u != null) {
            usersByUsername.remove(u.getUsername());
            return true;
        }
        return false;
    }
}
