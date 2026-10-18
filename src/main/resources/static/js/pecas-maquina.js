/*
 * Bloco "Peças da máquina" usado nas telas de cadastro de máquina.
 * Conversa com /api/cadastro-maquina/opcoes (peças disponíveis no estoque + catálogos).
 *
 * Cada peça: escolher do ESTOQUE ou marcar AVULSA (peça que já está na máquina e
 * nunca entrou no estoque — o backend cria no lote "AVULSO").
 *
 * Uso:
 *   PecasMaquina.init('pecas_box', { onJogo: nome => ... });
 *   const r = PecasMaquina.ler();   // { ok, erro, pecas: [...], resumo: {...}, placaTexto }
 *   PecasMaquina.limpar();
 *   PecasMaquina.reservar(new Set([12, 15]));  // peças já usadas em outras máquinas da lista
 */
(function (global) {
    'use strict';

    let opcoes = [];
    let container = null;
    let reservadas = new Set();
    let config = {};

    const ROTULO_MODELO = {
        PLACA: 'Modelo da placa',
        COLETOR: 'Modelo do coletor',
        JOGO: 'Jogo (catálogo)'
    };
    const DICA_CODIGO = {
        PLACA: 'Ex.: 0517 (no máx. 4 números) — vazio gera automático',
        FONTE: 'Etiqueta da fonte — vazio gera automático',
        MONITOR: 'Etiqueta do monitor — vazio gera automático',
        COLETOR: 'Etiqueta do coletor — vazio gera automático',
        JOGO: 'Etiqueta do jogo — vazio gera automático'
    };

    function esc(t) {
        return (t === null || t === undefined ? '' : String(t))
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function el(id) { return document.getElementById(id); }

    async function init(containerId, cfg) {
        container = el(containerId);
        config = cfg || {};
        try {
            const resp = await fetch('/api/cadastro-maquina/opcoes', { credentials: 'same-origin' });
            if (resp.status === 401) {
                container.innerHTML = '<p class="peca-erro mb-0">Sessão expirada. Faça login novamente para carregar as peças.</p>';
                return;
            }
            if (!resp.ok) throw new Error('Erro ' + resp.status);
            opcoes = await resp.json();
            render();
        } catch (e) {
            container.innerHTML = '<p class="peca-erro mb-0">Não foi possível carregar as peças do estoque: '
                + esc(e.message) + '</p>';
        }
    }

    function render() {
        // guarda o que já estava escolhido para não perder ao re-renderizar
        const atual = {};
        opcoes.forEach(o => {
            const s = el('peca_' + o.slot);
            if (s) atual[o.slot] = {
                v: s.value,
                cod: (el('peca_cod_' + o.slot) || {}).value || '',
                mod: (el('peca_mod_' + o.slot) || {}).value || ''
            };
        });

        container.innerHTML = opcoes.map(o => {
            const disp = o.disponiveis.filter(p => !reservadas.has(p.id));
            const semCategoria = !o.categoriaId;
            const opts = [
                `<option value="">${o.obrigatoria ? '— selecione —' : '— sem ' + esc(o.rotulo.toLowerCase()) + ' —'}</option>`,
                semCategoria ? '' : `<option value="AVULSA">➕ Peça avulsa (fora do estoque)</option>`,
                disp.length
                    ? `<optgroup label="Estoque — ${disp.length} disponível(is)">`
                      + disp.map(p => `<option value="${p.id}">${esc(p.codigo)}${p.detalhe ? ' — ' + esc(p.detalhe) : ''}</option>`).join('')
                      + `</optgroup>`
                    : `<option value="" disabled>(nenhuma no estoque)</option>`
            ].join('');

            const modelos = (o.modelos || []).length
                ? `<div class="col-md-6">
                       <label class="form-label" for="peca_mod_${o.slot}">${ROTULO_MODELO[o.slot] || 'Modelo'}</label>
                       <select class="form-select" id="peca_mod_${o.slot}">
                           <option value="">— não informado —</option>
                           ${o.modelos.map(m => `<option value="${m.id}">${esc(m.nome)}</option>`).join('')}
                       </select>
                   </div>`
                : '';

            return `
              <div class="peca-row" data-slot="${o.slot}">
                <div class="row g-2 align-items-center">
                  <div class="col-md-3">
                    <div class="peca-nome">${esc(o.rotulo)} ${o.obrigatoria ? '<span class="obrig">*</span>' : ''}</div>
                    <div class="peca-cat">${semCategoria
                        ? '<span class="peca-erro">categoria não encontrada no estoque</span>'
                        : esc(o.categoriaNome)}</div>
                  </div>
                  <div class="col-md-9">
                    <select class="form-select" id="peca_${o.slot}" ${semCategoria ? 'disabled' : ''}>${opts}</select>
                  </div>
                </div>
                <div class="peca-avulsa d-none" id="peca_av_${o.slot}">
                  <div class="row g-2">
                    <div class="col-md-6">
                      <label class="form-label" for="peca_cod_${o.slot}"><span class="tag-avulsa">Avulsa</span> — código / etiqueta</label>
                      <input type="text" class="form-control" id="peca_cod_${o.slot}" maxlength="40"
                             placeholder="${esc(DICA_CODIGO[o.slot] || 'Vazio gera automático')}">
                    </div>
                    ${modelos}
                  </div>
                </div>
              </div>`;
        }).join('');

        opcoes.forEach(o => {
            const s = el('peca_' + o.slot);
            if (!s) return;
            s.addEventListener('change', () => aoMudar(o));
            const mod = el('peca_mod_' + o.slot);
            if (mod) mod.addEventListener('change', () => aoMudar(o));

            // restaura escolha anterior (se a peça ainda estiver disponível)
            const a = atual[o.slot];
            if (a) {
                if ([...s.options].some(op => op.value === a.v)) s.value = a.v;
                if (el('peca_cod_' + o.slot)) el('peca_cod_' + o.slot).value = a.cod;
                if (mod && a.mod) mod.value = a.mod;
            }
            mostrarAvulsa(o);
        });
    }

    function mostrarAvulsa(o) {
        const s = el('peca_' + o.slot);
        const box = el('peca_av_' + o.slot);
        if (s && box) box.classList.toggle('d-none', s.value !== 'AVULSA');
    }

    function aoMudar(o) {
        mostrarAvulsa(o);
        // Jogo do catálogo → sugere o nome do jogo na máquina
        if (o.slot === 'JOGO' && typeof config.onJogo === 'function') {
            const nome = nomeDoJogoEscolhido(o);
            if (nome) config.onJogo(nome);
        }
    }

    function nomeDoJogoEscolhido(o) {
        const s = el('peca_' + o.slot);
        if (!s || !s.value) return null;
        if (s.value === 'AVULSA') {
            const mod = el('peca_mod_' + o.slot);
            if (mod && mod.value) {
                const m = o.modelos.find(x => String(x.id) === mod.value);
                return m ? m.nome : null;
            }
            return null;
        }
        const p = o.disponiveis.find(x => String(x.id) === s.value);
        return p ? p.detalhe : null;
    }

    /** Lê o bloco. Não limpa nada. */
    function ler() {
        const pecas = [];
        const resumo = {};
        let placaTexto = '';
        for (const o of opcoes) {
            const s = el('peca_' + o.slot);
            const v = s ? s.value : '';
            if (!v) {
                if (o.obrigatoria) {
                    return { ok: false, erro: 'Informe a peça ' + o.rotulo + ' (do estoque ou avulsa).' };
                }
                continue;
            }
            if (v === 'AVULSA') {
                const cod = (el('peca_cod_' + o.slot) || {}).value || '';
                const mod = (el('peca_mod_' + o.slot) || {}).value || '';
                if (o.slot === 'PLACA' && cod.replace(/\D/g, '').length > 4) {
                    return { ok: false, erro: 'O código da placa pode ter no máximo 4 números.' };
                }
                const modeloNome = mod ? ((o.modelos.find(m => String(m.id) === mod) || {}).nome || '') : '';
                pecas.push({ slot: o.slot, avulsa: true, codigo: cod.trim(), modeloId: mod ? Number(mod) : null });
                resumo[o.slot] = (cod.trim() || '(código automático)') + (modeloNome ? ' — ' + modeloNome : '') + ' [avulsa]';
                if (o.slot === 'PLACA') placaTexto = cod.trim().replace(/\D/g, '') || '(auto)';
            } else {
                const p = o.disponiveis.find(x => String(x.id) === v);
                pecas.push({ slot: o.slot, avulsa: false, pecaId: Number(v) });
                resumo[o.slot] = p ? (p.codigo + (p.detalhe ? ' — ' + p.detalhe : '')) : ('#' + v);
                if (o.slot === 'PLACA' && p) placaTexto = (p.codigo || '').replace(/\D/g, '');
            }
        }
        return { ok: true, pecas, resumo, placaTexto };
    }

    function limpar() {
        opcoes.forEach(o => {
            const s = el('peca_' + o.slot);
            if (s && !s.disabled) s.value = '';
            const c = el('peca_cod_' + o.slot);
            if (c) c.value = '';
            const m = el('peca_mod_' + o.slot);
            if (m) m.value = '';
            mostrarAvulsa(o);
        });
    }

    function reservar(ids) {
        reservadas = new Set(ids || []);
        if (container && opcoes.length) render();
    }

    function rotulo(slot) {
        const o = opcoes.find(x => x.slot === slot);
        return o ? o.rotulo : slot;
    }

    global.PecasMaquina = { init, ler, limpar, reservar, rotulo };
})(window);
