package com.ifpe.techifolio.service;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;

import org.bson.types.ObjectId;
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
        if (!aluno.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new Exception("Erro: Formato de email inválido.");
        }
        if (!aluno.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new Exception("Erro: Formato de email inválido.");
        }
        if (aluno.getSenha() == null || aluno.getSenha().isEmpty()) {
            throw new Exception("Erro: Senha não pode ser nula ou vazia.");
        }

        Optional<Aluno> verificaEmail = repository.findByEmail(aluno.getEmail());
        if (verificaEmail.isPresent()) {
            throw new Exception("Erro: Já existe um aluno cadastrado com o email informado.");
        }
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha())); 
        return repository.save(aluno);
    }

    public Aluno updateAluno(ObjectId id, Aluno alunoDetails) throws Exception {
        if (id == null) {
            throw new Exception("Erro: ID do aluno não pode ser nulo.");
        }
        Optional<Aluno> optionalAluno = repository.findById(id);
    
        if (!optionalAluno.isPresent()) {
            throw new Exception("Erro: Aluno não encontrado.");
        }
    
        Aluno existingAluno = optionalAluno.get();
    
        if (alunoDetails.getNome() == null && alunoDetails.getEmail() == null &&
            alunoDetails.getSenha() == null && alunoDetails.getFaculdade() == null) {
            return existingAluno; 
        }

        boolean nomeVazio = alunoDetails.getNome() != null && alunoDetails.getNome().isEmpty();
        boolean emailVazio = alunoDetails.getEmail() != null && alunoDetails.getEmail().isEmpty();
        boolean senhaVazia = alunoDetails.getSenha() != null && alunoDetails.getSenha().isEmpty();

        if (nomeVazio && emailVazio && senhaVazia) {
            throw new Exception("Erro: Nome, senha e email não podem ser nulos ou vazios.");
        }
        if (nomeVazio) {
            throw new Exception("Erro: Nome não pode ser nulo ou vazio.");
        }
        if (emailVazio) {
            throw new Exception("Erro: Email não pode ser nulo ou vazio.");
        }
        if (senhaVazia) {
            throw new Exception("Erro: Senha não pode ser nula ou vazia.");
        }
    
        if (alunoDetails.getNome() != null && !alunoDetails.getNome().isEmpty()) {
            if (!alunoDetails.getNome().matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
                throw new Exception("Erro: Nome contém caracteres inválidos.");
            }
            existingAluno.setNome(alunoDetails.getNome());
        }
    
        if (alunoDetails.getEmail() != null && !alunoDetails.getEmail().isEmpty()) {
            if (!alunoDetails.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new Exception("Erro: Formato de email inválido.");
            }
            Optional<Aluno> verificaEmail = repository.findByEmail(alunoDetails.getEmail());
            if (verificaEmail.isPresent() && !verificaEmail.get().getId().equals(existingAluno.getId())) {
                throw new Exception("Erro: Já existe um aluno cadastrado com o email informado.");
            }
            existingAluno.setEmail(alunoDetails.getEmail());
        }
    
        if (alunoDetails.getSenha() != null && !alunoDetails.getSenha().isEmpty()) {
            if (!passwordEncoder.matches(alunoDetails.getSenha(), existingAluno.getSenha())) {
                throw new Exception("Erro: Senha incorreta.");
            }
            existingAluno.setSenha(passwordEncoder.encode(alunoDetails.getSenha()));
        }
    
        if (alunoDetails.getFaculdade() != null && !alunoDetails.getFaculdade().isEmpty()) {
            existingAluno.setFaculdade(alunoDetails.getFaculdade());
        }
    
        return repository.save(existingAluno);
    }

    public void recuperarSenha(Aluno aluno) throws Exception {
        if (aluno.getEmail() == null || aluno.getEmail().isEmpty()) {
            throw new Exception("Erro: Email não pode ser nulo ou vazio.");
        }
        if (!aluno.getEmail().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new Exception("Erro: Formato de email inválido.");
        }
    
        Optional<Aluno> optionalAluno = repository.findByEmail(aluno.getEmail());
        if (!optionalAluno.isPresent()) {
            throw new Exception("Erro: Aluno não encontrado com o email informado.");
        }
    
        Aluno existingAluno = optionalAluno.get();
        String novaSenha = PasswordGenerator.generateRandomPassword(); 
        existingAluno.setSenha(passwordEncoder.encode(novaSenha)); 
        repository.save(existingAluno);
    
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