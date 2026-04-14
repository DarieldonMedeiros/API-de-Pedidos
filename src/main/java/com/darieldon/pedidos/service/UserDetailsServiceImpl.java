package com.darieldon.pedidos.service;

import com.darieldon.pedidos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado: " + email));
    }
}
