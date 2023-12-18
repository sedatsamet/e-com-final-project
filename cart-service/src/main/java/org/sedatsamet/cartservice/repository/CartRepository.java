package org.sedatsamet.cartservice.repository;

import org.sedatsamet.cartservice.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}
