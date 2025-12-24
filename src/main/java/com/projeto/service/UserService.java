package com.projeto.service;

import com.projeto.model.User;
import com.projeto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User save(@NonNull User user) {
        // Here you would add password hashing logic
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null)
            return Optional.empty();
        return userRepository.findByUsername(username);
    }

    public void delete(@NonNull Long id) {
        userRepository.deleteById(id);
    }
}
