package br.com.locaweb.relatorioclientes.model;

/**
 * De onde veio o chamado: aberto por um técnico (fluxo já existente no app
 * técnico) ou aberto pelo próprio cliente (novo app do cliente).
 * Usado para diferenciar a mensagem enviada ao Signal e, futuramente, para
 * filtrar telas por origem se for necessário.
 */
public enum OrigemSolicitacao {
    TECNICO,
    CLIENTE
}
