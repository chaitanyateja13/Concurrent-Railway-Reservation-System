package com.rbs.db;

import com.rbs.model.User;
import java.util.List;

public interface UserDao {
    // Basic CRUD operations for User
    User findByUsername(String username);
    boolean create(User user);
    boolean update(User user);
    boolean delete(long userId);
    List<User> findAll();
}



