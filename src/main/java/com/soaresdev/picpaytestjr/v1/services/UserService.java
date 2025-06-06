package com.soaresdev.picpaytestjr.v1.services;

import com.soaresdev.picpaytestjr.entities.User;
import com.soaresdev.picpaytestjr.entities.enums.UserType;
import com.soaresdev.picpaytestjr.exceptions.InvalidUserTypeException;
import com.soaresdev.picpaytestjr.repositories.UserRepository;
import com.soaresdev.picpaytestjr.utils.RegexUtils;
import com.soaresdev.picpaytestjr.v1.dtos.UserRequestDto;
import com.soaresdev.picpaytestjr.v1.dtos.UserResponseDto;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Caching(put = {@CachePut(value = "user-cache", key = "#result.email")},
            evict = {@CacheEvict(value = "users", allEntries = true)})
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        logger.info("Creating new user...");
        validateOnDatabase(userRequestDto);

        User user = convertRequestDtoToUser(userRequestDto);
        UserResponseDto userResponseDto = new UserResponseDto(userRepository.save(user));
        logger.info("User created successfully: {}", userResponseDto);
        return userResponseDto;
    }

    @Cacheable(
            value = "users",
            key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort.toString()",
            sync = true
    )
    public Page<UserResponseDto> findAll(Pageable pageable) {
        logger.info("Finding all users...");
        return userRepository.findAll(pageable).map(UserResponseDto::new);
    }

    @Cacheable(value = "user-cache", key = "#email")
    public UserResponseDto findUserByEmail(String email) {
        logger.info("Finding user by email: {}...", email);
        UserResponseDto user = userRepository.findByEmail(email).map(UserResponseDto::new).
                orElseThrow(() -> new EntityNotFoundException("User not found"));
        logger.info("User found: {}", user);
        return user;
    }

    private void validateOnDatabase(UserRequestDto userRequestDto) {
        logger.info("Validating user request on database...");
        if(userRepository.existsByCpfCnpj(userRequestDto.getCpfCnpj().replaceAll("[./-]", "")))
            throw new EntityExistsException("User CPF/CNPJ already exists");

        if(userRepository.existsByEmail(userRequestDto.getEmail()))
            throw new EntityExistsException("User email already exists");
        logger.info("User request successfully validated on database");
    }

    private User convertRequestDtoToUser(UserRequestDto userRequestDto) {
        UserType userType = determineUserType(userRequestDto.getCpfCnpj());
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        userRequestDto.setCpfCnpj(userRequestDto.getCpfCnpj().replaceAll("[./-]", ""));
        return new User(userRequestDto.getBalance(), userRequestDto.getCpfCnpj(),
                userRequestDto.getEmail(), userRequestDto.getFullName(),
                userRequestDto.getPassword(), userType.getCode());
    }

    private UserType determineUserType(String cpfCnpj) {
        logger.info("Determining user type for CPF/CNPJ: {}...", cpfCnpj);
        UserType userType;
        if(cpfCnpj.matches(RegexUtils.CPF_REGEX))
            userType = UserType.CUSTOMER;
        else if(cpfCnpj.matches(RegexUtils.CNPJ_REGEX))
            userType = UserType.SELLER;
        else
            throw new InvalidUserTypeException("Invalid CPF/CNPJ");
        logger.info("CPF/CNPJ successfully determined: {}", userType);
        return userType;
    }
}