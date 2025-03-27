package com.ifpe.techifolio.service;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extrair informações do usuário
        String email = oAuth2User.getAttribute("email");
        String nome = oAuth2User.getAttribute("name");

        // Verificar se o usuário já existe no banco de dados
        Optional<Aluno> aluno = alunoRepository.findByEmail(email);
        if (!aluno.isPresent()) {
            // Criar um novo usuário
            Aluno novoAluno = new Aluno();
            novoAluno.setNome(nome);
            novoAluno.setEmail(email);
            novoAluno.setSenha(PasswordGenerator.generateRandomPassword()); // Gerar senha automaticamente
            novoAluno.setFaculdade("Faculdade"); // Adicione a faculdade ou outra informação relevante
            alunoRepository.save(novoAluno);
        }

        return oAuth2User;
    }
}