package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
    private Long id;
    private String cuitCuil;
    private String denominacion;
    private String email;
    private String telefono;
}
