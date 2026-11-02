package com.tpgrupal.desarrollo_de_software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionesComboDTO {
    private List<ItemComboDTO> puntosVenta;
    private List<ItemComboDTO> condicionesIva;
    private List<ItemComboDTO> clientes;
    private List<ArticuloComboDTO> articulos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemComboDTO {
        private Long id;
        private String descripcion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticuloComboDTO {
        private Long id;
        private Long listaPrecioArticuloId;
        private String codigo;
        private String denominacion;
        private double precioVenta;
    }
}
