package com.soaresdev.picpaytestjr.entities.enums;

public enum UserTypes {
    CUSTOMERS(1),
    SELLERS(2);

    private final Integer code;

    UserTypes(Integer code) {
        this.code = code;
    }

    public static UserTypes getFromCode(Integer code) {
        for(UserTypes userType : UserTypes.values()) {
            if(userType.getCode().equals(code))
                return userType;
        }
        throw new IllegalArgumentException("Invalid user type code: " + code);
    }

    public Integer getCode() {
        return code;
    }
}