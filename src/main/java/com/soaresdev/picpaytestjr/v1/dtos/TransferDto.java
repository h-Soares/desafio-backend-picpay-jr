package com.soaresdev.picpaytestjr.v1.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class TransferDto {

    @NotBlank(message = "Payer email can not be null or empty")
    @Email(message = "Invalid payer email")
    private String payerEmail;

    @NotBlank(message = "Payee email can not be null or empty")
    @Email(message = "Invalid payee email")
    private String payeeEmail;

    @NotNull(message = "Balance can not be null")
    @Positive(message = "Balance must be greater than zero")
    private BigDecimal amount;

    public TransferDto() {
    }

    public TransferDto(String payerEmail, String payeeEmail, BigDecimal amount) {
        this.payerEmail = payerEmail;
        this.payeeEmail = payeeEmail;
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayeeEmail() {
        return payeeEmail;
    }

    public void setPayeeEmail(String payeeEmail) {
        this.payeeEmail = payeeEmail;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferDto{");
        sb.append("amount=").append(amount);
        sb.append(", payerEmail='").append(payerEmail).append('\'');
        sb.append(", payeeEmail='").append(payeeEmail).append('\'');
        sb.append('}');
        return sb.toString();
    }
}