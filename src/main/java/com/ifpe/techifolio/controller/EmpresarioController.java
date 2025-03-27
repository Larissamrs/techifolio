package com.ifpe.techifolio.controller;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ifpe.techifolio.entities.Empresario;
import com.ifpe.techifolio.repository.EmpresarioRepository;
import com.ifpe.techifolio.service.PasswordGenerator;
import com.ifpe.techifolio.dto.ErrorResponse;

@RestController
@RequestMapping("/empresarios")
public class EmpresarioController {
    @Autowired
    private EmpresarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<Object> createEmpresario(@RequestBody Empresario empresario) {
        String nullFieldMessage = empresario.getNullFieldMessageEmpresario();
        if (nullFieldMessage != null) {
            ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, empresario);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Optional<Empresario> verificaEmail = repository.findByEmail(empresario.getEmail());
        if (verificaEmail != null) {
            return ResponseEntity.status(409).body(new ErrorResponse("Erro: Já existe um empresário cadastrado com o email informado.", empresario));
        }
        Empresario savedEmpresario = repository.save(empresario);
        return ResponseEntity.status(201).body(savedEmpresario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Empresario empresario, HttpServletRequest request) {
        // Verifique campos obrigatórios
        if (empresario.getEmail() == null || empresario.getEmail().isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("Todos os campos devem ser preenchidos. Insira um e-mail para continuar.", empresario));
        }
        if (empresario.getSenha() == null || empresario.getSenha().isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("Todos os campos devem ser preenchidos. Insira uma senha para continuar.", empresario));
        }

        // Buscar usuário
        Optional<Empresario> optionalEmpresario = repository.findByEmail(empresario.getEmail());
        if (optionalEmpresario.isPresent()) {
            Empresario existingEmpresario = optionalEmpresario.get();
            if (passwordEncoder.matches(empresario.getSenha(), existingEmpresario.getSenha())) {
                try {
                    // Crie um UserDetails personalizado com as informações do empresario
                    User userDetails = new User(
                        existingEmpresario.getEmail(),
                        existingEmpresario.getSenha(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPRESARIO"))
                    );
                    
                    // Use o UserDetails como principal na autenticação
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                    
                    // Adicione as informações adicionais do empresario
                    Map<String, Object> details = new HashMap<>();
                    details.put("nome", existingEmpresario.getNome());
                    details.put("id", existingEmpresario.getId());
                    authToken.setDetails(details);
                    
                    // Configure a autenticação
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Crie a sessão
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    
                    // Retorne os dados do usuário logado
                    return ResponseEntity.ok(existingEmpresario);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body(new ErrorResponse("Erro ao processar autenticação: " + e.getMessage(), null));
                }
            } else {
                return ResponseEntity.status(401).body(new ErrorResponse("Senha incorreta", empresario));
            }
        } else {
            return ResponseEntity.status(404).body(new ErrorResponse("E-mail não cadastrado", empresario));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresario> getEmpresarioById(@PathVariable ObjectId id) {
        Optional<Empresario> empresario = repository.findById(id);
        return empresario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Empresario> getAllEmpresarios() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateEmpresario(@PathVariable ObjectId id, @RequestBody Empresario empresarioDetails) {
        Optional<Empresario> empresario = repository.findById(id);
        if (empresario.isPresent()) {
            String nullFieldMessage = empresarioDetails.getNullFieldMessageEmpresario();
            if (nullFieldMessage != null) {
                ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, empresarioDetails);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            Empresario updatedEmpresario = empresario.get();
            updatedEmpresario.setNome(empresarioDetails.getNome());
            updatedEmpresario.setEmail(empresarioDetails.getEmail());
            updatedEmpresario.setSenha(passwordEncoder.encode(empresarioDetails.getSenha()));
            updatedEmpresario.setEmpresa(empresarioDetails.getEmpresa());
            repository.save(updatedEmpresario);
            return ResponseEntity.ok(updatedEmpresario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmpresario(@PathVariable ObjectId id) {
        Optional<Empresario> empresario = repository.findById(id);
        if (empresario.isPresent()) {
            repository.delete(empresario.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recuperar-senha")//implementar api de envio de email
    public ResponseEntity<Object> recuperarSenha(@RequestParam String email) {
        if(email == null || email.isEmpty()){
            return ResponseEntity.status(400).body(new ErrorResponse("Erro: Email não pode ser nulo ou vazio.", null));
        }
        Optional<Empresario> optionalEmpresario = repository.findByEmail(email);
        if (optionalEmpresario == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Erro: Empresário não encontrado com o email informado.", null));
        }
        Empresario empresario = optionalEmpresario.get();
        String novaSenha = PasswordGenerator.generateRandomPassword();
        empresario.setSenha(passwordEncoder.encode(novaSenha));;
        repository.save(empresario);
        return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Nova senha: " + novaSenha, optionalEmpresario));
    }
}