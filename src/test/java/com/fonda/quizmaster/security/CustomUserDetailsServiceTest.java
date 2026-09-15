package com.fonda.quizmaster.security;

import com.fonda.quizmaster.security.user.CustomUserDetailsService;
import com.fonda.quizmaster.user.Role;
import com.fonda.quizmaster.user.User;
import com.fonda.quizmaster.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User activeUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        activeUser = new User("johndoe", "john@example.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(activeUser, "id", 1L);

        inactiveUser = new User("janedoe", "jane@example.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(inactiveUser, "id", 2L);
        inactiveUser.deactivate();
    }

    @Test
    void loadUserById_whenUserExistsAndActive_returnsCustomUserDetails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        var userDetails = userDetailsService.loadUserById(1L);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("johndoe");
        assertThat(userDetails.getEmail()).isEqualTo("john@example.com");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserById_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void loadUserById_whenUserIsInactive_throwsDisabledException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> userDetailsService.loadUserById(2L))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("User account is disabled");
    }

    @Test
    void loadUserByUsername_whenUserExistsAndActive_returnsUserDetails() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(activeUser));

        var userDetails = userDetailsService.loadUserByUsername("johndoe");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown");
    }

    @Test
    void loadUserByUsername_whenUserIsInactive_throwsDisabledException() {
        when(userRepository.findByUsername("janedoe")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("janedoe"))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("User account is disabled");
    }
}
