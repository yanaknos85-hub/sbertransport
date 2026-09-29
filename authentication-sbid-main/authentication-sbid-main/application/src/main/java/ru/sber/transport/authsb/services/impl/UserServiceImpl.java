package ru.sber.transport.authsb.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authsb.database.dao.UserRepository;
import ru.sber.transport.authsb.database.model.User;
import ru.sber.transport.authsb.services.UserService;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findBySub(String sub) {
        return userRepository.findBySub(sub);
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

}