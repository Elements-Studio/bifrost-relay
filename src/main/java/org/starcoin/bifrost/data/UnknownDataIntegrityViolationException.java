package org.starcoin.bifrost.data;


public class UnknownDataIntegrityViolationException extends Exception{

    public UnknownDataIntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }

}
