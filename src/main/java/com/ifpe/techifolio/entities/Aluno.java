package com.ifpe.techifolio.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "alunos")
public class Aluno extends Pessoa{
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String faculdade;

    @Override
    public String toString() {
        return "Aluno{" +
                "id=" + id +
                ", nome='" + getNome() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", faculdade='" + faculdade + '\'' +
                '}';
    }

    public Aluno(ObjectId id, String nome, String email, String senha, String faculdade) {
        super(nome, email, senha);
        this.id = id;
        this.faculdade = faculdade;
    }

    public String getNullFieldMessageAluno() {
        String returnText = "";
        if (this.getNome() == null || this.getNome().isEmpty()) {
            returnText += "Nome não pode ser nulo. ";
        }
        if (this.getEmail() == null || this.getEmail().isEmpty()) {
            returnText += "Email não pode ser nulo. ";
        }
        if (this.getSenha() == null || this.getSenha().isEmpty()) {
            returnText += "Senha não pode ser nula. ";
        }
        if (this.getFaculdade() == null || this.getFaculdade().isEmpty()) {
            returnText += "Faculdade não pode ser nula. ";
        }
        if (!returnText.isEmpty()) {
            return returnText;
        }
        return null;
    }
}
