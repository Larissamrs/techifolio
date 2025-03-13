package com.ifpe.techifolio.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ifpe.techifolio.entities.Aluno;



public interface AlunoRepository extends MongoRepository<Aluno, ObjectId> {
    @Query(value = "{ 'email': ?0 }")
    Optional<Aluno> findByEmail(String email);
    @Query(value = "{ 'email': ?0, 'senha': ?1 }")
    Aluno findByEmailAndSenha(String email, String senha);
}
