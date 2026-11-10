# Termavigator

Termavigator é um navegador Android leve, personalizável e de alto desempenho, desenvolvido com Jetpack Compose e renderização via Android System WebView.

## Metas de Desempenho e Requisitos

- **Compatibilidade:** Android 8.0+ (API 26 mínima).
- **ABIs Suportadas:** `armeabi-v7a` (32 bits) e `arm64-v8a` (64 bits).
- **Tamanho do APK:** Alvo ≤ 15 MB (limite estrito: 20 MB). R8 minification e shrinkResources ativados.
- **Consumo de RAM em repouso:** < 50 MB.
- **Gerenciamento de memória:** Limite de 3 abas ativas simultâneas (ajustável). Abas excedentes entram em modo pausado para poupar recursos.

## Como compilar

### Build de Distribuição (Release)
```bash
gradle :app:assembleRelease
```

### Build de Depuração (Debug)
```bash
gradle :app:assembleDebug
```

O artefato gerado é copiado para a pasta `/APK` do projeto:
- `APK/termavigator-release.apk`

## Como medir o tamanho do APK

```bash
ls -lh APK/termavigator-release.apk
```

Para analisar o tamanho por ABI e recursos:
```bash
unzip -l APK/termavigator-release.apk | grep "lib/"
```

## Como medir o consumo de RAM

Com o aplicativo aberto no aparelho ou emulador de teste:
```bash
adb shell dumpsys meminfo com.termavigator.app
```

Consulte a linha `TOTAL PSS` e `TOTAL RSS` com o aplicativo em repouso e com 3 abas abertas.

## Estrutura de Telas

- **T01 · Navegação:** Área web com WebView, barra inferior em pílula e indicador de progresso em gradiente de ação.
- **T02 · Nova aba:** Cabeçalho em gradiente de marca, campo de busca com microfone/câmera, grade 4x2 de atalhos e favoritos recentes.
- **T03 · Gerenciador de abas:** Lista de abas ativas e pausadas, botão flutuante FAB de nova aba.
- **T04 · Abas arquivadas:** Gerenciamento de abas salvas com ações de restaurar e excluir.
- **T05 · Menu:** Bottom sheet deslizante com acesso rápido a histórico, favoritos, downloads, configurações e modo anônimo.
- **T06 · Histórico:** Pesquisa e histórico agrupado por Hoje/Ontem.
- **T07 · Favoritos:** Lista de favoritos com opções de edição e exclusão.
- **T08 · Downloads:** Gerenciador de arquivos baixados com suporte a formatos ZIP, PDF e imagens.
- **T09 · Configurações:** Preferências gerais, privacidade, aparência, memória e sobre.
- **T10 · Aparência:** Seletor de temas (Claro, Escuro, Automático), cor de destaque e posição da barra.
- **T11 · Privacidade:** Bloqueio de anúncios e rastreadores, limpeza de cookies e dados.
- **T12 · Memória:** Controle do limite de abas ativas e liberação automática de memória.
- **T13 · Modo anônimo:** Navegação com descarte total de histórico e cookies ao fechar.
- **B01 a B04:** Estrutura de backup preparada (v0.2, inativa).
