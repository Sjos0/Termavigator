# PRD Completo: Termavigator para Google AI Studio

**Versão:** 1.0
**Data:** 10/11/2026
**Destino:** Google AI Studio (geração do projeto Android e das telas), depois GitHub (build)
**Documentos de referência:** PRD de Design v0.1, Escopo v0.1, Arquitetura v0.1, Backup na Conta Google v0.1, Telas e Fluxos v0.1

---

## 1. Resumo do produto

O Termavigator é um navegador Android leve e personalizável, feito para substituir o Chrome no uso diário. Ele usa o WebView do próprio sistema para renderizar páginas, o que garante compatibilidade com a web comum sem a necessidade de um motor próprio.

Dois requisitos guiam todas as decisões deste projeto:

1. **App leve em tamanho:** APK release com menos de 20 MB (alvo de 15 MB ou menos).
2. **Uso de RAM leve:** o app, sem páginas abertas, deve ficar abaixo de 50 MB de RAM em uso. O aparelho de referência tem 2 GB de RAM.

Qualquer decisão de implementação que aumente tamanho ou memória sem necessidade deve ser evitada.

## 2. Objetivo desta geração

Gerar o projeto Android completo, compilável, com:

- todas as telas do MVP (v0.1), seguindo as imagens de referência e o sistema visual deste documento;
- a navegação entre telas;
- a lógica de abas, histórico, favoritos e downloads em uma camada de núcleo com interface bem definida;
- a estrutura para o backup (v0.2), preparada mas não ativa.

Não é objetivo desta geração: integração com o Rust, sincronização, extensões, publicação na Play Store.

## 3. Escopo

### 3.1 Dentro do MVP (v0.1)

- Navegação: endereço, voltar, avançar, recarregar, parar carregamento.
- Barra de endereço: aceita URL ou termo de pesquisa.
- Abas: criar, fechar, alternar, arquivar, pausar. Restaurar a última sessão ao abrir o app.
- Histórico com busca e limpeza.
- Favoritos: adicionar, editar, remover.
- Downloads: listar status e abrir arquivos concluídos.
- Bloqueio básico de anúncios e rastreadores, por lista de domínios.
- Modo anônimo: nada é guardado, cookies são descartados ao fechar a aba.
- Configurações: Geral, Privacidade, Aparência, Memória, Sobre.
- Aparência: tema claro, escuro ou automático; cor de destaque; posição da barra de endereço (base ou topo).
- Controle do limite de abas ativas (padrão 3).

### 3.2 Fora do escopo desta geração

- Motor de renderização próprio.
- Sincronização entre aparelhos (exceto o backup da v0.2).
- Gerenciador de senhas, preenchimento automático.
- Extensões.
- Login com conta Google dentro do WebView.
- Agentes e controle externo do navegador (fase futura).
- Publicação na Play Store.

### 3.3 Preparado, mas não ativo (v0.2, backup)

As telas B01 a B04 existem como código, mas a funcionalidade fica desligada. A camada de backup entra como interface com implementação vazia.

## 4. Requisitos de tamanho e memória

Esta seção tem prioridade sobre conveniência de implementação.

### 4.1 Metas mensuráveis

| Métrica | Meta | Como medir |
|---|---|---|
| Tamanho do APK release (arm64) | ≤ 15 MB (limite rígido: 20 MB) | Tamanho do arquivo gerado no build |
| RAM do app em repouso, sem páginas | < 50 MB | `adb shell dumpsys meminfo com.termavigator.app` |
| RAM com 3 abas abertas em sites comuns | Definir teto após testes; meta provisória 250 MB | Mesmo comando, com as abas abertas |
| Tempo até a tela interativa | < 1,5 s no aparelho de 2 GB | Log de inicialização |
| Tempo de troca de aba pausada | < 1 s | Teste manual |

### 4.2 Regras de implementação para tamanho

- Build release com `minifyEnabled true` e `shrinkResources true` (R8).
- Gerar APK apenas para `arm64-v8a` por padrão. Incluir `armeabi-v7a` somente se for necessário para algum aparelho de teste.
- Usar apenas ícones e gráficos vetoriais (VectorDrawable). Sem PNG grandes, sem SVG pesado, sem imagens de fundo.
- Usar a fonte padrão do sistema (Roboto). Não embutir fontes.
- Sem animações pesadas e sem bibliotecas de animação (Lottie, por exemplo).
- Não incluir SDKs de analytics, anúncios, crash reporting ou Firebase.
- Antes de adicionar qualquer biblioteca, justificar o custo em tamanho. Preferir a biblioteca padrão do Android e o Jetpack.
- Sem bibliotecas de carregamento de imagem (Glide, Coil, Picasso). Os favicons, se usados, são pequenos e tratados com cache mínimo.

