package com.ifpe.techifolio.controller;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;
import com.ifpe.techifolio.service.AlunoService;
import jakarta.servlet.http.HttpServletRequest;
import com.ifpe.techifolio.dto.ErrorResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        try {
            Aluno updatedAluno = alunoService.updateAluno(id, alunoDetails);
            return ResponseEntity.ok(updatedAluno);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), alunoDetails));
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
        try {
            alunoService.recuperarSenha(recuperacaoSenhaRequest);
            return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Verifique seu e-mail para a nova senha.", null));
        } catch (Exception e) {
            if (e.getMessage().equals("Erro: Email não pode ser nulo ou vazio.")) {
                return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage(), null));
            } else if (e.getMessage().equals("Erro: Aluno não encontrado com o email informado.")) {
                return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage(), recuperacaoSenhaRequest.getEmail()));
            } else {
                return ResponseEntity.status(500).body(new ErrorResponse("Erro interno ao processar a solicitação.", null));
            }
        }
    }
}