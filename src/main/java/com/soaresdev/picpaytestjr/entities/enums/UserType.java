package com.soaresdev.picpaytestjr.entities.enums;

import com.soaresdev.picpaytestjr.exceptions.InvalidUserTypeException;

public enum UserType {
    CUSTOMER(1),
    SELLER(2);

    private final Integer code;

    UserType(Integer code) {
        this.code = code;
    }

    public static UserType getFromCode(Integer code) {
        for(UserType userType : UserType.values()) {
            if(userType.getCode().equals(code))
                return userType;
        }
        throw new InvalidUserTypeException("Invalid user type code: " + code);
    }

    public Integer getCode() {
        return code;
    }
}