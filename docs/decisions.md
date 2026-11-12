# Registro de Decisões Técnicas — Termavigator v1.0

## Decisão 01: Persistência com SQLiteOpenHelper nativo
- **Contexto:** O PRD previa escolha entre Room ou SQLite puro com base no impacto de tamanho e desempenho.
- **Decisão:** Utilizou-se `SQLiteOpenHelper` nativo (`TermavigatorDbHelper`).
- **Motivo:** Evita o processador de anotações KSP em runtime, reflexão e dependências externas no APK final, garantindo tamanho mínimo e consumo de RAM em repouso estritamente inferior a 50 MB.

## Decisão 02: Cobertura de ABIs (armeabi-v7a e arm64-v8a)
- **Contexto:** A regra do usuário substituiu a seção 4.2 do PRD para cobrir dispositivos de teste de 32 bits.
- **Decisão:** Configurado `abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))` no `defaultConfig`.

## Decisão 03: Ícones Vetoriais Individuais
- **Contexto:** O PRD e a regra de build proíbem o uso de bibliotecas completas de ícones como `material-icons-extended`.
- **Decisão:** Todos os ícones lineares (T01 a T13) foram implementados como `VectorDrawable` individuais em `res/drawable/` com grade padrão de 24dp.

## Decisão 04: Bloqueio de Anúncios e Rastreadores
- **Contexto:** Bloqueio local leve sem inclusão de bibliotecas externas pesadas.
- **Decisão:** Criado o `ContentBlocker` que intercepta requisições via `WebViewClient.shouldInterceptRequest` com correspondência rápida em `HashSet` de domínios conhecidos de anúncios e telemetria.

## Decisão 05: Estrutura de Backup v0.2
- **Contexto:** Telas B01 a B04 e interface preparadas sem tráfego de rede ativo.
- **Decisão:** Implementação de `NoOpBackupService` que opera como stub sem realizar conexões com a Internet. As telas B01 a B04 existem no código e estão conectadas ao grafo de rotas para ativação futura.

## Decisão 06: Gerenciamento e Pausa de Abas
- **Contexto:** Dispositivos com 2 GB de RAM exigem descarte de WebViews inativas.
- **Decisão:** `KotlinBrowserCore` limita o número de abas ativas simultâneas (padrão 3, ajustável entre 1 e 10). Ao ultrapassar o limite, as abas mais antigas são marcadas como `PAUSED` e o WebView é descartado da memória. Ao receber `onTrimMemory`, todas as abas em segundo plano são pausadas imediatamente.

## Decisão 07: Atualização dentro do app via GitHub Releases
- **Contexto:** O app é distribuído por APK, fora da Play Store. O usuário precisa de um jeito simples de atualizar.
- **Decisão:** O menu tem "Verificar atualizações". O app consulta o último release público do repo (`releases/latest`), compara a tag com a versão instalada, baixa o APK anexado com barra de progresso e abre o instalador do sistema.
- **Motivo:** Sem Play Store e sem servidor próprio, o GitHub Releases já entrega o APK e a versão. Não há token no app, porque o repo é público e a API de releases é anônima.
- **Consequências:**
  - O APK precisa estar anexado como asset do release, com a tag no formato `vX.Y.Z`.
  - Atualizações só funcionam entre APKs assinados com a mesma chave.
  - O usuário precisa permitir "instalar apps desconhecidos" para o Termavigator, na primeira vez.