### 4.3 Regras de implementação para memória

- Cada aba ativa usa um WebView. O limite padrão é 3 abas ativas. As demais são pausadas: o WebView é destruído e a aba guarda apenas URL, título e posição de rolagem. Ao ser reativada, a página é recarregada.
- Ao receber `onTrimMemory` com nível moderado ou superior, pausar e destruir os WebViews de abas inativas.
- Chamar `onPause` e `pauseTimers` em WebViews que não estão visíveis. Chamar `resumeTimers` ao voltar.
- Limitar o cache do WebView (por exemplo, 20 MB) e não manter cache de páginas no próprio app.
- Não guardar bitmaps de miniaturas de abas na memória. Miniaturas, se usadas, são geradas de forma pequena e descartadas quando não estão visíveis.
- Usar coroutines com `Dispatchers.IO` para histórico, favoritos e downloads. Nada de trabalho pesado na thread principal.
- Evitar listas inteiras em memória: histórico e favoritos devem usar lazy lists com paginação simples.
- Não usar `GlobalScope`. Escopos de coroutine ligados ao ciclo de vida da tela.

### 4.4 Regras de implementação para desempenho

- Jetpack Compose para as telas, com `remember` e `derivedStateOf` onde fizer sentido.
- Evitar recomposições desnecessárias: estados imutáveis e `key` nas listas.
- Gradientes simples, sem múltiplas camadas, sem animação de gradiente.

## 5. Plataforma e stack

- **Linguagem:** Kotlin.
- **UI:** Jetpack Compose, Material 3 como base, com tokens customizados (seção 6).
- **Renderização:** Android System WebView.
- **Persistência:** SQLite com helper simples, ou Room se o ganho de produtividade justificar o custo em tamanho. Decisão a ser registrada no README.
- **Preferências:** DataStore Preferences.
- **Downloads:** DownloadManager do sistema.
- **Bloqueio de conteúdo:** interceptação em `shouldInterceptRequest`, com lista de domínios em arquivo local.
- **Rede (apenas para listas de bloqueio e, depois, Drive):** cliente HTTP leve. Não adicionar outros clientes.
- **Build:** Gradle (Kotlin DSL), Android Gradle Plugin atual, GitHub Actions para o APK.
- **minSdk:** 26 (Android 8.0). Confirmar com o aparelho de teste antes de fixar.
- **targetSdk:** a versão estável mais recente do Android no momento do build.
- **applicationId:** `com.termavigator.app`.

### 5.1 Núcleo (camada de lógica)

Nesta geração, a lógica do navegador fica em Kotlin, atrás de uma interface, para que a implementação possa ser trocada por Rust depois sem mudar as telas.

Interface principal, `BrowserCore`, responsável por:

- normalizar texto da barra de endereço em URL ou busca;
- gerenciar abas (criar, fechar, alternar, arquivar, pausar, restaurar);
- gerenciar histórico, favoritos e downloads;
- decidir se uma requisição deve ser bloqueada;
- guardar e restaurar a sessão.

A implementação inicial é `KotlinBrowserCore`, em Kotlin puro, sem dependências externas.

## 6. Sistema visual

### 6.1 Paleta

| Token | Valor | Uso |
|---|---|---|
| `white` | #FFFFFF | Fundo no modo claro |
| `surface.light` | #F4F8FD | Superfícies no modo claro |
| `blue.light` | #5BB8F5 | Destaques secundários, ícones ativos |
| `blue.dark` | #0A2A5E | Cabeçalhos, superfícies no modo escuro |
| `yellow` | #FFC93C | Ação primária, aba ativa |
| `orange` | #FF8A1F | Ponta de gradiente de ação, alertas leves |
| `black` | #0B0B0F | Fundo no modo escuro, texto no modo claro |

**Proporção:** modo claro predominantemente branco e azul. Modo escuro predominantemente preto e azul escuro. Amarelo e laranja em pontos de ação, nunca como fundo de tela inteira.

