package com.darieldon.pedidos.config;

import com.darieldon.pedidos.service.JwtService;
import com.darieldon.pedidos.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@NullMarked
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Caso 1: Sem header Bearer — deixa o SecurityFilterChain decidir (ex.: 401 em rotas protegidas)
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer", 0, 6)) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer" + espaço + token (RFC 6750 permite "Bearer" case-insensitive)
        if (authHeader.length() < 8 || !Character.isWhitespace(authHeader.charAt(6))) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();

        // Caso 2: Token inválido ou expirado — resposta explícita (evita 403 ambíguo em algumas cadeias)
        if (!jwtService.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Caso 3: Já tem autenticação no contexto
        if(SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        //Registra o usuário autenticado no contexto do Spring Security
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
