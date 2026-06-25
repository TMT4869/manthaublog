package com.manthau.userservice.shared.exception;

public class DisplayNameAlreadyExistsException extends RuntimeException {
    public DisplayNameAlreadyExistsException(String displayName, String nameTag) {
        super("Display name already exists: " + displayName + "#" + nameTag);
    }
}
