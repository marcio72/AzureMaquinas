package br.com.locaweb.relatorioclientes.jogo.model;

import br.com.locaweb.relatorioclientes.jogo.DTO.*;
import java.util.*;
import java.util.stream.IntStream;

public class Partida {
    
    /* ===== IDENTIDADE ===== */
    private String id;
    private StatusPartida status;
    
    /* ===== CONFIGURAÇÃO ===== */
    private int maxJogadores;
    private int cartasPorJogador;
    private int totalRodadas;
    
    /* ===== CONTROLE ===== */
    private int rodadaAtual;
    
    /* ===== JOGO ===== */
    private Naipe naipeDaRodada;
    private List<CartaJogada> mesa;
    private List<Jogador> jogadores;
    
    /* ===== SORTEIO DE JOGADOR ===== */
    private int indiceJogadorInicial;
    private int indiceJogadorAtual;
    
    /* ===== CONSTRUTOR ===== */
    public Partida(String id, int maxJogadores, int cartasPorJogador) {
        this.id = id;
        this.maxJogadores = maxJogadores;
        this.cartasPorJogador = cartasPorJogador;
        this.totalRodadas = cartasPorJogador;
        this.rodadaAtual = 0;
        
        this.status = StatusPartida.AGUARDANDO_JOGADORES;
        this.mesa = new ArrayList<>();
        this.jogadores = new ArrayList<>();
        this.naipeDaRodada = null;
    }
    
    public void iniciarPartida() {
        // Validação básica (ajuste para 1 se estiver testando sozinho)
        if (jogadores.size() < 2 || jogadores.size() > maxJogadores) {
            throw new IllegalStateException("Quantidade inválida de jogadores");
        }
        
        distribuirCartas();
        
        this.indiceJogadorInicial = new Random().nextInt(jogadores.size());
        this.indiceJogadorAtual = indiceJogadorInicial;
        this.status = StatusPartida.EM_ANDAMENTO;
    }
    
    /* ===== GETTERS ===== */
    public String getId() { return id; }
    public StatusPartida getStatus() { return status; }
    public int getRodadaAtual() { return rodadaAtual; }
    public int getTotalRodadas() { return totalRodadas; }
    public Naipe getNaipeDaRodada() { return naipeDaRodada; }
    public List<CartaJogada> getMesa() { return mesa; }
    public List<Jogador> getJogadores() { return jogadores; }
    
    public Jogador getJogadorDaVez() {
        if (jogadores.isEmpty() || status != StatusPartida.EM_ANDAMENTO) {
            return null;
        }
        return jogadores.get(indiceJogadorAtual);
    }
    
    /* ===== MÉTODOS DE CONTROLE DA PARTIDA ===== */
    private void avancarJogador() {
        indiceJogadorAtual = (indiceJogadorAtual + 1) % jogadores.size();
    }
    
    /* ===== MÉTODOS DE CONTROLE DA PARTIDA ===== */
    
    public void jogarCarta(Jogador jogador, Carta carta) {
        // 1️⃣ Validações básicas
        if (status == StatusPartida.FINALIZADA) {
            throw new IllegalStateException("Partida já finalizada");
        }
        if (!jogador.equals(getJogadorDaVez())) {
            throw new IllegalStateException("Não é a vez desse jogador");
        }
        
        // 🔥 AQUI ESTÁ A MÁGICA: LIMPEZA TARDIA 🔥
        // Se a mesa ainda está cheia da rodada anterior, limpamos AGORA,
        // antes de colocar a nova carta da nova rodada.
        if (mesa.size() >= jogadores.size()) {
            limparMesa();
        }
        
        // Lógica normal de jogar...
        jogador.removerCarta(carta);
        
        boolean seguiuNaipe = naipeDaRodada == null || carta.getNaipe() == naipeDaRodada;
        
        CartaJogada jogada = new CartaJogada(
                jogador.getNome(),
                carta,
                rodadaAtual,
                seguiuNaipe,
                0
        );
        
        mesa.add(jogada);
        definirNaipeDaRodada(jogada);
        
        avancarJogador();
        
        // Verifica se fechou a rodada com essa carta
        if (rodadaFinalizada()) {
            finalizarRodada();
        }
    }
    
    private boolean rodadaFinalizada() {
        return mesa.size() == jogadores.size();
    }
    
    private void finalizarRodada() {
        pontuarRodada();        // Calcula quem ganhou e dá pontos
        
        // limparMesa();        <--- REMOVA OU COMENTE ESTA LINHA!
        // (Não limpamos mais aqui para que as cartas fiquem visíveis)
        
        rodadaAtual++;          // Avança rodada
        naipeDaRodada = null;   // Reseta naipe (importante!)
        
        if (rodadaAtual >= totalRodadas) {
            finalizarPartida();
        }
    }
    
    private void finalizarPartida() {
        status = StatusPartida.FINALIZADA;
    }
    
    private void definirNaipeDaRodada(CartaJogada jogada) {
        if (naipeDaRodada == null) {
            naipeDaRodada = jogada.getCarta().getNaipe();
        }
    }
    
