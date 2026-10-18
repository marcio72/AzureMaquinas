package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tela "Editar Execuções" (/execucoes/gerenciar).
 *
 * Um único DTO para os dois sentidos:
 *  - resposta da busca por NF (todos os campos preenchidos, cliente/técnico/data só leitura);
 *  - corpo do PUT de salvar (só descricaoExecucao, descricaoProblema e maquinaId são lidos).
 *
 * NF = execucao_manutencao.id (mesmo critério do filtro "nf" do relatório).
 */
@Getter
@Setter
@NoArgsConstructor
public class ExecucaoCrudDTO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Long nf;

    // --- só leitura ---
    private Long solicitacaoId;
    private Long codCliente;
    private String nomeCliente;
    private String tecnico;
    private String dataExecucao;   // já formatado dd/MM/yyyy HH:mm:ss
    private Double valor;
    private boolean emailEnviado;
    private boolean pdfGerado;
    private long qtdPecasVinculadas;   // MovimentoEstoque ligados à execução
    private long qtdFichasIrmas;       // outras execuções da mesma solicitação

    // --- editáveis ---
    private Long maquinaId;
    private String descricaoProblema;
    private String descricaoExecucao;

    // --- apoio para o select de máquina ---
    private String nomeMaquina;
    private List<MaquinaOpcao> maquinasDoCliente = new ArrayList<>();

    public static String formatarData(LocalDateTime data) {
        return data == null ? null : data.format(FMT);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MaquinaOpcao {
        private Long id;
        private String nome;
        private String jogo;
        private boolean ativa;

        public MaquinaOpcao(Long id, String nome, String jogo, boolean ativa) {
            this.id = id;
            this.nome = nome;
            this.jogo = jogo;
            this.ativa = ativa;
        }
    }
}
