package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.chave.model.UsoChave;

/**
 * POST /api/chaves/maquinas/{maquinaId}
 * { "chaveId": 7, "uso": "COFRE", "observacao": "trocada fechadura" }
 * uso é opcional: se vier nulo, segue o tipo da chave (C → COFRE, T → TAMPA, D → CADEADO).
 */
public record VinculoChaveRequest(Long chaveId, UsoChave uso, String observacao) {
}
