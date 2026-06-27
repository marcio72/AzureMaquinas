package br.com.locaweb.relatorioclientes.service;

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

    /**
     * Salva o lote e gera as peças com numeração automática a partir do ponto de início.
     *
     * @param lote          entidade Lote preenchida
     * @param numeroInicial número a partir do qual os códigos serão gerados.
     *                      Ex.: alias="pl", numeroInicial=45, qtd=5
     *                      → pl-0045, pl-0046, pl-0047, pl-0048, pl-0049
     */
    @Transactional
    public Lote salvarLoteComPecas(Lote lote, int numeroInicial) {

        // 1 — Salvar o lote primeiro (sem as peças)
        Lote loteSalvo = loteRepository.save(lote);

        // 2 — Gerar as peças a partir do número informado
        int numero = numeroInicial;
        for (int i = 0; i < lote.getQuantidadeComprada(); i++) {

            String codigoGerado = lote.getAlias()
                    + "-"
                    + String.format("%04d", numero);

            Peca peca = new Peca();
            peca.setCodigo(codigoGerado);
            peca.setLote(loteSalvo);
            peca.setCategoria(loteSalvo.getCategoria());
            peca.setStatus("ESTOQUE");
            pecaRepository.save(peca);

            numero++;
        }

        // 3 — Atualizar quantidade atual
        loteSalvo.setQuantidadeAtual(lote.getQuantidadeComprada());

        return loteRepository.save(loteSalvo);
    }

    /**
     * Sobrecarga para compatibilidade com código existente (Thymeleaf).
     * Usa o último código da categoria como ponto de partida (comportamento antigo).
     */
    @Transactional
    public Lote salvarLoteComPecas(Lote lote) {

        // Busca o último código da categoria para continuar a sequência
        String ultimoCodigo = pecaRepository.findUltimoCodigoByCategoria(
                lote.getCategoria().getId());

        int proximoNumero = 1;
        if (ultimoCodigo != null && ultimoCodigo.contains("-")) {
            String numeroStr = ultimoCodigo.split("\\-")[1];
            proximoNumero = Integer.parseInt(numeroStr) + 1;
        }

        return salvarLoteComPecas(lote, proximoNumero);
    }
}
