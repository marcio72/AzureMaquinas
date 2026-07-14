package br.com.locaweb.relatorioclientes.DTO;

import br.com.locaweb.relatorioclientes.model.Jogo;

public class JogoResponseDTO {

    private Long id;
    private String nome;

    public static JogoResponseDTO fromEntity(Jogo jogo) {
        JogoResponseDTO dto = new JogoResponseDTO();
        dto.id = jogo.getId();
        dto.nome = jogo.getDescricaojogo();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
