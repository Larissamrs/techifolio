package com.ifpe.techifolio.controllerTest;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifpe.techifolio.controller.AlunoController;
import com.ifpe.techifolio.entities.Aluno;
import com.ifpe.techifolio.repository.AlunoRepository;

import org.springframework.http.MediaType;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AlunoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AlunoController alunoController;

    private ObjectId id;
    private Aluno aluno;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        id = new ObjectId();
        aluno = new Aluno(id, "João", "joao@mail.com", "123456", "ABC");
        mockMvc = MockMvcBuilders.standaloneSetup(alunoController)
            .setControllerAdvice() 
            .build();
    }

    @Test
    void testLoginSuccessTC001() throws Exception {
        when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/alunos/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(aluno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(aluno.getNome()))
                .andExpect(jsonPath("$.email").value(aluno.getEmail()));
    }

    @Test
    void testLoginNonExistentEmailTC002() throws Exception {
        when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(Optional.empty());

        mockMvc.perform(post("/alunos/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(aluno)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("E-mail não cadastrado"));
    }

    @Test
    void testLoginIncorrectPasswordTC003() throws Exception {
        when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/alunos/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(aluno)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Senha incorreta"));
    }

    @Test
    void testLoginMissingEmailTC004() throws Exception {
        Aluno invalidAluno = new Aluno(id, "João", null, "123456", "ABC");

        mockMvc.perform(post("/alunos/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Todos os campos devem ser preenchidos. Insira um e-mail para continuar."));
    }

    @Test
    void testLoginMissingPasswordTC005() throws Exception {
        Aluno invalidAluno = new Aluno(id, "João", "joao@mail.com", null, "ABC");

        mockMvc.perform(post("/alunos/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Todos os campos devem ser preenchidos. Insira uma senha para continuar."));
    }
}