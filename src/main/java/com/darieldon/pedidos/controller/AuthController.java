package com.darieldon.pedidos.controller;

import com.darieldon.pedidos.dto.request.LoginRequestDTO;
import com.darieldon.pedidos.dto.response.TokenResponseDTO;
import com.darieldon.pedidos.model.User;
import com.darieldon.pedidos.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Realizar o login e obter o token JWT")
    public TokenResponseDTO login(@RequestBody LoginRequestDTO dto){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.email(),
                        dto.password()
                )
        );

        Object principal = auth.getPrincipal();

        // Coloquei esta verificação para evitar o warning de: Method invocation 'getEmail' may produce 'NullPointerException'
        if(!(principal instanceof User user)) {
            throw new RuntimeException("Usuário inválido");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId()
        );

        return new TokenResponseDTO(token);
    }
}
