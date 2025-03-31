package com.ifpe.techifolio.service;

import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;

import org.bson.types.ObjectId;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    // Testes inseridos por Larissa Maria

    @Test
    public void testUpdateAlunoAlterarNomeComSucesso() throws Exception {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setNome("Novo Nome");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));
        when(repository.save(any(Aluno.class))).thenReturn(existingAluno);

        Aluno updatedAluno = alunoService.updateAluno(id, alunoDetails);

        assertNotNull(updatedAluno);
        assertEquals("Novo Nome", updatedAluno.getNome());
    }

    @Test
    public void testUpdateAlunoSenhaIncorreta() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setSenha("senhaIncorreta");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Senha incorreta.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoNomeVazio() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setNome("");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Nome não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoSenhaVazia() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setSenha("");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Senha não pode ser nula ou vazia.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoEmailVazio() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setEmail("");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Email não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoNomeComCaracteresEspeciais() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setNome("Nome@123!");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Nome contém caracteres inválidos.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoEmailFormatoInvalido() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setEmail("emailinvalido");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Formato de email inválido.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoEmailEmBranco() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setEmail(""); 

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Email não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoEmailDuplicado() {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setEmail("duplicado@example.com");

        Aluno outroAluno = new Aluno();
        outroAluno.setId(new ObjectId());
        outroAluno.setEmail("duplicado@example.com");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));
        when(repository.findByEmail(alunoDetails.getEmail())).thenReturn(Optional.of(outroAluno));

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.updateAluno(id, alunoDetails);
        });

        assertEquals("Erro: Já existe um aluno cadastrado com o email informado.", exception.getMessage());
    }

    @Test
    public void testUpdateAlunoSalvarSemAlterar() throws Exception {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno(); 

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));
        when(repository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aluno updatedAluno = alunoService.updateAluno(id, alunoDetails);

        assertNotNull(updatedAluno);
        assertEquals(existingAluno.getNome(), updatedAluno.getNome());
        assertEquals(existingAluno.getEmail(), updatedAluno.getEmail());
        assertEquals(existingAluno.getFaculdade(), updatedAluno.getFaculdade());
        assertEquals(existingAluno.getSenha(), updatedAluno.getSenha());
    }

    @Test
    public void testUpdateAlunoAlterarEmail() throws Exception {
        ObjectId id = new ObjectId();
        Aluno alunoDetails = new Aluno();
        alunoDetails.setEmail("novoemail@example.com");

        when(repository.findById(id)).thenReturn(Optional.of(existingAluno));
        when(repository.findByEmail(alunoDetails.getEmail())).thenReturn(Optional.empty());
        when(repository.save(any(Aluno.class))).thenReturn(existingAluno);

        Aluno updatedAluno = alunoService.updateAluno(id, alunoDetails);

        assertNotNull(updatedAluno);
        assertEquals("novoemail@example.com", updatedAluno.getEmail());
    }

    @Test
    public void testRecuperarSenhaFormularioNaoPreenchido() {
        Aluno aluno = new Aluno();

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.recuperarSenha(aluno);
        });

        assertEquals("Erro: Email não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    public void testRecuperarSenhaEmailNaoCadastrado() {
        Aluno aluno = new Aluno();
        aluno.setEmail("naocadastrado@example.com");

        when(repository.findByEmail(aluno.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.recuperarSenha(aluno);
        });

        assertEquals("Erro: Aluno não encontrado com o email informado.", exception.getMessage());
    }

    @Test
    public void testRecuperarSenhaSucesso() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@example.com");

        when(repository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(existingAluno));
        when(passwordEncoder.encode(anyString())).thenReturn("novaSenhaCodificada");

        alunoService.recuperarSenha(aluno);

        verify(repository).save(existingAluno);
    }

    @Test
    public void testCreateAlunoSemFaculdade() {
        Aluno newAluno = new Aluno();
        newAluno.setNome("Test User");
        newAluno.setEmail("test@example.com");
        newAluno.setSenha("password");

        Exception exception = assertThrows(Exception.class, () -> {
            alunoService.createAluno(newAluno);
        });

        assertEquals("Erro: Faculdade não pode ser nula ou vazia.", exception.getMessage());
    }
}