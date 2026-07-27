# relatorio-clientes

Sistema de gestão de máquinas de jogo/vending em pontos de cliente (bares, mercados etc).
Backend Java Spring Boot + Thymeleaf, banco MySQL, deploy no Azure. Trabalha em conjunto com
um app Android (`App_Controle`) que não faz parte deste repositório.

## Comandos

- Build: `./mvnw clean install`
- Rodar local: `./mvnw spring-boot:run`
- Sem testes automatizados configurados no projeto ainda.

## Arquitetura / domínio

### Cliente (`Tbl_Cliente`)
- `codCliente`, `nomCliente`, `logradouro` (`log`), `bairro` (`bai`), `telefone` (`tel`), `contato` (`cont`)
- `praca` (String, ex.: "V1", "V3"...) — identifica a praça/rota do cliente
- `leiturista` (Integer) — **deve ser sempre derivado da praça**: praça "V3" → leiturista = 3.
  Ver `cadastro-cliente-maquinas.html` (`salvarTudo()`) pra lógica de conversão.
- `regiao` (Integer, convertido por `ConvertRegiao` util pra nome de exibição)
- `ativo` (Boolean) — **sempre forçar `true` no backend ao cadastrar** (trava de segurança em
  `CadastroService.cadastrarClienteComMaquinas` e em `ClienteCrudController.salvarCliente`),
  não confiar só no que o front manda.

### ⚠️ Cliente genérico "INSTALAÇÃO" (`cod_cliente = 1`)
Não é um cliente real — é uma "gaveta" pra agrupar `SolicitacaoManutencao` que são pedidos de
instalação em ponto novo (não manutenção de cliente existente). **Sempre excluir esse ID de**
**listas/árvores de clientes reais** (ver `CLIENTE_ID_INSTALACAO` nas classes que usam isso:
`HomeController`, `ExplorerController`, `InstalacoesViewController`).

Detecção de "é instalação" em qualquer lugar do sistema = `solicitacao.cliente.codCliente == 1`
(nunca por texto/nome do serviço, é frágil).

### Maquina
- `nom_maq`, `nom_jogo`, `numeroPlaca`, `codCliente` (Integer, não é FK JPA de verdade),
  `ativo`
- Colunas de "slot" (`monitor`, `fonte`, `coletor`, `modPlaca`) guardam o ID da peça
  atualmente instalada naquele slot daquela máquina.

### Peca / HistoricoPeca / MovimentoEstoque
- `Peca.status`: `ESTOQUE` | `INSTALADA` | `DESCARTADA`
- `Peca.categoria` → `Categoria.nome` (ex.: "Placa Mãe", "Fonte", "Monitor", "Coletor", "Jogo")
- `HistoricoPeca`: log append-only de eventos (`tipoEvento`: ENTRADA_ESTOQUE, INSTALACAO,
  RETIRADA, AVALIACAO). Só tem menção em **texto livre** à execução que gerou o evento — não é
  uma FK confiável.
- `MovimentoEstoque`: **essa é a fonte confiável** pra ligar troca de peça → execução. Tem FK
  própria pra `execucao` (`ExecucaoManutencao`), `peca`, `maquina`, `cliente`. Usar esta tabela
  (não `HistoricoPeca`) sempre que precisar saber "qual execução trocou qual peça".

### Fluxo de manutenção
`SolicitacaoManutencao` (status Boolean: `true` = aberta/pendente, `false` = concluída/fechada)
  → tem uma lista de `ProblemaMaquina` (cada um ligado a uma `Maquina`)
    → cada `ProblemaMaquina` pode ter **uma** `ExecucaoManutencao` vinculada (`@OneToOne`),
      com `tecnico`, `valor`, `dataExecucao`, `descricao` (relatório/observações do técnico).

O "NF" que aparece nos relatórios pro usuário é literalmente o **id da ExecucaoManutencao**.

### Padrão de texto pro "Descreva o Problema" (quando é instalação)
Formato de 3 linhas esperado (usado por `form_solicitacao.html` ao pré-preencher, e
interpretado por `cadastro-cliente-maquinas.html` ao fazer o parse de volta):
```
Nome do Cliente(Nome do Ponto)
Endereço - Bairro
11 9999 9999 - Nome Contato
```
Linha 2 e linha 3 são divididas por `" - "` (com espaços). Se mudar esse formato num lugar,
mudar a lógica de parse no outro.

## Telas principais

- `/` — Dashboard (home.html): cards de estoque, Ações Rápidas
- `/explorer` — árvore Cliente → Máquina (ExplorerController), com filtro por praça,
  trocas de peça por categoria (via MovimentoEstoque), manutenções com NF clicável
- `/instalacoes` — lista de solicitações do cliente "INSTALAÇÃO", com filtro de período
- `/clientes/clientes-e-maquinas` — listagem principal de clientes com contagem de máquinas
- `/clientes/novo-com-maquinas` — cadastro combinado de cliente + máquinas
- `/form_execucao` — execução de serviço (técnico registra o que foi feito); arquivo grande,
  **muitas funções importantes, cuidado extra ao editar** — sempre revisar o arquivo inteiro
  antes de mexer, mudança cirúrgica só no necessário.
- `/form_solicitacao` — abertura de solicitação de manutenção/instalação
- `/menu` — menu principal (mobile-friendly, usado por técnicos em campo); redireciona
  pra `/login` se não houver `usuarioLogado` na sessão

## Convenções e pegadinhas já resolvidas (não reintroduzir)

- **Nunca usar `new Date().toISOString()` pra preencher campo de hora/data padrão** — isso
  retorna UTC, não hora local, e causa adiantamento (ex.: 3h no Brasil). Montar a string
  manualmente com `getFullYear()/getMonth()/getDate()/getHours()/getMinutes()`.
- Campo "Técnico" em `form_execucao.html` vem do parâmetro `?tecnico=` na URL (setado por
  `menu.html` a partir do usuário logado na sessão) — é `readonly`, não deve virar editável.
- Ponte entre `form_execucao.html` e `cadastro-cliente-maquinas.html` (fluxo de instalação)
  é feita **via query string na URL** (`solicitacaoId`, `idProblema`, `tecnico`,
  `resumoInstalacao`/`pedidoTexto`), não via `localStorage` — localStorage vazava dado entre
  sessões/abas diferentes.
- Ao adicionar CSS num template que usa `fragments/menu.html` (sidebar), sempre usar
  `!important` nas propriedades de cor — o fragmento carrega Bootstrap de novo e sobrescreve
  estilos sem `!important`.

## Preferências de trabalho do Marcio

- Confirmar antes de gerar código quando a mudança for grande/ambígua; mudanças pequenas e
  claras pode implementar direto.
- Mudança cirúrgica — não tocar em código não relacionado ao pedido, principalmente em
  arquivos grandes como `form_execucao.html`.
- Testa sempre no deploy real do Azure depois.
