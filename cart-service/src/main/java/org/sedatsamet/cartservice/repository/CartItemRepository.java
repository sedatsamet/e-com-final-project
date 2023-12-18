package org.sedatsamet.cartservice.repository;


import org.sedatsamet.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
