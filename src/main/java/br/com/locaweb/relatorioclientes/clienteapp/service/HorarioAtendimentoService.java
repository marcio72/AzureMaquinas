package br.com.locaweb.relatorioclientes.clienteapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Regra de horário de atendimento do app do cliente (padrão: 8h às 22h,
 * horário de Brasília — independente do fuso UTC usado para persistir datas
 * no banco).
 *
 * Fica DESLIGADA por padrão (clienteapp.horario.validacao.ativa=false), pra
 * dar pra testar o app em qualquer horário. Pra ativar em produção, é só
 * virar essa flag pra true no application.properties — não precisa mexer
 * em código.
 */
@Service
public class HorarioAtendimentoService {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    @Value("${clienteapp.horario.validacao.ativa:false}")
    private boolean validacaoAtiva;

    @Value("${clienteapp.horario.inicio:8}")
    private int horaInicio;

    @Value("${clienteapp.horario.fim:22}")
    private int horaFim;

    /** true = pode abrir chamado agora (ou a validação está desligada). */
    public boolean dentroDoHorario() {
        if (!validacaoAtiva) {
            return true;
        }
        int horaAtual = ZonedDateTime.now(FUSO_BRASIL).getHour();
        return horaAtual >= horaInicio && horaAtual < horaFim;
    }

    public String mensagemForaDoHorario() {
        return String.format(
                "Atendimento disponível das %02d:00 às %02d:00. Tente novamente dentro desse horário.",
                horaInicio, horaFim
        );
    }
}
