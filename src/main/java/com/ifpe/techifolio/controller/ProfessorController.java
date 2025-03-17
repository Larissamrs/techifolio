package com.ifpe.techifolio.controller;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ifpe.techifolio.entities.Professor;
import com.ifpe.techifolio.repository.ProfessorRepository;
import com.ifpe.techifolio.service.PasswordGenerator;
import com.ifpe.techifolio.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/professores")
public class ProfessorController {
    @Autowired
    private ProfessorRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<Object> createProfessor(@RequestBody Professor professor) {
        String nullFieldMessage = professor.getNullFieldMessageProfessor();
        if (nullFieldMessage != null) {
            ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, professor);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<Professor> verificaEmail = repository.findByEmail(professor.getEmail());
        if (verificaEmail.isPresent()) {
            return ResponseEntity.status(409).body(new ErrorResponse("Erro: Já existe um professor cadastrado com o email informado.", professor));
        }
        professor.setSenha(passwordEncoder.encode(professor.getSenha())); // Criptografar a senha antes de salvar
        Professor savedProfessor = repository.save(professor);
        return ResponseEntity.status(201).body(savedProfessor);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Professor professor, HttpServletRequest request) {
        // Verifique campos obrigatórios
        if (professor.getEmail() == null || professor.getEmail().isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("Todos os campos devem ser preenchidos. Insira um e-mail para continuar.", professor));
        }
        if (professor.getSenha() == null || professor.getSenha().isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("Todos os campos devem ser preenchidos. Insira uma senha para continuar.", professor));
        }

        // Buscar usuário
        Optional<Professor> optionalProfessor = repository.findByEmail(professor.getEmail());
        if (optionalProfessor.isPresent()) {
            Professor existingProfessor = optionalProfessor.get();
            if (passwordEncoder.matches(professor.getSenha(), existingProfessor.getSenha())) {
                try {
                    // Crie um UserDetails personalizado com as informações do professor
                    User userDetails = new User(
                        existingProfessor.getEmail(),
                        existingProfessor.getSenha(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESSOR"))
                    );
                    
                    // Use o UserDetails como principal na autenticação
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                    
                    // Adicione as informações adicionais do professor
                    Map<String, Object> details = new HashMap<>();
                    details.put("nome", existingProfessor.getNome());
                    details.put("id", existingProfessor.getId());
                    authToken.setDetails(details);
                    
                    // Configure a autenticação
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Crie a sessão
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    
                    // Retorne os dados do usuário logado
                    return ResponseEntity.ok(existingProfessor);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body(new ErrorResponse("Erro ao processar autenticação: " + e.getMessage(), null));
                }
            } else {
                return ResponseEntity.status(401).body(new ErrorResponse("Senha incorreta", professor));
            }
        } else {
            return ResponseEntity.status(404).body(new ErrorResponse("E-mail não cadastrado", professor));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> getProfessorById(@PathVariable ObjectId id) {
        Optional<Professor> professor = repository.findById(id);
        return professor.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Professor> getAllProfessores() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProfessor(@PathVariable ObjectId id, @RequestBody Professor professorDetails) {
        Optional<Professor> professor = repository.findById(id);
        if (professor.isPresent()) {
            String nullFieldMessage = professorDetails.getNullFieldMessageProfessor();
            if (nullFieldMessage != null) {
                ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, professorDetails);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            Professor updatedProfessor = professor.get();
            updatedProfessor.setNome(professorDetails.getNome());
            updatedProfessor.setEmail(professorDetails.getEmail());
            updatedProfessor.setSenha(passwordEncoder.encode(professorDetails.getSenha())); // Criptografar a senha antes de salvar
            updatedProfessor.setFaculdade(professorDetails.getFaculdade());
            repository.save(updatedProfessor);
            return ResponseEntity.ok(updatedProfessor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable ObjectId id) {
        Optional<Professor> professor = repository.findById(id);
        if (professor.isPresent()) {
            repository.delete(professor.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Object> recuperarSenha(@RequestParam String email) {
        if(email == null || email.isEmpty()){
            return ResponseEntity.status(400).body(new ErrorResponse("Erro: Email não pode ser nulo ou vazio.", null));
        }
        Optional<Professor> optionalProfessor = repository.findByEmail(email);
        if (!optionalProfessor.isPresent()) {
            return ResponseEntity.status(404).body(new ErrorResponse("Erro: Professor não encontrado com o email informado.", null));
        }
        Professor professor = optionalProfessor.get();
        String novaSenha = PasswordGenerator.generateRandomPassword();
        professor.setSenha(passwordEncoder.encode(novaSenha));
        repository.save(professor);
        return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Nova senha: " + novaSenha, professor));
    }
}
