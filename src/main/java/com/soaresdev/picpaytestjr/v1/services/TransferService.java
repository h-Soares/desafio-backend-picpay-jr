package com.soaresdev.picpaytestjr.v1.services;

import com.soaresdev.picpaytestjr.entities.Transfer;
import com.soaresdev.picpaytestjr.entities.User;
import com.soaresdev.picpaytestjr.entities.enums.UserType;
import com.soaresdev.picpaytestjr.exceptions.TransferException;
import com.soaresdev.picpaytestjr.repositories.TransferRepository;
import com.soaresdev.picpaytestjr.repositories.UserRepository;
import com.soaresdev.picpaytestjr.v1.dtos.TransferDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class TransferService {
    private final Logger logger = LoggerFactory.getLogger(TransferService.class.getName());
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public TransferService(UserRepository userRepository, TransferRepository transferRepository, AuthorizationService authorizationService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void transfer(TransferDto transferDto) {
        if(authorizationService.isAuthorized())
            doTransfer(transferDto);
    }

    private void doTransfer(TransferDto transferDto) {
        logger.info("Transfer started: {}...", transferDto);
        User payer = getUserEntityByEmail(transferDto.payerEmail());
        User payee = getUserEntityByEmail(transferDto.payeeEmail());

        validateTransfer(payer, payee, transferDto.amount());

        payer.setBalance(payer.getBalance().subtract(transferDto.amount()));
        payee.setBalance(payee.getBalance().add(transferDto.amount()));
        userRepository.saveAll(List.of(payer, payee));

        transferRepository.save(new Transfer(payee, payer, Instant.now(), transferDto.amount()));

        notificationService.notifyUser();
        logger.info("Transfer finished: {}", transferDto);
    }

    private User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email).
                orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void validateTransfer(User payer, User payee, BigDecimal amount) {
        logger.info("Validating transfer...");
        if(payer.getUserTypeCode().equals(UserType.SELLER.getCode()))
            throw new TransferException("You are not allowed to transfer as a seller");

        if(payer.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0)
            throw new TransferException("You do not have enough money to transfer");

        if(payer.getEmail().equals(payee.getEmail()))
            throw new TransferException("You are not allowed to transfer to yourself");
        logger.info("Transfer validated successfully...");
    }
}