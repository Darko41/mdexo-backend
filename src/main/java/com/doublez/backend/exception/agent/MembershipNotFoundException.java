package com.doublez.backend.exception.agent;

public class MembershipNotFoundException extends RuntimeException {
    public MembershipNotFoundException(Long membershipId) {
        super("Agency membership not found with id: " + membershipId);
    }
}
