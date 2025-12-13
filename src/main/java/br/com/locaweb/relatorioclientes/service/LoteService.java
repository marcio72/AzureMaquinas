package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;

    public LoteService(LoteRepository loteRepository, PecaRepository pecaRepository) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public Lote salvarLoteComPecas(Lote lote) {

        // 1 — Salvar o lote primeiro
        Lote loteSalvo = loteRepository.save(lote);

        // 2 — Buscar o último código já usado pela CATEGORIA
        String ultimoCodigo = pecaRepository.findUltimoCodigoByCategoria(
                lote.getCategoria().getId());

        // 3 — Calcular o próximo número global da categoria
        int proximoNumero = 1;

        if (ultimoCodigo != null && ultimoCodigo.contains("-")) {
            String numeroStr = ultimoCodigo.split("\\-")[1];
            proximoNumero = Integer.parseInt(numeroStr) + 1;
        }

        // 4 — Gerar as peças do lote
        for (int i = 0; i < lote.getQuantidadeComprada(); i++) {

            // O alias usado é aquele digitado no lote
            String codigoGerado = lote.getAlias()
                    + "-"
                    + String.format("%04d", proximoNumero);

            Peca peca = new Peca();
            peca.setCodigo(codigoGerado);
            peca.setLote(loteSalvo);
            peca.setCategoria(loteSalvo.getCategoria());
            peca.setStatus("ESTOQUE"); // AQUI MARCIÃO!!!
            pecaRepository.save(peca);

            proximoNumero++;
        }

        // 5 — Atualizar quantidade atual
        loteSalvo.setQuantidadeAtual(lote.getQuantidadeComprada());

        return loteRepository.save(loteSalvo);
    }
}