package com.soaresdev.picpaytestjr.v1.controllers;

import com.soaresdev.picpaytestjr.exceptions.StandardError;
import com.soaresdev.picpaytestjr.exceptions.StandardRequestError;
import com.soaresdev.picpaytestjr.v1.dtos.UserRequestDto;
import com.soaresdev.picpaytestjr.v1.dtos.UserResponseDto;
import com.soaresdev.picpaytestjr.v1.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/v1/user")
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(description = "Create a new user", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid body arguments", content = @Content(schema = @Schema(implementation = StandardRequestError.class))),
            @ApiResponse(responseCode = "409", description = "Entity already exists", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto userDTO = userService.createUser(userRequestDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(userDTO.getEmail()).toUri();
        return ResponseEntity.created(uri).body(userDTO);
    }

    @Operation(description = "Get a paginated list of all users", method = "GET")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<UserResponseDto>> findAllUsers(@ParameterObject @PageableDefault(sort = {"fullName", "balance"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @Operation(description = "Get an user by email", method = "GET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Illegal argument", content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    @GetMapping(value = "/{email}", produces = "application/json")
    public ResponseEntity<UserResponseDto> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }
}