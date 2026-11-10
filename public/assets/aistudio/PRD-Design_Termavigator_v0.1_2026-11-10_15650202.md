# PRD de Design: Termavigator

**Versão:** 0.1
**Data:** 10/11/2026
**Status:** para uso no Google Stitch
**Documentos relacionados:** 01 Escopo, 02 Arquitetura, 04 Backup na Conta Google, 05 Telas e Fluxos

---

## 1. Visão geral

O Termavigator é um navegador Android leve, pensado para substituir o Chrome no uso diário. A interface deve ser funcional e parecida com um navegador conhecido, mas com identidade visual própria. O app roda em celulares com 2 GB de RAM, então a interface precisa ser leve: poucas imagens, gradientes simples e nenhuma animação pesada.

## 2. Objetivo deste PRD

Gerar, no Google Stitch, as telas do MVP (versão 0.1) e as telas de backup (versão 0.2), em modo claro e escuro, seguindo o sistema visual e as regras abaixo.

## 3. Princípios de design

- **Uso com uma mão:** ações frequentes ficam na parte de baixo da tela.
- **Clareza:** cada tela tem uma função principal, visível sem rolagem sempre que possível.
- **Leveza:** ícones vetoriais, sem fotos decorativas, sem efeitos de sombra pesados.
- **Identidade própria:** cores, gradientes e formas formam a assinatura do app.
- **Transparência:** modo anônimo e backup sempre indicam o estado atual.

## 4. Sistema visual

### 4.1 Paleta

| Token | Cor | Uso principal |
|---|---|---|
| `color.white` | #FFFFFF | Fundo principal no modo claro, texto sobre azul escuro |
| `color.surface.light` | #F4F8FD | Superfícies, cards e listas no modo claro |
| `color.blue.light` | #5BB8F5 | Destaques secundários, ícones ativos, links |
| `color.blue.dark` | #0A2A5E | Cabeçalhos, superfícies no modo escuro, texto principal no modo claro |
| `color.yellow` | #FFC93C | Ação primária, aba ativa, indicador de seleção |
| `color.orange` | #FF8A1F | Ação de destaque, ponta de gradiente, alertas leves |
| `color.black` | #0B0B0F | Fundo no modo escuro, texto principal no modo claro |

**Regra de proporção:** no modo claro, predominam branco e azul. No modo escuro, predominam preto e azul escuro. Amarelo e laranja aparecem em pontos de ação, nunca como cor de fundo de telas inteiras.

### 4.2 Gradientes

| Token | Definição | Uso |
|---|---|---|
| `gradient.brand` | Linear, 135°, de #5BB8F5 para #0A2A5E | Cabeçalhos de tela, tela inicial de nova aba, fundo de telas de backup |
| `gradient.action` | Linear, 90°, de #FFC93C para #FF8A1F | Botão primário, indicador de progresso, aba ativa |
| `gradient.night` | Linear, 180°, de #0B0B0F para #0A2A5E | Cabeçalho e fundo sutil no modo escuro |

**Regras de uso dos gradientes:**
- No máximo um gradiente de destaque visível por tela, além do fundo.
- Gradientes são estáticos. Nada de animação de gradiente.
- Nunca usar gradiente como cor de fundo de texto longo.
- Gradientes ficam em áreas grandes e simples, como cabeçalhos e botões, para não pesar no aparelho de 2 GB.

### 4.3 Contraste e acessibilidade

