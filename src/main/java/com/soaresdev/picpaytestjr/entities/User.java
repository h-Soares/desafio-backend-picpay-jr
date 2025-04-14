package com.soaresdev.picpaytestjr.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_user")
public class User {
    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "user_type_code", nullable = false)
    private Integer userTypeCode;

    @Column(name = "cpf_cnpj", unique = true, nullable = false, length = 14)
    private String cpfCnpj;

    @Column(unique = true, nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 70)
    private String password;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    public User() {
    }

    @PrePersist
    public void generateUuidv7() {
        if(Objects.isNull(id))
            id = UuidCreator.getTimeOrderedEpoch();
    }

    public User(BigDecimal balance, String cpfCnpj, String email, String fullName, String password, Integer userTypeCode) {
        this.balance = balance;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.userTypeCode = userTypeCode;
    }

    public User(Integer userTypeCode, String password, String fullName, String email, String cpfCnpj, BigDecimal balance) {
        this.userTypeCode = userTypeCode;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.balance = balance;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserTypeCode() {
        return userTypeCode;
    }

    public void setUserTypeCode(Integer userTypeCode) {
        this.userTypeCode = userTypeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(fullName, user.fullName) && Objects.equals(cpfCnpj, user.cpfCnpj) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, cpfCnpj, email);
    }
}