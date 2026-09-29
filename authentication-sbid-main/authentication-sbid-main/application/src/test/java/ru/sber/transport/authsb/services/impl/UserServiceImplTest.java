package ru.sber.transport.authsb.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.authsb.database.dao.UserRepository;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.database.model.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private static final String USER_SUB = "user-12345";
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .sub(USER_SUB)
                .fullName("Иван Иванов")
                .inn("1234567890")
                .phoneNumber("+7 (900) 123-45-67")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .organization(Organization.builder().id(UUID.randomUUID()).inn("6565757").build())
                .updatedAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();
    }

    @Test
    void findBySub_ShouldReturnUser_WhenExists() {
        when(userRepository.findBySub(USER_SUB)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findBySub(USER_SUB);
        assertThat(result).isPresent();
        assertThat(result.get().getSub()).isEqualTo(USER_SUB);
        assertThat(result.get().getFullName()).isEqualTo("Иван Иванов");
        assertThat(result.get().getInn()).isEqualTo("1234567890");
        assertThat(result.get().getPhoneNumber()).isEqualTo("+7 (900) 123-45-67");
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
        assertThat(result.get().getOrganization()).isNotNull();
        verify(userRepository, times(1)).findBySub(eq(USER_SUB));
    }

    @Test
    void findBySub_ShouldReturnEmpty_WhenNotFound() {
        when(userRepository.findBySub(any())).thenReturn(Optional.empty());
        Optional<User> result = userService.findBySub("unknown-sub");
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findBySub(eq("unknown-sub"));
    }

    @Test
    void save_ShouldSaveAndReturnUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.save(user);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getSub()).isEqualTo(USER_SUB);
        assertThat(result.getFullName()).isEqualTo("Иван Иванов");
        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getSub().equals(USER_SUB) &&
                        savedUser.getFullName().equals("Иван Иванов") &&
                        savedUser.getInn().equals("1234567890")
        ));
    }

    @Test
    void save_ShouldHandleNewUser() {
        User newUser = User.builder()
                .id(null)
                .sub("new-user-sub")
                .fullName("Петр Петров")
                .build();
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .sub("new-user-sub")
                .fullName("Петр Петров")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        User result = userService.save(newUser);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getSub()).isEqualTo("new-user-sub");
        assertThat(result.getFullName()).isEqualTo("Петр Петров");
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository, times(1)).save(argThat(u ->
                u.getId() == null &&
                        u.getSub().equals("new-user-sub") &&
                        u.getFullName().equals("Петр Петров")
        ));
    }
}