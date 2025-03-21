package com.ifpe.techifolio.service;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AlunoServiceTest {

    @Mock
    private AlunoRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AlunoService alunoService;

    private Aluno aluno;
    private Aluno existingAluno;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getSession(true)).thenReturn(session);

        aluno = new Aluno();
        aluno.setEmail("test@example.com");
        aluno.setSenha("password");

        existingAluno = new Aluno();
        existingAluno.setEmail("test@example.com");
        existingAluno.setSenha("encodedPassword");
        existingAluno.setNome("Test User");
        existingAluno.setFaculdade("Test University");
    }

    @Test
    public void testLoginSuccess() throws Exception {
        when(repository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(existingAluno));
        when(passwordEncoder.matches(aluno.getSenha(), existingAluno.getSenha())).thenReturn(true);

        Map<String, Object> response = alunoService.login(aluno, request);

        assertNotNull(response);
        assertEquals("Test User", response.get("nome"));
        assertEquals("test@example.com", response.get("email"));
    }

    @Test
    public void testLoginIncorrectPassword() {
        when(repository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(existingAluno));
        when(passwordEncoder.matches(aluno.getSenha(), existingAluno.getSenha())).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.login(aluno, request);
        });

        assertEquals("Senha incorreta", exception.getMessage());
    }

    @Test
    public void testLoginEmailNotFound() {
        when(repository.findByEmail(aluno.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.login(aluno, request);
        });

        assertEquals("E-mail não cadastrado", exception.getMessage());
    }

    @Test
    public void testLoginMissingEmail() {
        aluno.setEmail(null);

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.login(aluno, request);
        });

        assertEquals("E-mail não informado", exception.getMessage());
    }

    @Test
    public void testLoginMissingPassword() {
        aluno.setSenha(null);

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.login(aluno, request);
        });

        assertEquals("Senha não informada", exception.getMessage());
    }

    @Test
    public void testCreateAlunoSuccess() throws Exception {
        Aluno newAluno = new Aluno();
        newAluno.setNome("Test User");
        newAluno.setFaculdade("Test University");
        newAluno.setEmail("test@example.com");
        newAluno.setSenha("password");

        when(repository.findByEmail(newAluno.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(newAluno.getSenha())).thenReturn("encodedPassword");
        when(repository.save(any(Aluno.class))).thenReturn(newAluno);

        Aluno createdAluno = alunoService.createAluno(newAluno);

        assertNotNull(createdAluno);
        assertEquals("Test User", createdAluno.getNome());
        assertEquals("Test University", createdAluno.getFaculdade());
        assertEquals("test@example.com", createdAluno.getEmail());
        assertEquals("encodedPassword", createdAluno.getSenha());
    }

    @Test
    public void testCreateAlunoEmailAlreadyExists() {
        Aluno newAluno = new Aluno();
        newAluno.setNome("Test User");
        newAluno.setFaculdade("Test University");
        newAluno.setEmail("test@example.com");
        newAluno.setSenha("password");

        when(repository.findByEmail(newAluno.getEmail())).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.createAluno(newAluno);
        });

        assertEquals("Erro: Já existe um aluno cadastrado com o email informado.", exception.getMessage());
    }

    @Test
    public void testCreateAlunoMissingNome() {
        Aluno newAluno = new Aluno();
        newAluno.setFaculdade("Test University");
        newAluno.setEmail("test@example.com");
        newAluno.setSenha("password");

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.createAluno(newAluno);
        });

        assertEquals("Erro: Nome não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testCreateAlunoMissingEmail() {
        Aluno newAluno = new Aluno();
        newAluno.setNome("Test User");
        newAluno.setFaculdade("Test University");
        newAluno.setSenha("password");

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.createAluno(newAluno);
        });

        assertEquals("Erro: Email não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testCreateAlunoMissingSenha() {
        Aluno newAluno = new Aluno();
        newAluno.setNome("Test User");
        newAluno.setFaculdade("Test University");
        newAluno.setEmail("test@example.com");

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.createAluno(newAluno);
        });

        assertEquals("Erro: Senha não pode ser nula ou vazia.", exception.getMessage());
    }
}