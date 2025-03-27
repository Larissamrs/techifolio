package com.ifpe.techifolio.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ifpe.techifolio.entities.Empresario;



public interface EmpresarioRepository extends MongoRepository<Empresario, ObjectId> {
    @Query(value = "{ 'email': ?0 }")
    Optional<Empresario> findByEmail(String email);
    @Query(value = "{ 'email': ?0, 'senha': ?1 }")
    Empresario findByEmailAndSenha(String email, String senha);
}
