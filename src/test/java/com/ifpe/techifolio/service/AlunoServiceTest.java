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
}