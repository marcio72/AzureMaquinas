package br.com.locaweb.relatorioclientes.DTO;

import java.util.ArrayList;
import java.util.List;

import br.com.locaweb.relatorioclientes.model.Cliente;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTOs do cadastro de máquina com peças (estoque ou avulsa).
 * Tudo num arquivo só para não espalhar classes pequenas.
 */
public class CadastroMaquinaDTO {

    private CadastroMaquinaDTO() { }

    /** Uma máquina sendo cadastrada. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Maquina {
        private String nomMaq;          // número escrito na máquina
        private String nomJogo;         // nome do jogo (texto) — a peça Jogo do catálogo sobrescreve
        private String obs;
        private String entrada;         // leitura do contador de entrada (só números)
        private String saida;           // leitura do contador de saída (só números)
        private List<Peca> pecas = new ArrayList<>();
    }

    /** Uma peça da máquina: do estoque (pecaId) ou avulsa (codigo + modeloId). */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Peca {
        private String slot;            // PLACA, FONTE, MONITOR, COLETOR, JOGO
        private boolean avulsa;         // true = fora do estoque (cria no lote AVULSO)
        private Long pecaId;            // peça do estoque
        private String codigo;          // avulsa: código/etiqueta da peça (vazio = gera automático)
        private Long modeloId;          // avulsa: Tbl_Placa / Tbl_coletor / Tbl_Jogos, conforme o slot
    }

    /** Cadastro de cliente novo com as máquinas dele. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ClienteComMaquinas {
        private Cliente cliente;
        private List<Maquina> maquinas = new ArrayList<>();
        private boolean confirmarDuplicado; // true = já viu o aviso de nº repetido na praça e quer seguir
    }

    /** Cadastro de UMA máquina para um cliente que já existe. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class MaquinaDeCliente {
        private Long codCliente;
        private Maquina maquina;
        private boolean confirmarDuplicado;
    }

    // ------------------------------------------------------------------ opções da tela

    /** O que a tela precisa para montar o bloco de uma peça. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class OpcaoSlot {
        private String slot;
        private String rotulo;          // "Placa", "Fonte"...
        private boolean obrigatoria;
        private Long categoriaId;       // null = categoria não encontrada no estoque
        private String categoriaNome;
        private List<PecaDisponivel> disponiveis = new ArrayList<>();
        private List<Modelo> modelos = new ArrayList<>();  // catálogo p/ avulsa (placa/coletor/jogo)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PecaDisponivel {
        private Long id;
        private String codigo;
        private String detalhe;         // modelo da placa, nome do coletor, jogo ou subcategoria

        public PecaDisponivel(Long id, String codigo, String detalhe) {
            this.id = id;
            this.codigo = codigo;
            this.detalhe = detalhe;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Modelo {
        private Long id;
        private String nome;

        public Modelo(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }
}