    private List<CartaJogada> cartasQueSeguiramNaipe() {
        return mesa.stream()
                       .filter(CartaJogada::isSeguiuNaipe)
                       .toList();
    }
    
    private CartaJogada calcularVencedorDaRodada() {
        return cartasQueSeguiramNaipe()
                       .stream()
                       .max((a, b) -> {
                           // Cuidado: Certifique-se que ValorCarta existe no seu projeto
                           ValorCarta v1 = ValorCarta.fromSimbolo(a.getCarta().getValor());
                           ValorCarta v2 = ValorCarta.fromSimbolo(b.getCarta().getValor());
                           return Integer.compare(v1.getForca(), v2.getForca());
                       })
                       .orElseThrow(() -> new IllegalStateException("Nenhuma carta válida na rodada"));
    }
    
    private void pontuarRodada() {
        CartaJogada vencedora = calcularVencedorDaRodada();
        jogadores.stream()
                .filter(j -> j.getNome().equals(vencedora.getJogador()))
                .findFirst()
                .ifPresent(Jogador::adicionarPonto);
    }
    
    private void limparMesa() {
        mesa.clear();
    }
    
    public ResultadoJogadorDTO getVencedor() {
        if (status != StatusPartida.FINALIZADA) {
            throw new IllegalStateException("Partida ainda não finalizada");
        }
        return getRankingFinal().get(0);
    }
    
    public EstadoPartidaDTO toDTO() {
        String jogadorDaVez = getJogadorDaVez() != null ? getJogadorDaVez().getNome() : null;
        
        // Conversão de jogadores para DTO
        List<JogadorDTO> jogadoresDTO = jogadores.stream()
                                                .map(j -> new JogadorDTO(
                                                        j.getNome(),
                                                        j.getPontos(),
                                                        j.getCartas().stream()
                                                                .map(c -> new CartaDTO(c.getNaipe().name(), c.getValor()))
                                                                .toList()
                                                ))
                                                .toList();
        
        // Conversão da mesa para DTO
        List<CartaJogadaDTO> mesaDTO = mesa.stream()
                                               .map(j -> new CartaJogadaDTO(
                                                       j.getJogador(),
                                                       j.getCarta().getNaipe().name(),
                                                       j.getCarta().getValor()
                                               ))
                                               .toList();
        
        // Importante: Adicionei o naipeDaRodada aqui se o seu DTO suportar
        // Se o seu EstadoPartidaDTO não tiver esse campo no construtor, remova a linha abaixo
        String naipeAtual = (naipeDaRodada != null) ? naipeDaRodada.name() : null;
        
        return new EstadoPartidaDTO(
                id,
                rodadaAtual,
                totalRodadas,
                status.name(),
                jogadorDaVez,
                jogadoresDTO,
                mesaDTO,
                naipeAtual // <--- Verifique se seu DTO aceita este parâmetro extra
        );
    }
    
    public List<ResultadoJogadorDTO> getRankingFinal() {
        if (status != StatusPartida.FINALIZADA) {
            throw new IllegalStateException("Partida ainda não finalizada");
        }
        
        List<Jogador> ordenados = jogadores.stream()
                                          .sorted(Comparator.comparingInt(Jogador::getPontos).reversed())
                                          .toList();
        
        return IntStream.range(0, ordenados.size())
                       .mapToObj(i -> new ResultadoJogadorDTO(
                               ordenados.get(i).getNome(),
                               ordenados.get(i).getPontos(),
                               i + 1
                       ))
                       .toList();
    }
    
    public void adicionarJogador(Jogador jogador) {
        if (status != StatusPartida.AGUARDANDO_JOGADORES) {
            throw new IllegalStateException("Partida já iniciada");
        }
        if (jogadores.size() >= maxJogadores) {
            throw new IllegalStateException("Partida cheia");
        }
        jogadores.add(jogador);
    }
    
    public void removerJogador(String nomeJogador) {
        jogadores.removeIf(j -> j.getNome().equals(nomeJogador));
        if (status == StatusPartida.EM_ANDAMENTO && jogadores.size() < 2) {
            status = StatusPartida.FINALIZADA;
        }
    }
    
    private List<Carta> criarBaralho() {
        List<Carta> baralho = new ArrayList<>();
        for (Naipe naipe : Naipe.values()) {
            // Cartas padrão de Poker/Truco (ajuste conforme regra)
            String[] valores = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
            for(String valor : valores) {
                baralho.add(new Carta(naipe, valor));
            }
        }
        Collections.shuffle(baralho);
        return baralho;
    }
    
    private void distribuirCartas() {
        List<Carta> baralho = criarBaralho();
        for (Jogador jogador : jogadores) {
            jogador.getCartas().clear(); // Garante mão limpa
            for (int i = 0; i < cartasPorJogador; i++) {
                if(!baralho.isEmpty()) {
                    jogador.adicionarCarta(baralho.remove(0));
                }
            }
        }
    }
}