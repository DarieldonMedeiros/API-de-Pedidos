package com.darieldon.pedidos.controller;

import com.darieldon.pedidos.dto.request.LoginRequestDTO;
import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    private User mockUser() {
        return User.builder()
                .id(10L)
                .email("darieldon@test.com")
                .password("password")
                .role("ROLE_USER")
                .build();
    }

    @Nested
    @DisplayName("POST: /auth/login")
    class Login {

        @Test
        @DisplayName("Deve retornar token quando credenciais são válidas")
        void shouldReturnTokenWhenCredentialsAreValid() throws Exception {

            User user = mockUser();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtService.generateToken(user.getEmail(), user.getId())).thenReturn("mocked-jwt-token");

            LoginRequestDTO login = new LoginRequestDTO(
                    "darieldon@test.com", "password");

            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Token").value("mocked-jwt-token"));

            verify(authenticationManager).authenticate(any());
            verify(jwtService).generateToken(user.getEmail(), user.getId());
        }

        @Test
        @DisplayName("Deve retornar 401 quando as credenciais são inválidas")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            LoginRequestDTO login = new LoginRequestDTO("errado@test.com", "wrong-password");

            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando principal não é User")
        void shouldThrowWhenPrincipalIsNotUser() throws Exception {

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "string-nao-e-user", null);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            LoginRequestDTO login = new LoginRequestDTO("darieldon@test.com", "password");

            mockMvc.perform(post("/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
