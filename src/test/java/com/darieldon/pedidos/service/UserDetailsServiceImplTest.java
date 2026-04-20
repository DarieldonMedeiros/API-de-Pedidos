package com.darieldon.pedidos.service;

import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Nested
    @DisplayName("loadByUsername()")
    class LoadByUsername{

        @Test
        @DisplayName("Deve retornar UserDetails quando o usuário existe")
        void shouldReturnUserDetailsWhenUserExists() {

            User user = User.builder()
                    .id(1L)
                    .email("darieldon@test.com")
                    .password("password")
                    .role("ROLE_USER")
                    .build();

            when(repository.findByEmail("darieldon@test.com"))
                    .thenReturn(Optional.of(user));

            UserDetails result =  service.loadUserByUsername("darieldon@test.com");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("darieldon@test.com");
            verify(repository).findByEmail("darieldon@test.com");
        }

        @Test
        @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não existe")
        void shouldThrowExceptionWhenUserNotFound() {

            when(repository.findByEmail("naoexiste@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.loadUserByUsername("naoexiste@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("naoexiste@test.com");

            verify(repository).findByEmail("naoexiste@test.com");
        }
    }
}
