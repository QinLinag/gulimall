package com.atguigu.gulimall.cart.service;


import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {

    //向购物车中添加购物项，
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;


    //获取购物车中某个购物项
    CartItem getCartItem(Long skuId);

    //获取整个购物车，
    Cart getCart() throws ExecutionException, InterruptedException;

    //清空购物车数据
    void clearCart(String cartKey);

    //勾选购物车数据
    void checkItem(Long skuId, Integer check);


    //修改购物项数量，
    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();

}
