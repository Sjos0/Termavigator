# Checklist: Modo desenvolvedor (F1 da ponte com a IA do site)

Referência: doc 06 "Ponte com a IA do Site (TermAI)" no Drive.
Branch: `feature/modo-desenvolvedor`. Status dos itens de código: escritos, ainda não compilados.

## Escopo da F1
- [x] Tela de modo desenvolvedor, acessível em Configurações
- [x] Abrir um endereço em WebView próprio, separado das abas
- [x] Extrair árvore reduzida da página (campos, botões, mensagens, indicadores)
- [x] Seletor de elementos por toque, com papéis: entrada, enviar, pensando, resposta, parar
- [x] Testar o seletor, mostrando quantos elementos ele encontra
- [x] Salvar perfil em JSON no armazenamento interno do app
- [x] Listar e apagar perfis
- [ ] Teste de calibração completo, com mensagem de teste (F2)
- [ ] Ciclo de envio e leitura (F2)

## Segurança
- [x] Ponte JavaScript só existe na tela de modo desenvolvedor, e é removida ao sair
- [x] Aviso visível na tela de que a página recebe uma ponte de JavaScript
- [ ] Decidir se a ponte deve ser restrita aos domínios cadastrados em perfis
- [ ] Revisar se a árvore extraída contém dados sensíveis antes de copiar

## Qualidade
- [ ] Build release compila, com minSdk 26 e as ABIs arm64-v8a e armeabi-v7a
- [ ] Textos dos papéis migrados para strings.xml (hoje estão fixos em português na tela)
- [ ] Testar seletor em um site real, com a tela aberta
- [ ] Medir RAM com a tela de modo desenvolvedor aberta

## Próximo passo
- [ ] Adaptar o TermAI para consumir os perfis (fase Termux, depois do modo desenvolvedor validado)
