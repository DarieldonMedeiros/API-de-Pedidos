package com.darieldon.pedidos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-para-testes-unitarios-apenas");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Deve gerar token não nulo e não vazio")
        void shouldGenerateNonNullToken() {

            String token = jwtService.generateToken("darieldon@test.com", 10L);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Deve gerar token com três partes separadas por ponto")
        void shouldGenerateTokenWithThreeParts() {
            String token = jwtService.generateToken("darieldon@test.com", 10L);

            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername{

        @Test
        @DisplayName("Deve extrair o username do token corretamente")
        void shouldExtractUsernameFromToken() {

            String token = jwtService.generateToken("darieldon@test.com", 10L);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("darieldon@test.com");
        }
    }

    @Nested
    @DisplayName("extractUserId()")
    class ExtractUserId{

        @Test
        @DisplayName("Deve extrair o userId do token corretamente")
        void shouldExtractUserIdFromToken() {

            String token = jwtService.generateToken("darieldon@test.com", 10L);

            Long userId = jwtService.extractUserId(token);

            assertThat(userId).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("isValid()")
    class IsValid{

        @Test
        @DisplayName("Deve retornar verdadeiro para token válido")
        void shouldReturnTrueForValidToken() {

            String token =  jwtService.generateToken("darieldon@test.com", 10L);

            assertThat(jwtService.isValid(token)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar falso para token inválido")
        void shouldReturnFalseForInvalidToken() {

            assertThat(jwtService.isValid("token.invalido.aqui")).isFalse();
        }

        @Test
        @DisplayName("Deve retornar falso para token expirado")
        void shouldReturnFalseForExpiredToken() {

            ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
            String expiredToken = jwtService.generateToken("darieldon@test.com", 10L);

            assertThat(jwtService.isValid(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar falso para token nulo")
        void shouldReturnFalseForNullToken() {

            assertThat(jwtService.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getKey()")
    class GetKey{

        @Test
        @DisplayName("Deve retornar chave não nula")
        void shouldReturnNullForNullKey() {

            assertThat(jwtService.getKey()).isNotNull();
        }
    }
}
