package br.com.locaweb.relatorioclientes.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrocarSenhaDTO {
    private String username;
    private String senhaAtual;
    private String senhaNova;
}