### 6.2 Gradientes

- `gradient.brand`: linear, 135°, de `blue.light` para `blue.dark`. Cabeçalhos e tela de nova aba.
- `gradient.action`: linear, 90°, de `yellow` para `orange`. Botão primário, indicador de progresso, aba ativa.
- `gradient.night`: linear, 180°, de `black` para `blue.dark`. Cabeçalho sutil no modo escuro.

Regras: no máximo um gradiente de destaque por tela além do fundo; gradientes estáticos; nunca em texto longo.

### 6.3 Contraste

- Mínimo WCAG AA (4.5:1 para texto normal).
- Texto sobre amarelo e laranja: preto.
- Texto branco apenas sobre azul escuro ou preto. Nunca sobre azul claro.
- Alvos de toque de no mínimo 48 dp.

### 6.4 Tipografia (sp)

- Título de tela: 24, bold.
- Título de seção: 18, medium.
- Corpo: 16, regular.
- Legenda: 13, regular.
- Rótulo de botão e aba: 14, medium.

### 6.5 Espaçamento, formas e ícones

- Grade de 8 dp.
- Cards: raio 12 dp. Barra de endereço: pílula (raio total). Botões: raio 16 dp.
- Ícones: estilo linear, traço uniforme, 24 dp padrão, em VectorDrawable.

### 6.6 Modos

Todas as telas funcionam em modo claro e escuro, com tema automático seguindo o sistema por padrão.

## 7. Layout e navegação

### 7.1 Estrutura de navegação

- Tela principal (T01) é a tela raiz.
- A partir de T01, abrem: T03 (abas), T05 (menu), T02 (nova aba, dentro do fluxo de abas).
- A partir de T05: T06 (histórico), T07 (favoritos), T08 (downloads), T09 (configurações), T13 (modo anônimo).
- A partir de T09: T10 (aparência), T11 (privacidade), T12 (memória), e Sobre.
- T04 (abas arquivadas) é aberta a partir de T03.
- Telas B01 a B04 ficam atrás de uma chave de ativação em Privacidade ou Geral, desligadas por padrão.

### 7.2 Barra inferior (padrão: base)

Da esquerda para a direita: voltar, barra de endereço (ocupa o centro), avançar, contador de abas, menu.

Com a configuração "topo", a barra de endereço vai para o topo e a barra inferior mantém apenas voltar, avançar, contador de abas e menu.

### 7.3 Indicador de progresso

Linha fina de `gradient.action` no topo da área da página, visível apenas durante o carregamento.

### 7.4 Área de conteúdo

Ocupa toda a tela entre a barra de status e a barra inferior. O WebView respeita as áreas seguras do sistema (insets).

## 8. Telas

Para cada tela: objetivo, elementos, estados e comportamento. As imagens de referência geradas previamente devem ser usadas como guia visual de cada tela.

### T01 · Navegação
- **Elementos:** área da página, barra inferior, indicador de progresso.
- **Estados:** carregando, carregada, bloqueada por anúncio, erro de conexão, sem página (aba vazia leva a T02).
- **Comportamento:** o botão voltar percorre o histórico da aba; ao não haver histórico, volta para T03 ou fecha a aba, conforme configuração.

### T02 · Nova aba
- **Elementos:** cabeçalho com `gradient.brand`, campo de busca grande, grade de atalhos (favoritos recentes, até 8), barra inferior.
- **Estados:** vazia (sem favoritos), com favoritos.
- **Comportamento:** enviar texto na busca abre T01 com a URL ou a busca.

### T03 · Gerenciador de abas
- **Elementos:** lista de abas abertas e seção de abas pausadas; cada item com miniatura pequena ou título, ações de fechar e arquivar; botão flutuante de nova aba em `gradient.action`.
- **Estados:** com abas, sem abas, aba pausada com indicador próprio.
- **Comportamento:** tocar em aba pausada a reativa e recarrega a página.

### T04 · Abas arquivadas
- **Elementos:** lista de abas arquivadas com título e endereço; ações de reabrir e excluir.
- **Estados:** vazia, com itens.

### T05 · Menu
- **Elementos:** folha inferior com nova aba, nova aba anônima (destaque preto), histórico, favoritos, downloads, configurações, adicionar aos favoritos, compartilhar.
- **Comportamento:** compartilhar usa a intent de compartilhamento do sistema.