- Texto deve atender no mínimo ao contraste WCAG AA (4.5:1 para texto normal).
- Texto sobre amarelo e laranja deve ser preto (#0B0B0F).
- Texto branco só sobre azul escuro ou preto. Nunca branco sobre azul claro.
- Alvos de toque com no mínimo 48 dp.

### 4.4 Tipografia

- Família: sans-serif do sistema Android (Roboto), para leveza e legibilidade. Pode ser trocada na fase de identidade final.
- Escala:
  - Título de tela: 24 sp, peso bold
  - Título de seção: 18 sp, peso medium
  - Corpo: 16 sp, peso regular
  - Legenda: 13 sp, peso regular
  - Rótulo de botão e aba: 14 sp, peso medium

### 4.5 Espaçamento, formas e ícones

- Grade de 8 dp para espaçamentos.
- Cantos: cards com 12 dp; barra de endereço em formato de pílula (raio total); botões com 16 dp.
- Ícones: estilo linear, traço uniforme, vetoriais. Tamanho padrão 24 dp.

### 4.6 Modos claro e escuro

- **Modo claro:** fundo branco, superfícies #F4F8FD, texto preto, destaques em azul e amarelo/laranja.
- **Modo escuro:** fundo preto, superfícies azul escuro, texto branco, destaques em amarelo/laranja e azul claro.
- Todas as telas devem ser entregues nos dois modos.

## 5. Layout de referência

- **Tela de referência:** 360 × 800 dp (celular Android comum).
- **Barra de endereço:** na base da tela, por padrão, em formato de pílula. Pode ser movida para o topo nas configurações.
- **Barra inferior (com a barra de endereço na base):** voltar à esquerda; barra de endereço ocupando o centro; avançar, contador de abas e menu à direita.
- **Indicador de progresso:** linha fina de gradiente `gradient.action` no topo da página, enquanto ela carrega.
- **Área de conteúdo:** ocupa toda a tela entre a barra superior de status e a barra inferior.

## 6. Telas do MVP (v0.1)

Para cada tela: objetivo, elementos, estados e uma descrição pronta para o Stitch.

### T01 · Navegação
- **Objetivo:** exibir a página carregada.
- **Elementos:** área da página, barra inferior (voltar, avançar, barra de endereço, contador de abas, menu), indicador de progresso no topo.
- **Estados:** página carregando, página carregada, página bloqueada por anúncios, erro de conexão.
- **Prompt:** "Tela principal de navegador Android com área de página ocupando quase toda a tela, barra inferior com barra de endereço em formato de pílula no centro, botões voltar e avançar à esquerda, contador de abas e menu à direita, e uma linha de progresso em gradiente amarelo para laranja no topo."

### T02 · Nova aba (página inicial)
- **Objetivo:** primeira tela de cada nova aba, com busca em destaque.
- **Elementos:** cabeçalho com gradiente de marca, campo de busca grande, favoritos recentes em grade, atalhos.
- **Estados:** vazia (sem favoritos), com favoritos.
- **Prompt:** "Página inicial de nova aba de navegador, com cabeçalho em gradiente azul claro para azul escuro, campo de busca grande no centro, grade de atalhos de favoritos e barra inferior com a mesma barra de endereço do navegador, no modo claro e no modo escuro."

### T03 · Gerenciador de abas
- **Objetivo:** ver, alternar, criar, fechar e arquivar abas.
- **Elementos:** lista de abas abertas e pausadas, miniatura ou título, ações de fechar e arquivar, botão de nova aba com destaque em amarelo.
- **Estados:** com abas, sem abas, aba pausada com indicador próprio.
- **Prompt:** "Tela de gerenciamento de abas de navegador Android, com lista de abas abertas e uma seção separada de abas pausadas, cada item com miniatura, título e botões de fechar e arquivar, e um botão flutuante de nova aba em gradiente amarelo para laranja."

### T04 · Abas arquivadas
- **Objetivo:** guardar abas para depois.
- **Elementos:** lista de abas arquivadas, ação de reabrir e excluir.
- **Estados:** vazia, com itens.
- **Prompt:** "Tela de abas arquivadas de navegador Android, lista simples com título e endereço de cada aba, ações de reabrir e excluir em cada item, e um estado vazio com ilustração vetorial simples."

### T05 · Menu
- **Objetivo:** acessar as demais áreas e ações da página atual.
- **Elementos:** nova aba, nova aba anônima (destaque preto), histórico, favoritos, downloads, configurações, adicionar aos favoritos, compartilhar.
- **Prompt:** "Menu inferior em folha deslizante de navegador Android, com lista de ações com ícones lineares, a opção de nova aba anônima em destaque com fundo preto, e divisores finos entre os grupos."

### T06 · Histórico
- **Objetivo:** consultar páginas visitadas.
- **Elementos:** busca no topo, lista agrupada por dia, ação de limpar por período.
- **Estados:** vazio, com itens, sem resultados na busca.
- **Prompt:** "Tela de histórico de navegador Android, com campo de busca no topo, lista de páginas agrupada por dia com cabeçalhos de seção e botão de limpar histórico no canto superior."

### T07 · Favoritos
- **Objetivo:** gerenciar favoritos.
- **Elementos:** lista, editar e excluir por item, ação de adicionar.
- **Estados:** vazio, com itens.
- **Prompt:** "Tela de favoritos de navegador Android, lista com ícone de site, título e endereço, ações de editar e excluir deslizando o item, e botão de adicionar em gradiente amarelo para laranja."

### T08 · Downloads
- **Objetivo:** acompanhar e abrir arquivos baixados.
- **Elementos:** lista com status (em andamento, concluído, falhou), ação de abrir.
- **Estados:** vazio, em andamento, concluído, falhou.
- **Prompt:** "Tela de downloads de navegador Android, lista de arquivos com nome, tamanho, barra de progresso fina e status, com ação de abrir em cada item concluído."

### T09 · Configurações
- **Objetivo:** organizar as configurações do app.
- **Elementos:** grupos Geral, Privacidade, Aparência, Memória, Sobre. Cada grupo é um item de lista com ícone e seta.
- **Prompt:** "Tela de configurações de navegador Android, com grupos em lista (Geral, Privacidade, Aparência, Memória, Sobre), cada um com ícone linear, texto e seta, e cabeçalho em gradiente azul escuro."

### T10 · Aparência
- **Objetivo:** personalizar o visual do app.
- **Elementos:** seleção de tema (claro, escuro, automático), cor de destaque, posição da barra de endereço (base ou topo), prévia ao vivo.
- **Prompt:** "Tela de aparência de navegador Android, com seletor de tema claro, escuro ou automático, paleta de cores de destaque em círculos, opção de posição da barra de endereço e uma prévia em miniatura do navegador na parte de cima."

### T11 · Privacidade
- **Objetivo:** controlar bloqueio e limpeza de dados.
- **Elementos:** chave de bloqueio de anúncios e rastreadores, limpar dados de navegação, limpar cookies, explicação do modo anônimo.
- **Prompt:** "Tela de privacidade de navegador Android, com chaves de ativação para bloqueio de anúncios e rastreadores, botões de limpar dados e cookies, e um bloco explicativo sobre o modo anônimo."

### T12 · Memória
- **Objetivo:** limitar o número de abas ativas.
- **Elementos:** controle do limite de abas ativas (3 por padrão), explicação do efeito no consumo.
- **Prompt:** "Tela de memória de navegador Android, com controle deslizante para o número de abas ativas, texto explicativo curto sobre o consumo de RAM e um indicador visual simples de uso."

### T13 · Modo anônimo
- **Objetivo:** indicar com clareza a navegação sem histórico.
- **Elementos:** barra inferior em preto, ícone de anonimato, aviso de que histórico e cookies não são guardados.
- **Estados:** navegação anônima ativa.
- **Prompt:** "Tela de navegação anônima de navegador Android, com fundo escuro, barra inferior preta com ícone de anonimato e um aviso discreto no topo informando que histórico e cookies não são guardados."

## 7. Telas de backup (v0.2)

### B01 · Backup na conta Google
- **Elementos:** chave de ativação, categorias a enviar (abas, arquivadas, favoritos, configurações, histórico opcional), botão de backup manual, data do último backup.
- **Prompt:** "Tela de backup na conta Google para navegador Android, com chave de ativação no topo, lista de categorias com caixas de seleção, botão de backup manual em gradiente amarelo para laranja e a data do último backup em texto pequeno."

### B02 · Criar senha de backup
- **Elementos:** campo de senha, confirmação, indicador de força, aviso de que sem a senha o backup não pode ser recuperado.
- **Prompt:** "Tela de criação de senha de backup de navegador Android, com dois campos de senha, indicador de força, um aviso em destaque informando que sem a senha o backup não pode ser recuperado, e botão de confirmar."

### B03 · Restaurar em novo aparelho
- **Elementos:** login com Google, campo de senha de backup, escolha do que restaurar, progresso.
- **Prompt:** "Tela de restauração de backup em novo aparelho para navegador Android, com botão de entrar com Google, campo de senha de backup, lista de itens para restaurar com caixas de seleção e barra de progresso em gradiente."

### B04 · Gerenciar backup
- **Elementos:** resumo do que está enviado por categoria, apagar backup (com confirmação), desligar backup automático.
- **Prompt:** "Tela de gerenciamento de backup de navegador Android, com resumo do que está enviado em cada categoria, botão de apagar backup em vermelho sobre fundo escuro, e chave para desligar o backup automático."

## 8. Componentes

- Barra inferior de navegação com barra de endereço.
- Barra de endereço em pílula, com estados de foco, carregando e página segura ou não.
- Item de lista (aba, histórico, favorito, download), com ações de deslizar.
- Campo de senha com mostrar e ocultar, e indicador de força.
- Diálogo de confirmação para ações destrutivas.
- Cabeçalho de seção com gradiente de marca.
- Botão primário com gradiente de ação.
- Estados vazios com ilustração vetorial simples.

## 9. Estados transversais

- **Vazio:** mensagem curta e ação clara.
- **Carregando:** indicador de progresso fino, sem animação decorativa.
- **Erro:** texto explicativo e ação de tentar novamente.
- **Memória baixa:** aviso discreto quando abas são pausadas pelo sistema.

## 10. Entregáveis esperados do Stitch

1. Telas do MVP (T01 a T13) em modo claro e escuro.
2. Telas de backup (B01 a B04) em modo claro e escuro, na segunda leva.
3. Versões de estado (vazio, erro, carregando) para T01, T03, T06 e T08.
4. Arquivo de tokens com as cores, gradientes e escala tipográfica da seção 4.

## 11. Pontos fechados e em aberto

**Fechados:**
- Paleta e gradientes (seção 4).
- Barra de endereço na base por padrão, com opção de topo.
- Modo claro e escuro para todas as telas.

**Em aberto:**
- Abas em lista ou em grade no gerenciador de abas (T03).
- Se o modo anônimo tem atalho na página inicial (T02).
- Se o onboarding terá uma tela inicial.

## 12. Restrições técnicas

- Implementação em Kotlin com Jetpack Compose.
- Sem imagens grandes; ilustrações vetoriais.
- Textos em português do Brasil.
- Gradientes precisam de implementação simples, sem múltiplas camadas.
