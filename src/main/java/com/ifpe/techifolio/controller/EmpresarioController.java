package com.ifpe.techifolio.controller;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Object> createEmpresario(@RequestBody Empresario empresario) {
        String nullFieldMessage = empresario.getNullFieldMessageEmpresario();
        if (nullFieldMessage != null) {
            ErrorResponse errorResponse = new ErrorResponse("Erro: " + nullFieldMessage, empresario);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Empresario verificaEmail = repository.findByEmail(empresario.getEmail());
        if (verificaEmail != null) {
            return ResponseEntity.status(409).body(new ErrorResponse("Erro: Já existe um empresário cadastrado com o email informado.", empresario));
        }
        Empresario savedEmpresario = repository.save(empresario);
        return ResponseEntity.status(201).body(savedEmpresario);
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
            updatedEmpresario.setSenha(empresarioDetails.getSenha());
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

    @PostMapping("/login")
    public ResponseEntity<Empresario> login(@RequestBody Empresario loginRequest) {
        Empresario empresario = repository.findByEmailAndSenha(loginRequest.getEmail(), loginRequest.getSenha());
        if (empresario != null) {
            return ResponseEntity.ok(empresario);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/recuperar-senha")//implementar api de envio de email
    public ResponseEntity<Object> recuperarSenha(@RequestParam String email) {
        if(email == null || email.isEmpty()){
            return ResponseEntity.status(400).body(new ErrorResponse("Erro: Email não pode ser nulo ou vazio.", null));
        }
        Empresario empresario = repository.findByEmail(email);
        if (empresario == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("Erro: Empresário não encontrado com o email informado.", null));
        }
        String novaSenha = PasswordGenerator.generateRandomPassword();
        empresario.setSenha(novaSenha);
        repository.save(empresario);
        return ResponseEntity.ok(new ErrorResponse("Senha atualizada com sucesso. Nova senha: " + novaSenha, empresario));
    }
}