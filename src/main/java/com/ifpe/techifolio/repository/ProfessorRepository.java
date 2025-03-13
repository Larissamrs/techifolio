package com.ifpe.techifolio.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ifpe.techifolio.entities.Professor;



public interface ProfessorRepository extends MongoRepository<Professor, ObjectId> {
    @Query(value = "{ 'email': ?0 }")
    Professor findByEmail(String email);
    @Query(value = "{ 'email': ?0, 'senha': ?1 }")
    Professor findByEmailAndSenha(String email, String senha);
}
