package com.github.axelliljendal.finance_tracker.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    // User creation test cases
    @Test
    void createUser_WithValidData_ShouldSucceed() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123@";
        Set<String> roles = Set.of("USER");
        String hashedPassword = "hashedPassword";

        // Mock that will say the email does not exist
        when(userRepository.existsByEmail(email)).thenReturn(false);
        // Mock that will say the password encoder returns a hashed password
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        // Mock that will return the user when saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the method that is being tested
        User result = userService.createUser(email, password, roles);

        // Verify the results
        assertThat(result.getEmail()).isEqualTo(email); // Email should match
        assertThat(result.getPassword()).isEqualTo(hashedPassword); // Password should be hashed
        assertThat(result.getRoles()).isEqualTo(roles); // Roles should match

        // Verify that mocks were actually called
        verify(passwordEncoder).encode(password); // Password was encoded
        verify(userRepository).save(any(User.class)); // User was saved
    }

    // Veryify that passwords are NEVER stored in plain text
    @Test
    void createUser_ShouldEncodePassword() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123@";
        String hashedPassword = "hashedPassword123";

        // Mock that will say the email does not exist
        when(userRepository.existsByEmail(email)).thenReturn(false);
        // Mock that will say the password encoder returns a hashed password
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        // Mock that will return the user when saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User result = userService.createUser(email, password, Set.of("USER"));

        // ASSERT
        assertThat(result.getPassword()).isEqualTo(hashedPassword); // Password should be hashed
        assertThat(result.getPassword()).isNotEqualTo(password); // Password should NOT be plain text
        verify(passwordEncoder).encode(password); // Password was encoded
    }

    // Test that emails must follow proper format
    @Test
    void createUser_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        String invalidEmail = "invalid-email";
        String password = "Password123@";

        // ACT & ASSERT
        // Checks that calling the method throws an exception
        assertThatThrownBy(() -> userService.createUser(invalidEmail, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class) // Should be this type of exception
                .hasMessage("Invalid email format"); // Should have this message

        // Verify that save() was never called because validation failed early
        verify(userRepository, never()).save(any());
    }

    // Test that email cannot be empty
    @Test
    void createUser_WithEmptyMail_ShouldThrowException() {
        // Arrange
        String password = "Password123@";

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser("", password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    // Test that emails cannot be null
    @Test
    void createUser_WithNullEmail_ShouldThrowException() {
        // Arrange
        String password = "Password123@";

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(null, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    // Test that with existing email, exception is thrown
    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        String email = "existing@example.com";
        String password = "Password123@";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");
    }

    // === Password validation tests ===

    // Test that weak passwords are rejected
    @Test
    void createUser_WithWeakPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String weakPassword = "weak"; // Too short and simple

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, weakPassword, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least");
    }

    // Test missing uppercase letter in password
    @Test
    void createUser_WithPasswordMissingUppercase_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "password123@"; // No uppercase letters

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase");
    }

    // Test missing lowercase letter in password
    @Test
    void createUser_WithPasswordMissingLowercase_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "PASSWORD123@"; // No lowercase letters

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void createUser_WithPasswordMissingDigit_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "Password@"; // No digits

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("digit");
    }

    // Test missing special character in password
    @Test
    void createUser_WithPasswordMissingSpecialCharacter_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123"; // No special characters

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, password, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("special character");
    }

    // Test null password
    @Test
    void createUser_WithNullPassword_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(email, null, Set.of("USER")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // === User retrieval tests ===

    // Test finding user by email
    @Test
    void findByEmail_ShouldCallRepository() {
        // Arrange
        String email = "test@example.com";

        User user = new User(email, "hashedPassword", Set.of("USER"));

        // Mock that repository returns the user
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    // Test finding user by email when user does not exist
    @Test
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // Arrange
        String email = "test@example.com";

        // Mock that repository returns empty
        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertThat(result).isEmpty();
    }

    // Test that email is null should return empty
    @Test
    void findByEmail_NullEmail_ShouldReturnEmpty() {
        // Arrange
        String email = null;

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertThat(result).isEmpty();
    }
}