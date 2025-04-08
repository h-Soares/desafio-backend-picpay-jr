package com.soaresdev.picpaytestjr.v1.dtos;

import com.soaresdev.picpaytestjr.utils.RegexUtils;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

public class UserRequestDto {
    private static final String NAME_REGEX = "^(?!.*[#@!0-9])[A-Za-zÀ-ÖØ-öø-ÿ]+( [A-Za-zÀ-ÖØ-öø-ÿ]+){0,10}$";
    private static final String PASSWORD_REGEX = "/^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()\\-_=+{}\\[\\]|\\\\:;\"'<>,.?\\/`~]{6,}$/";

    @NotNull(message = "Name can not be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = NAME_REGEX, message = "Invalid name")
    private String fullName;

    @Pattern(regexp = RegexUtils.CPF_REGEX + "|" + RegexUtils.CNPJ_REGEX, message = "Invalid CPF/CNPJ")
    private String cpfCnpj;

    @NotBlank(message = "Email can not be null or empty")
    @Size(max = 320, message = "Email must be at most 320 characters long")
    @Email(message = "Invalid email")
    private String email;

    @NotNull(message = "Password can not be null")
    @Size(min = 6, max = 70, message = "Password must be between 6 and 70 characters")
    @Pattern(regexp = PASSWORD_REGEX, message =
            "Password must contain at least 6 characters, with at least one number")
    private String password;

    @NotNull(message = "Balance can not be null")
    @Positive(message = "Balance must be greater than zero")
    private BigDecimal balance;

    public UserRequestDto() {
    }

    public UserRequestDto(BigDecimal balance, String cpfCnpj, String email, String fullName, String password) {
        this.balance = balance;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRequestDto userRequestDto = (UserRequestDto) o;
        return Objects.equals(cpfCnpj, userRequestDto.cpfCnpj) && Objects.equals(email, userRequestDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpfCnpj, email);
    }
}