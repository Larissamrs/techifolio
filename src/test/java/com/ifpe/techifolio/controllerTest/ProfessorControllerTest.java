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
import com.ifpe.techifolio.controller.ProfessorController;
import com.ifpe.techifolio.entities.Professor;
import com.ifpe.techifolio.repository.ProfessorRepository;

import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class ProfessorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private ProfessorController professorController;

    private ObjectId id;
    private Professor professor;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        id = new ObjectId();
        professor = new Professor(id, "João", "joao@mail.com", "123456", "ABC");
        mockMvc = MockMvcBuilders.standaloneSetup(professorController)
            .setControllerAdvice() 
            .build();
    }

    @Test
    void testCreateProfessorSucessTC013() throws Exception {
       //Define o comportamento do mock: quando o método save for chamado, retorne o objeto professor
        when(professorRepository.save(any(Professor.class))).thenReturn(professor);

        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(professor)))
                .andExpect(status().isCreated()) // Verifica se o status da resposta é 201 Created
                .andExpect(jsonPath("$.nome").value(professor.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.email").value(professor.getEmail()))
                .andExpect(jsonPath("$.senha").value(professor.getSenha()))
                .andExpect(jsonPath("$.faculdade").value(professor.getFaculdade())); 
    }

    @Test
    void testCreateProfessorErrorDuplicateEmailTC014() throws Exception {
        //Define o comportamento do mock: quando o método findByEmail for chamado, retorne o objeto professor
        when(professorRepository.findByEmail("joao@mail.com")).thenReturn(professor);

        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(professor)))
                .andExpect(status().isConflict()) // Verifica se o status da resposta é 409 Conflict
                .andExpect(jsonPath("$.message").value("Erro: Já existe um professor cadastrado com o email informado.")) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.nome").value(professor.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.email").value(professor.getEmail()))
                .andExpect(jsonPath("$.receivedObject.senha").value(professor.getSenha()))
                .andExpect(jsonPath("$.receivedObject.faculdade").value(professor.getFaculdade())); 
    }

    @Test
    void testCreateProfessorErrorNullFieldNomeTC015() throws Exception {
        Professor invalidProfessor = new Professor(id, null, "joao@mail.com","123456", "ABC");

       mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidProfessor)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Nome não pode ser nulo. ")) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.nome").isEmpty()); // Verifica o conteúdo da resposta
                
    }
    @Test
    void testCreateProfessorErrorNullFieldEmailTC016() throws Exception {
        Professor invalidProfessor = new Professor(id, "João", null,"123456", "ABC");
       
        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidProfessor)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Email não pode ser nulo. ")) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.email").isEmpty()); // Verifica o conteúdo da resposta 
    }
    @Test
    void testCreateProfessorErrorNullFieldPasswordTC017() throws Exception {
       Professor invalidProfessor = new Professor(id, "João", "joao@mail.com",null, "ABC");

        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidProfessor)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Senha não pode ser nula. ")) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.senha").isEmpty());
    }
    @Test
    void testCreateProfessorErrorNullFieldFaculdadeTC018() throws Exception {
       Professor invalidProfessor = new Professor(id, "João", "joao@mail.com","123456", null);

        mockMvc.perform(post("/professores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidProfessor)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Faculdade não pode ser nula. ")) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.faculdade").isEmpty());
    }
    // @Test
    // public void testGetProfessorById() throws Exception {
    //     // Define o comportamento do mock: quando o método findById for chamado, retorne um Optional contendo o professor
    //     when(professorRepository.findById(id)).thenReturn(Optional.of(professor));

    //     // Simula uma requisição GET para obter um professor pelo ID
    //     mockMvc.perform(get("/professores/{id}", id.toHexString()))
    //             .andExpect(status().isOk()) // Verifica se o status da resposta é 200 OK
    //             .andExpect(jsonPath("$.nome").value(professor.getNome())); // Verifica o conteúdo da resposta
    // }

    // @Test
    // public void testUpdateProfessor() throws Exception {
    //     // Define o comportamento do mock: quando o método findById for chamado, retorne um Optional contendo o professor
    //     when(professorRepository.findById(id)).thenReturn(Optional.of(professor));
    //     // Define o comportamento do mock: quando o método save for chamado, retorne o objeto professor
    //     when(professorRepository.save(any(Professor.class))).thenReturn(professor);

    //     // Simula uma requisição PUT para atualizar um professor
    //     mockMvc.perform(put("/professores/{id}", id.toHexString())
    //             .contentType(MediaType.APPLICATION_JSON) // Define o tipo de conteúdo da requisição
    //             .content(new ObjectMapper().writeValueAsString(professor))) // Define o corpo da requisição
    //             .andExpect(status().isOk()) // Verifica se o status da resposta é 200 OK
    //             .andExpect(content().string("Professor atualizado com sucesso")); // Verifica o conteúdo da resposta
    // }

    // @Test
    // public void testDeleteProfessor() throws Exception {
    //     // Define o comportamento do mock: quando o método findById for chamado, retorne um Optional contendo o professor
    //     when(professorRepository.findById(id)).thenReturn(Optional.of(professor));

    //     // Simula uma requisição DELETE para deletar um professor
    //     mockMvc.perform(delete("/professores/{id}", id.toHexString()))
    //             .andExpect(status().isNoContent()); // Verifica se o status da resposta é 204 No Content
    // }

    // @Test
    // public void testLogin() throws Exception {
    //     // Define o comportamento do mock: quando o método findByEmailAndSenha for chamado, retorne o objeto professor
    //     when(professorRepository.findByEmailAndSenha(professor.getEmail(), professor.getSenha())).thenReturn(professor);

    //     // Simula uma requisição POST para login
    //     mockMvc.perform(post("/professores/login")
    //             .param("email", professor.getEmail()) // Define o parâmetro de email
    //             .param("senha", professor.getSenha())) // Define o parâmetro de senha
    //             .andExpect(status().isOk()) // Verifica se o status da resposta é 200 OK
    //             .andExpect(jsonPath("$.nome").value(professor.getNome())); // Verifica o conteúdo da resposta
    // }
}
