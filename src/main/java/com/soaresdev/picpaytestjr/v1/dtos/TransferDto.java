package com.soaresdev.picpaytestjr.v1.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferDto(@NotBlank(message = "Payer email can not be null or empty")
@Email(message = "Invalid payer email") String payerEmail,
                          @NotBlank(message = "Payee email can not be null or empty")
@Email(message = "Invalid payee email") String payeeEmail,
                          @NotNull(message = "Balance can not be null")
@Positive(message = "Balance must be greater than zero") BigDecimal amount) {

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