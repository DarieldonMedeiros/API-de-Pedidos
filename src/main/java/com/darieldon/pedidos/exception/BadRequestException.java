package com.darieldon.pedidos.exception;

public class BadRequestException  extends RuntimeException{

    public BadRequestException(String message){
        super(message);
    }
}
