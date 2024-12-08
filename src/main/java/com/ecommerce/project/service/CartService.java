package com.ecommerce.project.service;


import com.ecommerce.project.payload.CartDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {


    CartDTO addProductToCart(Long productId, Integer quantity);


    List<CartDTO> getAllCarts();


    CartDTO getCart(String emailId, Long cartId);


    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, int quantity);


    @Query("delete  from  CartItem  ci where ci.cart=?1 and ci.product.id=?2")
    String deleteProductFromCart(Long cartId, Long productId);

}
