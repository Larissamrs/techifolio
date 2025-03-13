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
@Document(collection = "empresarios")
public class Empresario extends Pessoa{
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String empresa;

    public boolean nullFieldEmpresario() {
        return this.getNome() == null || this.getEmail() == null || this.getSenha() == null || this.getEmpresa() == null;
    }
    
    public Empresario(ObjectId id, String nome, String email, String senha, String empresa) {
        super(nome, email, senha);
        this.id = id;
        this.empresa = empresa;
    }

    public String getNullFieldMessageEmpresario() {
        String returnText = "";
        if (this.getNome() == null) {
            returnText += "Nome não pode ser nulo. ";
        }
        if (this.getEmail() == null) {
            returnText += "Email não pode ser nulo. ";
        }
        if (this.getSenha() == null) {
            returnText += "Senha não pode ser nula. ";
        }
        if (this.getEmpresa() == null) {
            returnText += "Empresa não pode ser nula. ";
        }
        if (!returnText.isEmpty()) {
            return returnText;
        }
        return null;
    }
}
