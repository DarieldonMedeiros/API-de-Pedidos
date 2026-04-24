package com.darieldon.pedidos.controller;

import com.darieldon.pedidos.dto.request.LoginRequestDTO;
import com.darieldon.pedidos.dto.request.RegisterRequestDTO;
import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.repository.UserRepository;
import com.darieldon.pedidos.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // Request válido
    private RegisterRequestDTO requestDTO () {

        return new RegisterRequestDTO(
                "darieldon@teste.com",
                "senha"
        );
    }

    // Request com email vazio
    private RegisterRequestDTO requestDTOWithEmptyEmail() {
        return new RegisterRequestDTO(
                "",
                "senha"
        );
    }

    // Request com email nulo
    private RegisterRequestDTO requestDTOWithNullEmail() {
        return new RegisterRequestDTO(
                null,
                "senha"
        );
    }

    // Request com senha em branco
    private RegisterRequestDTO requestDTOWithEmptyPassword() {
        return new RegisterRequestDTO(
                "darieldon@test.com",
                ""
        );
    }

    // Request com senha nula
    private RegisterRequestDTO requestDTOWithNullPassword() {
        return new RegisterRequestDTO(
                "darieldon@test.com",
                null
        );
    }

    // Request com letras maiúsculas
    private RegisterRequestDTO requestDTOWithUpperCase() {
        return new RegisterRequestDTO(
                "DARIELDON@test.com",
                "senha"
        );
    }

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

    @Nested
    @DisplayName("POST: auth/register")
    class Register {

        @Test
        @DisplayName("Deve cadastrar usuário e retornar token quando os dados são válidos")
        void shouldRegisterAndReturnTokenWhenDataIsValid() throws Exception {

            User savedUser = User.builder()
                    .id(1L)
                    .email(requestDTO().email())
                    .password("$2a$fake-hash")
                    .role("ROLE_USER")
                    .build();

            when(userRepository.findByEmail(requestDTO().email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(requestDTO().password())).thenReturn("$2a$fake-hash");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(requestDTO().email(), 1L)).thenReturn("jwt-mock");

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.Token").value("jwt-mock"));

            verify(userRepository).findByEmail(requestDTO().email());
            verify(passwordEncoder).encode(requestDTO().password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve retornar 400 quando o email está em branco")
        void shouldReturn400WhenEmailIsBlank() throws Exception {

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTOWithEmptyEmail())))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve retornar 400 quando o email é nulo")
        void shouldReturn400WhenEmailIsNull() throws Exception {

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTOWithNullEmail())))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve retornar 400 quando a senha está em branco")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTOWithEmptyPassword())))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve retornar 400 quando a senha é nula")
        void shouldReturn400WhenPasswordIsNull() throws Exception {

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTOWithNullPassword())))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve retornar 400 quando o email já existe")
        void shouldReturn400WhenEmailAlreadyExists() throws Exception {

            User existingUser = User.builder()
                    .id(99L)
                    .email("darieldon@teste.com")
                    .password("$2a$hash")
                    .role("ROLE_USER")
                    .build();

            when(userRepository.findByEmail("darieldon@teste.com")).thenReturn(Optional.of(existingUser));

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isBadRequest());

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Deve normalizar email (trim + lowercase) antes de salvar")
        void shouldTrimAndLowercaseEmailBeforeSaving() throws Exception {

            User savedUser = User.builder()
                    .id(2L)
                    .email("darieldon@teste.com")
                    .password("$2a$hash")
                    .role("ROLE_USER")
                    .build();

            when(userRepository.findByEmail("DARIELDON@teste.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(requestDTOWithUpperCase().password())).thenReturn("$2a$hash");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken("darieldon@teste.com", 2L)).thenReturn("jwt-2");

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isOk());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User userToSave = captor.getValue();
            assertEquals("darieldon@teste.com", userToSave.getEmail());
            assertEquals("$2a$hash", userToSave.getPassword());

        }

        @Test
        @DisplayName("Deve retornar 500 quando a persistência falha inesperadamente")
        void shouldReturn500WhenRepositorySaveFails() throws Exception {

            when(userRepository.findByEmail(requestDTO().email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(requestDTO().password())).thenReturn("$2a$hash");
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Falha no banco de dados"));

            mockMvc.perform(post("/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO())))
                    .andExpect(status().isInternalServerError());

            verify(jwtService, never()).generateToken(anyString(), anyLong());
        }

    }
}
