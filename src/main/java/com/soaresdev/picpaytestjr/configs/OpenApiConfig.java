package com.soaresdev.picpaytestjr.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Linkedin",
                        url = "https://www.linkedin.com/in/hiago-soares-96840a271"
                ),
                description = "Documentation for Junior software developer test at PicPay",
                title = "PicPay test for Junior software developer",
                version = "1.0"
        )
)
public class OpenApiConfig {
}