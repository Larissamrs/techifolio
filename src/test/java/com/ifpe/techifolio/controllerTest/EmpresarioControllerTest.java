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
import com.ifpe.techifolio.controller.EmpresarioController;
import com.ifpe.techifolio.entities.Empresario;
import com.ifpe.techifolio.repository.EmpresarioRepository;

import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class EmpresarioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EmpresarioRepository empresarioRepository;

    @InjectMocks
    private EmpresarioController empresarioController;

    private ObjectId id;
    private Empresario empresario;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        id = new ObjectId();
        empresario = new Empresario(id, "João", "joao@mail.com", "123456", "Empresa ABC");
        mockMvc = MockMvcBuilders.standaloneSetup(empresarioController)
            .setControllerAdvice() 
            .build();
    }

    @Test
    void testCreateEmpresarioSuccessTC013() throws Exception {
        when(empresarioRepository.save(any(Empresario.class))).thenReturn(empresario);

        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(empresario)))
                .andExpect(status().isCreated()) // Verifica se o status da resposta é 201 Created
                .andExpect(jsonPath("$.nome").value(empresario.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.email").value(empresario.getEmail()))
                .andExpect(jsonPath("$.senha").value(empresario.getSenha()))
                .andExpect(jsonPath("$.empresa").value(empresario.getEmpresa()));
    }

    @Test
    void testCreateEmpresarioErrorDuplicateEmailTC014() throws Exception {
        when(empresarioRepository.findByEmail(empresario.getEmail())).thenReturn(empresario);

        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(empresario)))
                .andExpect(status().isConflict()) // Verifica se o status da resposta é 409 Conflict
                .andExpect(jsonPath("$.message").value("Erro: Já existe um empresário cadastrado com o email informado.")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.nome").value(empresario.getNome())) // Verifica o conteúdo da resposta
                .andExpect(jsonPath("$.receivedObject.email").value(empresario.getEmail()))
                .andExpect(jsonPath("$.receivedObject.senha").value(empresario.getSenha()))
                .andExpect(jsonPath("$.receivedObject.empresa").value(empresario.getEmpresa()));
    }

    @Test
    void testCreateEmpresarioErrorNullFieldNomeTC015() throws Exception {
        Empresario invalidEmpresario = new Empresario(id, null, "joao@mail.com", "123456", "Empresa ABC");
        
        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidEmpresario)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Nome não pode ser nulo. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.nome").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateEmpresarioErrorNullFieldEmailTC016() throws Exception {
        Empresario invalidEmpresario = new Empresario(id, "João", null, "123456", "Empresa ABC");
        
        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidEmpresario)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Email não pode ser nulo. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.email").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateEmpresarioErrorNullFieldPasswordTC017() throws Exception {
        Empresario invalidEmpresario = new Empresario(id, "João", "joao@mail.com", null, "Empresa ABC");
        
        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidEmpresario)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Senha não pode ser nula. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.senha").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    @Test
    void testCreateEmpresarioErrorNullFieldEmpresaTC019() throws Exception {
        Empresario invalidEmpresario = new Empresario(id, "João", "joao@mail.com", "123456", null);
        
        mockMvc.perform(post("/empresarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidEmpresario)))
                .andExpect(status().isBadRequest()) // Verifica se o status da resposta é 400 Bad Request
                .andExpect(jsonPath("$.message").value("Erro: Empresa não pode ser nula. ")) // Verifica a mensagem de erro
                .andExpect(jsonPath("$.receivedObject.empresa").isEmpty()); // Verifica o campo nulo no objeto recebido
    }

    // @Test
    // public void testGetEmpresarioById() throws Exception {
    //     when(empresarioRepository.findById(id)).thenReturn(Optional.of(empresario));

    //     mockMvc.perform(get("/empresarios/{id}", id.toHexString()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(empresario.getNome()));
    // }

    // @Test
    // public void testUpdateEmpresarioSuccess() throws Exception {
    //     when(empresarioRepository.findById(id)).thenReturn(Optional.of(empresario));
    //     when(empresarioRepository.save(any(Empresario.class))).thenReturn(empresario);

    //     mockMvc.perform(put("/empresarios/{id}", id.toHexString())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(new ObjectMapper().writeValueAsString(empresario)))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(empresario.getNome()))
    //             .andExpect(jsonPath("$.email").value(empresario.getEmail()))
    //             .andExpect(jsonPath("$.senha").value(empresario.getSenha()))
    //             .andExpect(jsonPath("$.empresa").value(empresario.getEmpresa()));
    // }

    // @Test
    // public void testUpdateEmpresarioErrorNullFieldNome() throws Exception {
    //     Empresario invalidEmpresario = new Empresario(id, null, "joao@mail.com", "123456", "Empresa ABC");
    //     ErrorResponse errorResponse = new ErrorResponse("Erro: Nome não pode ser nulo", invalidEmpresario);

    //     when(empresarioRepository.findById(id)).thenReturn(Optional.of(empresario));

    //     mockMvc.perform(put("/empresarios/{id}", id.toHexString())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(new ObjectMapper().writeValueAsString(invalidEmpresario)))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.message").value("Erro: Nome não pode ser nulo"))
    //             .andExpect(jsonPath("$.receivedObject.nome").isEmpty());
    // }

    // @Test
    // public void testDeleteEmpresario() throws Exception {
    //     when(empresarioRepository.findById(id)).thenReturn(Optional.of(empresario));

    //     mockMvc.perform(delete("/empresarios/{id}", id.toHexString()))
    //             .andExpect(status().isNoContent());
    // }

    // @Test
    // public void testLogin() throws Exception {
    //     when(empresarioRepository.findByEmailAndSenha(empresario.getEmail(), empresario.getSenha())).thenReturn(empresario);

    //     mockMvc.perform(post("/empresarios/login")
    //             .param("email", empresario.getEmail())
    //             .param("senha", empresario.getSenha()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.nome").value(empresario.getNome()));
    // }

    // @Test
    // public void testRecuperarSenha() throws Exception {
    //     when(empresarioRepository.findByEmail(empresario.getEmail())).thenReturn(empresario);

    //     mockMvc.perform(post("/empresarios/recuperar-senha")
    //             .param("email", empresario.getEmail()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.message").value("Senha atualizada com sucesso. Nova senha: " + empresario.getSenha()));
    // }
}