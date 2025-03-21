package com.ifpe.techifolio.service;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AlunoService {

    @Autowired
    private AlunoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Aluno createAluno(Aluno aluno) throws Exception {
        if (aluno.getNome() == null || aluno.getNome().isEmpty()) {
            throw new Exception("Erro: Nome não pode ser nulo ou vazio.");
        }
        if (aluno.getFaculdade() == null || aluno.getFaculdade().isEmpty()) {
            throw new Exception("Erro: Faculdade não pode ser nula ou vazia.");
        }
        if (aluno.getEmail() == null || aluno.getEmail().isEmpty()) {
            throw new Exception("Erro: Email não pode ser nulo ou vazio.");
        }
        if (aluno.getSenha() == null || aluno.getSenha().isEmpty()) {
            throw new Exception("Erro: Senha não pode ser nula ou vazia.");
        }

        Optional<Aluno> verificaEmail = repository.findByEmail(aluno.getEmail());
        if (verificaEmail.isPresent()) {
            throw new Exception("Erro: Já existe um aluno cadastrado com o email informado.");
        }
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha())); // Criptografar a senha antes de salvar
        return repository.save(aluno);
    }

    public Map<String, Object> login(Aluno aluno, HttpServletRequest request) throws Exception {
        if (aluno.getEmail() == null || aluno.getEmail().isEmpty()) {
            throw new Exception("E-mail não informado");
        }

        if (aluno.getSenha() == null || aluno.getSenha().isEmpty()) {
            throw new Exception("Senha não informada");
        }

        Optional<Aluno> optionalAluno = repository.findByEmail(aluno.getEmail());
        if (optionalAluno.isPresent()) {
            Aluno existingAluno = optionalAluno.get();
            if (passwordEncoder.matches(aluno.getSenha(), existingAluno.getSenha())) {
                User userDetails = new User(
                    existingAluno.getEmail(),
                    existingAluno.getSenha(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );

                Map<String, Object> details = new HashMap<>();
                details.put("nome", existingAluno.getNome());
                details.put("id", existingAluno.getId());
                authToken.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authToken);

                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                Map<String, Object> response = new HashMap<>();
                response.put("nome", existingAluno.getNome());
                response.put("email", existingAluno.getEmail());
                return response;
            } else {
                throw new Exception("Senha incorreta");
            }
        } else {
            throw new Exception("E-mail não cadastrado");
        }
    }
}