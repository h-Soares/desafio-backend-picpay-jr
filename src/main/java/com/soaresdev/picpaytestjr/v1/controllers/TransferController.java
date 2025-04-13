package com.soaresdev.picpaytestjr.v1.controllers;

import com.soaresdev.picpaytestjr.exceptions.StandardError;
import com.soaresdev.picpaytestjr.exceptions.StandardRequestError;
import com.soaresdev.picpaytestjr.v1.dtos.TransferDto;
import com.soaresdev.picpaytestjr.v1.services.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transfer")
@Tag(name = "Transfer")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(description = "Do a new transfer", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success. No content", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid body arguments", content = @Content(schema = @Schema(implementation = StandardRequestError.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "422", description = "Something wrong occurred during the transfer", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferDto transferDto) {
        transferService.transfer(transferDto);
        return ResponseEntity.noContent().build();
    }
}