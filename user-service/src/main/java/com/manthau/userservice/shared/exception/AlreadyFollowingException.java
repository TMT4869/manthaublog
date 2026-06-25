package com.manthau.userservice.shared.exception;

public class AlreadyFollowingException extends RuntimeException {
    public AlreadyFollowingException(String username) {
        super("Already following: " + username);
    }
}