### T06 · Histórico
- **Elementos:** campo de busca, lista agrupada por dia, ação de limpar por período.
- **Estados:** vazio, com itens, sem resultados.
- **Comportamento:** tocar em um item abre a página em uma nova aba.

### T07 · Favoritos
- **Elementos:** lista com ícone, título e endereço; editar e excluir deslizando o item; botão de adicionar.
- **Estados:** vazio, com itens.

### T08 · Downloads
- **Elementos:** lista com nome, tamanho, status (em andamento, concluído, falhou), ação de abrir.
- **Estados:** vazio, em andamento, concluído, falhou.
- **Comportamento:** abrir usa intent do sistema para o tipo de arquivo.

### T09 · Configurações
- **Elementos:** grupos Geral, Privacidade, Aparência, Memória, Sobre, em lista com ícone, rótulo e seta.

### T10 · Aparência
- **Elementos:** seletor de tema (claro, escuro, automático), seleção de cor de destaque, posição da barra de endereço (base ou topo), prévia ao vivo.
- **Comportamento:** a mudança é aplicada imediatamente e salva.

### T11 · Privacidade
- **Elementos:** chave de bloqueio de anúncios e rastreadores; botões de limpar dados de navegação e limpar cookies (com confirmação); bloco explicativo sobre o modo anônimo.

### T12 · Memória
- **Elementos:** controle deslizante do limite de abas ativas (1 a 5, padrão 3); texto explicativo curto; indicador simples de uso.
- **Comportamento:** ao reduzir o limite, abas excedentes são pausadas.

### T13 · Modo anônimo
- **Elementos:** barra inferior preta, ícone de anonimato, aviso discreto de que histórico e cookies não são guardados.
- **Comportamento:** cookies e cache da aba anônima são descartados ao fechar. Nada entra no histórico.

### B01 · Backup na conta Google (v0.2, desligada)
- **Elementos:** chave de ativação, categorias a enviar (abas, arquivadas, favoritos, configurações, histórico opcional), backup manual, data do último backup.

### B02 · Criar senha de backup (v0.2, desligada)
- **Elementos:** dois campos de senha, indicador de força, aviso de que sem a senha o backup não pode ser recuperado, botão de confirmar.

### B03 · Restaurar em novo aparelho (v0.2, desligada)
- **Elementos:** login com Google, campo de senha de backup, escolha do que restaurar, progresso.

### B04 · Gerenciar backup (v0.2, desligada)
- **Elementos:** resumo por categoria, apagar backup com confirmação, desligar backup automático.

## 9. Estados transversais

- **Vazio:** mensagem curta e uma ação clara.
- **Carregando:** indicador de progresso fino, sem animação decorativa.
- **Erro:** texto explicativo e ação de tentar novamente.
- **Memória baixa:** aviso discreto quando abas são pausadas pelo sistema.

## 10. Modelo de dados

- **Aba:** id, URL, título, posição de rolagem, estado (ativa, pausada, arquivada), data de criação, data de última visita.
- **Histórico:** id, URL, título, data e hora.
- **Favorito:** id, URL, título, pasta opcional, data.
- **Download:** id, nome, caminho, tamanho, status, data.
- **Configurações:** tema, cor de destaque, posição da barra, limite de abas ativas, bloqueio de anúncios on/off, configurações de backup (v0.2).

## 11. Privacidade e segurança

- Cookies e sessões ficam apenas no aparelho, dentro do WebView.
- Modo anônimo: sem histórico, cookies descartados ao fechar a aba.
- Nenhum dado é enviado a servidores do próprio app.
- Permissões mínimas: internet, notificações (somente para downloads, se o sistema exigir), armazenamento apenas para downloads.
- Nada de rastreamento ou analytics.
- Listas de bloqueio são arquivos locais, atualizados apenas quando o usuário pedir.

## 12. Backup na conta Google (v0.2, apenas a estrutura)

Quando ativado no futuro, o backup segue estas regras, já definidas:

- Armazenamento na pasta privada do app no Google Drive do usuário (escopo appDataFolder). Não usar Firebase.
- Login com Google pelo fluxo nativo do Android, nunca por WebView.
- Dados criptografados no aparelho antes do envio, com chave derivada de uma senha de backup definida pelo usuário.
- Backup manual na primeira versão; automático depois, com limite de frequência.
- Cookies, tokens e senhas nunca entram no backup.
- Ativação explícita, desligado por padrão.

