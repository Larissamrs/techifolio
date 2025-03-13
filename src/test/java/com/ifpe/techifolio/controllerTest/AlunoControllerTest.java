package com.ifpe.techifolio.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class AlunoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AlunoRepository alunoRepository;
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
    void testCreateAlunoSuccessTC013() throws Exception {
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(aluno)))
                .andExpect(status().isCreated()) // Verifica se o status da resposta é 201 Created
                .andExpect(jsonPath("$.nome").value(aluno.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.email").value(aluno.getEmail()))
                .andExpect(jsonPath("$.senha").value(aluno.getSenha()))
                .andExpect(jsonPath("$.faculdade").value(aluno.getFaculdade()));
    }

    @Test
    void testCreateAlunoErrorDuplicateEmailTC014() throws Exception {
        when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(Optional.of(aluno));

        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(aluno)))
                .andExpect(status().isConflict()) // Verifica se o status da resposta é 409 Conflict
                .andExpect(jsonPath("$.message").value("Erro: Já existe um aluno cadastrado com o email informado.")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.nome").value(aluno.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.email").value(aluno.getEmail()))
                .andExpect(jsonPath("$.receivedObject.senha").value(aluno.getSenha()))
                .andExpect(jsonPath("$.receivedObject.faculdade").value(aluno.getFaculdade()));
    }

    @Test
    void testCreateAlunoErrorNullFieldNomeTC015() throws Exception {
        Aluno invalidAluno = new Aluno(id, null, "joao@mail.com", "123456", "ABC");
        
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Nome não pode ser nulo. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.nome").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateAlunoErrorNullFieldEmailTC016() throws Exception {
        Aluno invalidAluno = new Aluno(id, "João", null, "123456", "ABC");
        
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Email não pode ser nulo. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.email").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateAlunoErrorNullFieldPasswordTC017() throws Exception {
        Aluno invalidAluno = new Aluno(id, "João", "joao@mail.com", null, "ABC");
        
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Senha não pode ser nula. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.senha").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateAlunoErrorNullFieldFaculdadeTC018() throws Exception {
        Aluno invalidAluno = new Aluno(id, "João", "joao@mail.com", "123456", null);
        
        mockMvc.perform(post("/alunos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidAluno)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Faculdade não pode ser nula. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.faculdade").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    // @Test
    // public void testGetAlunoById() throws Exception {
    //     when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));

    //     mockMvc.perform(get("/alunos/{id}", id.toHexString()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(aluno.getNome()));
    // }

    // @Test
    // public void testUpdateAlunoSuccess() throws Exception {
    //     when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));
    //     when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

    //     mockMvc.perform(put("/alunos/{id}", id.toHexString())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(new ObjectMapper().writeValueAsString(aluno)))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(aluno.getNome()))
    //             .andExpect(jsonPath("$.email").value(aluno.getEmail()))
    //             .andExpect(jsonPath("$.senha").value(aluno.getSenha()))
    //             .andExpect(jsonPath("$.faculdade").value(aluno.getFaculdade()));
    // }

    // @Test
    // public void testUpdateAlunoErrorNullFieldNome() throws Exception {
    //     Aluno invalidAluno = new Aluno(id, null, "joao@mail.com", "123456", "ABC");
    //     ErrorResponse errorResponse = new ErrorResponse("Erro: Nome não pode ser nulo", invalidAluno);

    //     when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));

    //     mockMvc.perform(put("/alunos/{id}", id.toHexString())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(new ObjectMapper().writeValueAsString(invalidAluno)))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.message").value("Erro: Nome não pode ser nulo"))
    //             .andExpect(jsonPath("$.receivedObject.nome").isEmpty());
    // }

    // @Test
    // public void testDeleteAluno() throws Exception {
    //     when(alunoRepository.findById(id)).thenReturn(Optional.of(aluno));

    //     mockMvc.perform(delete("/alunos/{id}", id.toHexString()))
    //             .andExpect(status().isNoContent());
    // }

    // @Test
    // public void testLogin() throws Exception {
    //     when(alunoRepository.findByEmailAndSenha(aluno.getEmail(), aluno.getSenha())).thenReturn(aluno);

    //     mockMvc.perform(post("/alunos/login")
    //             .param("email", aluno.getEmail())
    //             .param("senha", aluno.getSenha()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(aluno.getNome()));
    // }

    // @Test
    // public void testRecuperarSenha() throws Exception {
    //     when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(aluno);

    //     mockMvc.perform(post("/alunos/recuperar-senha")
    //             .param("email", aluno.getEmail()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.message").value("Senha atualizada com sucesso. Nova senha: " + aluno.getSenha()));
    // }
}