package com.soaresdev.picpaytestjr.v1.dtos;

import com.soaresdev.picpaytestjr.entities.User;
import com.soaresdev.picpaytestjr.entities.enums.UserType;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class UserResponseDto {
    private UUID id;
    private String fullName;
    private UserType userType;
    private String cpfCnpj;
    private String email;
    private BigDecimal balance;

    public UserResponseDto() {
    }

    public UserResponseDto(BigDecimal balance, String cpfCnpj, String email, String fullName, UUID id, UserType userType) {
        this.balance = balance;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.fullName = fullName;
        this.id = id;
        this.userType = userType;
    }

    public UserResponseDto(User user) {
        this(user.getBalance(), user.getCpfCnpj(), user.getEmail(), user.getFullName(),
                user.getId(), UserType.getFromCode(user.getUserTypeCode()));
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDto that = (UserResponseDto) o;
        return Objects.equals(id, that.id) && Objects.equals(cpfCnpj, that.cpfCnpj) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpfCnpj, email);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserResponseDto{");
        sb.append("balance=").append(balance);
        sb.append(", id=").append(id);
        sb.append(", fullName='").append(fullName).append('\'');
        sb.append(", userType=").append(userType);
        sb.append(", cpfCnpj='").append(cpfCnpj).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append('}');
        return sb.toString();
    }
}