Nesta geração, criar apenas a interface `BackupService` com implementação vazia e as telas B01 a B04 desativadas.

## 13. Estrutura do projeto

```
termavigator/
├── app/
│   ├── src/main/
│   │   ├── java/com/termavigator/app/
│   │   │   ├── ui/              (telas Compose: T01 a T13, B01 a B04)
│   │   │   ├── theme/           (tokens: cores, gradientes, tipografia, formas)
│   │   │   ├── navigation/      (grafo de navegação)
│   │   │   ├── shell/           (gerenciamento de WebViews e abas ativas/pausadas)
│   │   │   ├── core/            (interface BrowserCore e KotlinBrowserCore)
│   │   │   ├── blocking/        (bloqueio por lista de domínios)
│   │   │   ├── storage/         (SQLite: histórico, favoritos, abas, downloads)
│   │   │   ├── settings/        (DataStore)
│   │   │   └── backup/          (BackupService: interface e implementação vazia)
│   │   ├── res/
│   │   │   ├── drawable/        (apenas VectorDrawable)
│   │   │   └── values/          (strings em português do Brasil)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── docs/                        (PRDs e registros de decisão)
├── .github/workflows/build.yml  (gera o APK release a cada commit)
├── README.md                    (como compilar, medir memória e tamanho)
├── settings.gradle.kts
└── build.gradle.kts
```

## 14. Build e entrega

- Repositório no GitHub, branch principal.
- Workflow do GitHub Actions: compila o APK release e publica como artefato.
- README com: como compilar, como medir tamanho e memória, versão mínima do Android, decisões registradas.
- O APK deve ser instalável diretamente no aparelho (instalação de fontes desconhecidas).

## 15. Critérios de aceite

**Tamanho e memória**
- APK release arm64 com menos de 20 MB.
- RAM em repouso abaixo de 50 MB, medida com `dumpsys meminfo`.
- Com 3 abas abertas, o app não é encerrado pelo sistema durante uso normal.

**Funcional**
- Navegar em 20 sites populares sem falhas de exibição críticas.
- Abrir, alternar, arquivar e pausar abas. Abas pausadas recarregam ao serem selecionadas.
- Restaurar as abas ao reabrir o app.
- Histórico, favoritos e downloads funcionam e persistem após reiniciar.
- Bloqueio de anúncios de uma lista de teste.
- Modo anônimo não grava histórico e descarta cookies ao fechar.
- Troca de tema e de posição da barra persistem após reiniciar.

**Visual**
- Telas batem com as imagens de referência em modo claro e escuro.
- Paleta, gradientes e contraste seguem a seção 6.
- Textos em português do Brasil.

**Técnico**
- Projeto compila sem erros no GitHub Actions.
- Nenhum SDK de analytics, anúncios ou Firebase.
- Interface `BrowserCore` separada das telas.

## 16. O que não fazer

- Não adicionar bibliotecas sem justificar o custo em tamanho.
- Não criar motor de renderização próprio.
- Não usar login Google dentro do WebView.
- Não manter cache de páginas no próprio app.
- Não guardar miniaturas grandes na memória.
- Não criar animações pesadas ou gradientes em camadas múltiplas.
- Não incluir código de backup ativo nesta geração.

## 17. Pontos em aberto

- Padrão de abas no gerenciador: lista ou grade (T03).
- Atalho do modo anônimo na página inicial (T02).
- Tela de onboarding inicial, ou abrir direto na navegação.
- minSdk final, após confirmar a versão do Android do aparelho de teste.
- Room ou SQLite puro, decidido pelo primeiro build e pelo tamanho do APK.

## 18. Ordem sugerida de geração

1. Projeto Gradle base, tema e tokens, build do APK vazio com medição de tamanho.
2. Shell com um WebView e medição de RAM com uma aba.
3. Gerenciamento de abas ativas e pausadas, com limite.
4. Telas T01, T02, T03, T05.
5. Histórico, favoritos, downloads (T06, T07, T08).
6. Configurações, aparência, privacidade, memória (T09 a T12).
7. Modo anônimo (T13) e abas arquivadas (T04).
8. Bloqueio de anúncios básico.
9. Estrutura de backup, desligada (B01 a B04 e `BackupService`).
10. Revisão de tamanho e memória antes de encerrar.
