package com.servicelink.repository;

import com.servicelink.model.ServiceCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ServiceCategoryRepository extends MongoRepository<ServiceCategory, Long> {
    Optional<ServiceCategory> findByName(String name);
}