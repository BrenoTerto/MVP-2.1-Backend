package com.redepatas.api.cliente.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.parceiro.repositories.PartnerRepository;

@Service
public class AuthorizationServices implements UserDetailsService {

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    PartnerRepository partnerRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Primeiro tenta buscar como cliente
        UserDetails client = clientRepository.findByLogin(login);
        if (client != null) {
            return client;
        }

        // Se não encontrou como cliente, tenta como partner
        UserDetails partner = partnerRepository.findByLogin(login);
        if (partner != null) {
            return partner;
        }

        // Se não encontrou nem como cliente nem como partner
        throw new UsernameNotFoundException("Usuário não encontrado com o login: " + login);
    }

}
