package com.backend.ms_security.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionValidationRequestDTO {

    @NotBlank(message = "La URL es obligatoria.")
    @Size(max = 255, message = "La URL no puede superar 255 caracteres.")
    private String url;

    @NotBlank(message = "El método HTTP es obligatorio.")
    @Pattern(
            regexp = "GET|POST|PUT|PATCH|DELETE",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El método HTTP no es válido."
    )
    private String method;
}
