package br.com.locaweb.relatorioclientes.DTO;

import br.com.locaweb.relatorioclientes.model.Placa;

public class PlacaResponseDTO {

    private Long id;
    private String modelo;
    private String fabricante;

    public static PlacaResponseDTO fromEntity(Placa placa) {
        PlacaResponseDTO dto = new PlacaResponseDTO();
        dto.id = placa.getId();
        dto.modelo = placa.getModelo();
        dto.fabricante = placa.getFabricante();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getModelo() {
        return modelo;
    }

    public String getFabricante() {
        return fabricante;
    }
}
