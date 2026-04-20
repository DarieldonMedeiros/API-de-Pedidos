package com.darieldon.pedidos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException e retornar 404")
    void shouldHandleResourceNotFoundException() {

        String message = "Pedido não encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        ErrorResponse response = handler.handleNotFound(exception);

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException e retornar 403")
    void shouldHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        ErrorResponse response = handler.handleForbidden(exception);

        assertThat(response.status()).isEqualTo(403);
        assertThat(response.message()).isEqualTo("Acesso negado: " + exception);
    }

    @Test
    @DisplayName("Deve tratar Exception e retornar 500")
    void shouldHandleInternalServerErrorException() {
        Exception exception = new Exception("Erro de teste");

        ErrorResponse response = handler.handleGeneric(exception);

        assertThat(response.status()).isEqualTo(500);
        assertThat(response.message()).isEqualTo("Erro interno do servidor. " + exception);
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException e formatar erros de campo")
    void shouldHandleMethodArgumentNotValidException() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "order", "customerName", "não pode estar em branco");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ErrorResponse response = handler.handleValidation(exception);

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("customerName: não pode estar em branco");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com múltiplos erros de campo")
    void shouldHandleMethodArgumentNotValidExceptionWithMultipleErrors() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError(
                "order", "customerName", "não pode ser vazio");
        FieldError error2 = new FieldError(
                "order", "items", "deve ter ao menos um item");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        ErrorResponse response = handler.handleValidation(exception);

        assertThat(response.status()).isEqualTo(400);
        assertThat(response.message()).contains("customerName", "items");
    }

    @Test
    @DisplayName("Deve tratar BadCredentialsException e retornar 401")
    void shouldHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Unauthorized");

        ErrorResponse response = handler.handleBadCredentials(exception);

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.message()).isEqualTo("Credenciais inválidas: " + exception);
    }
}
