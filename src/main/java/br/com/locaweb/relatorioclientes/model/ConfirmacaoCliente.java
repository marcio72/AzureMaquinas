package br.com.locaweb.relatorioclientes.model;

/**
 * Situação da confirmação do serviço pelo cliente, via WhatsApp,
 * após a finalização de um chamado aberto pelo app do cliente.
 *
 * NULL na solicitação = sem pendência de confirmação (chamado antigo,
 * chamado de técnico, ou WhatsApp desligado quando finalizou).
 */
public enum ConfirmacaoCliente {
    AGUARDANDO,        // mensagem de finalização enviada, aguardando resposta 1/2
    CONFIRMADO,        // cliente respondeu 1 - serviço OK
    PROBLEMA_CONTINUA  // cliente respondeu 2 - equipe avisada via Signal
}
