package com.ifpe.techifolio.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pessoa {
    private String nome;
    private String email;
    private String senha;
}
