package org.sedatsamet.productservice.repository;

import org.sedatsamet.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
