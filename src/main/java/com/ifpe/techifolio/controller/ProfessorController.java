package com.ifpe.techifolio.controller;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifpe.techifolio.entities.Professor;
import com.ifpe.techifolio.repository.ProfessorRepository;
import com.ifpe.techifolio.service.PasswordGenerator;
import com.ifpe.techifolio.dto.ErrorResponse;

@RestController
@RequestMapping("/professores")
public class ProfessorController {
    @Autowired
    private ProfessorRepository repository;


    @PostMapping
    public ResponseEntity<Object> createProfessor(@RequestBody Professor professor) {
        String nullFieldMessage = professor.getNullFieldMessageProfessor();
        if (nullFieldMessage != null) {
            ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, professor);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Professor verificaEmail = repository.findByEmail(professor.getEmail());
        if (verificaEmail != null) {
            return ResponseEntity.status(409).body(new ErrorResponse("Erro: Já existe um professor cadastrado com o email informado.", professor));
        }
        Professor savedProfessor = repository.save(professor);
        return ResponseEntity.status(201).body(savedProfessor);
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
            updatedProfessor.setSenha(professorDetails.getSenha());
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

    @PostMapping("/login")
    public ResponseEntity<Professor> login(@RequestBody Professor loginRequest) {
        Professor professor = repository.findByEmailAndSenha(loginRequest.getEmail(), loginRequest.getSenha());
        if (professor != null) {
            return ResponseEntity.ok(professor);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/recuperar-senha")//posteriormente implementar api para enviar por email
    public ResponseEntity<Object> recuperarSenha(@RequestParam String email) {
        if(email == null || email.isEmpty()){
            return ResponseEntity.status(400).body(new ErrorResponse("Erro: Email não pode ser nulo ou vazio.", null));
        }
        Professor professor = repository.findByEmail(email);
        if (professor == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Erro: Professor não encontrado com o email informado.", null));
        }
        String novaSenha = PasswordGenerator.generateRandomPassword();
        professor.setSenha(novaSenha);
        repository.save(professor);
        return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Nova senha: " + novaSenha, professor));
    }
}
