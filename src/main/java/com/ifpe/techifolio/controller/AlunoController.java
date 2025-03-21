package com.ifpe.techifolio.controller;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;
import com.ifpe.techifolio.service.AlunoService;
import com.ifpe.techifolio.service.PasswordGenerator;

import jakarta.servlet.http.HttpServletRequest;
import com.ifpe.techifolio.dto.ErrorResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import java.util.Map;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AlunoService alunoService;

    @PostMapping
    public ResponseEntity<Object> createAluno(@RequestBody Aluno aluno) {
        try {
            Aluno savedAluno = alunoService.createAluno(aluno);
            return ResponseEntity.status(201).body(savedAluno);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), aluno));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Aluno aluno, HttpServletRequest request) {
        try {
            Map<String, Object> response = alunoService.login(aluno, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            if (e.getMessage().equals("Senha incorreta")) {
                return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage(), aluno));
            } else if (e.getMessage().equals("E-mail não cadastrado")) {
                return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage(), aluno));
            } else {
                return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage(), null));
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> getAlunoById(@PathVariable ObjectId id) {
        Optional<Aluno> aluno = repository.findById(id);
        return aluno.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Aluno> getAllAlunos() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAluno(@PathVariable ObjectId id, @RequestBody Aluno alunoDetails) {
        Optional<Aluno> aluno = repository.findById(id);
        if (aluno.isPresent()) {
            String nullFieldMessage = alunoDetails.getNullFieldMessageAluno();
            if (nullFieldMessage != null) {
                ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, alunoDetails);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            Aluno updatedAluno = aluno.get();
            updatedAluno.setNome(alunoDetails.getNome());
            updatedAluno.setEmail(alunoDetails.getEmail());
            updatedAluno.setSenha(passwordEncoder.encode(alunoDetails.getSenha())); // Criptografar a senha antes de salvar
            updatedAluno.setFaculdade(alunoDetails.getFaculdade());
            repository.save(updatedAluno);
            return ResponseEntity.ok(updatedAluno);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAluno(@PathVariable ObjectId id) {
        Optional<Aluno> aluno = repository.findById(id);
        if (aluno.isPresent()) {
            repository.delete(aluno.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Object> recuperarSenha(@RequestBody Aluno recuperacaoSenhaRequest) {
        if (recuperacaoSenhaRequest.getEmail() == null || recuperacaoSenhaRequest.getEmail().isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("Erro: Email não pode ser nulo ou vazio.", null));
        }
        Optional<Aluno> optionalAluno = repository.findByEmail(recuperacaoSenhaRequest.getEmail());
        if (!optionalAluno.isPresent()) {
            return ResponseEntity.status(404).body(new ErrorResponse("Erro: Aluno não encontrado com o email informado.", recuperacaoSenhaRequest.getEmail()));
        }
        Aluno aluno = optionalAluno.get();
        String novaSenha = PasswordGenerator.generateRandomPassword();
        aluno.setSenha(passwordEncoder.encode(novaSenha));
        repository.save(aluno);
        return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Nova senha: " + novaSenha, aluno));
    }
}