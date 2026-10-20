# Ajuda Aí 1.0 — TP2 de AEDS3

## Participantes

Enzo Russo, Henrique Paes, Otoniel Goulart, Paulo Campos

## O que o sistema faz

O **Ajuda Aí** é um sistema de perguntas e respostas em modo texto (Java). Cada usuário pode:

- criar a conta, entrar no sistema e gerenciar seus dados (TP1);
- publicar, alterar e arquivar perguntas (TP1);
- **buscar as perguntas mais recentes**, em páginas de 5, da mais nova para a mais antiga;
- **responder** a perguntas de qualquer usuário;
- **avaliar** respostas de outros usuários (concordar: +1 / discordar: −1), uma única vez por resposta;
- ver, para cada pergunta, as respostas **ordenadas por nota**;
- na **Minha área**: ver as respostas às suas perguntas (e arquivar as inadequadas), ver e arquivar as respostas que escreveu e ver e cancelar as avaliações que fez.

Nada é excluído fisicamente pela interface: perguntas e respostas são **arquivadas** (`ativa = false`) e deixam de aparecer nas listagens.

## Como compilar e executar

Na raiz do repositório (Linux/macOS):

```
javac -d build $(find aeds3/src -name "*.java")
java -cp build visao.MenuAcesso
```

No PowerShell (Windows):

```
$arquivos = Get-ChildItem -Recurse -Path aeds3\src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d build $arquivos
java -Dfile.encoding=UTF-8 -cp build visao.MenuAcesso
```

Os arquivos de dados são criados na pasta `dados/` na primeira execução.

## Estrutura do projeto e classes

| Pacote | Classe | Papel |
|---|---|---|
| `entidades` | `Usuario`, `Pergunta` | Entidades do TP1 |
| `entidades` | `Resposta` | `idResposta`, `idPergunta`, `idUsuario`, `criacao`, `resposta`, `nota`, `avaliacoes`, `ativa` |
| `entidades` | `Avaliacao` | `idAvaliacao`, `idUsuario`, `idResposta`, `avaliacao` (true = positiva) |
| `dados` | `ArquivoUsuarios`, `ArquivoPergunta` | CRUD do TP1; `ArquivoPergunta` ganhou o índice por data |
| `dados` | `ArquivoResposta` | CRUD de respostas + 2 árvores B+ |
| `dados` | `ArquivoAvaliacao` | CRUD de avaliações + 2 árvores B+ + ajuste da nota |
| `estruturas` | `Arquivo`, `ArvoreBMais`, `HashExtensivel` | CRUD genérico e índices |
| `util` | `ParIntInt` | Par de chaves das árvores de relacionamento |
| `util` | `ParLongInt` | Par (data, idPergunta) do índice de perguntas recentes |
| `visao` | `MenuAcesso` | Login, menu principal e Minha área |
| `visao` | `MenuBusca` | Buscar perguntas e perguntas mais recentes (paginadas) |
| `visao` | `MenuPerguntas` | Minhas perguntas (e respostas às minhas perguntas) |
| `visao` | `MenuResposta` | Responder, avaliar, arquivar, Minhas respostas, Minhas avaliações |

## Operações especiais implementadas

**1. Listagem paginada de perguntas por data decrescente.**
`ArquivoPergunta` mantém a árvore B+ `indiceData`, com chaves `ParLongInt(criacao, idPergunta)`. O método `listarRecentes(pagina)` percorre o índice do mais recente para o mais antigo, pula as perguntas arquivadas e devolve 5 por página. `totalPaginasRecentes()` informa o número de páginas. A tela é `MenuBusca`.

**2. Relacionamento N:N entre usuários e perguntas, por meio de `Resposta`.**
`ArquivoResposta` mantém duas árvores B+ de `ParIntInt`:
- `(idPergunta, idResposta)`: usada em `readByPergunta`, que lista as respostas de uma pergunta;
- `(idUsuario, idResposta)`: usada em `readByUsuario`, que lista as respostas de um usuário (Minhas respostas).

No `create`, o sistema confere se o usuário existe e se a pergunta existe e está ativa. No `delete`, as duas árvores são atualizadas.

**3. Ordenação das respostas por nota.**
`readByPergunta` ordena a lista por nota decrescente (empate: a resposta mais antiga primeiro).

**4. Avaliações.**
`ArquivoAvaliacao` mantém duas árvores B+: `(idResposta, idAvaliacao)` e `(idUsuario, idAvaliacao)`.
- `create`: recusa avaliar a própria resposta, resposta arquivada e voto repetido (`jaAvaliou`); depois ajusta a `Resposta` (nota +1 ou −1 e avaliações +1).
- `delete` (cancelar): remove o registro das duas árvores e **desfaz** o efeito na nota e na contagem.
- Para mudar uma avaliação, o usuário a cancela em *Minhas avaliações* e avalia de novo.

**5. Arquivamento.**
`ArquivoResposta.arquivar` e `ArquivoPergunta.arquivar` fazem `ativa = false`; os registros e os índices continuam. O autor de uma pergunta pode arquivar respostas de outros usuários a ela.

**6. Respostas com vários parágrafos ou código.**
A resposta é digitada em várias linhas e termina com uma linha contendo apenas `.`. Linhas vazias são preservadas.

## Telas

Capturas do terminal, obtidas executando o sistema. Os nomes e as datas são de teste.

### 1. Buscar perguntas

Menu aberto pela opção B do menu principal.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas

(A) Perguntas mais recentes
(B) Busca por palavras-chave

(R) Retornar

Opção: _
```

### 2. Perguntas mais recentes (página 1)

Cinco perguntas por página, da mais recente para a mais antiga. Digita-se o número para abrir uma pergunta.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes
Página 1 de 2

(1) Por que sentimos mais sono depois do almoço?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(2) Como funciona o fuso horário?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(3) Por que o café parece perder o efeito quando é consumido todos os dias?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(4) É realmente necessário fechar os aplicativos do celular em segundo plano?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(5) Por que algumas pessoas conseguem trabalhar melhor ouvindo música?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(B) Próxima página

(R) Retornar

Opção: _
```

### 3. Perguntas mais recentes (página 2)

A opção (B) avança para o próximo conjunto de cinco; a opção (A) volta.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes
Página 2 de 2

(1) Carregar o celular durante a noite prejudica a bateria?
    Criada por Roberto Souza em 20/10/2026 às 00:35

(A) Página anterior

(R) Retornar

Opção: _
```

### 4. Pergunta com as respostas

Respostas ativas em ordem decrescente de nota. Digita-se o número de uma resposta para abri-la.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes > Pergunta

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

RESPOSTAS
---------
(1) Porque o corpo gasta energia para fazer a digestão.
Respondido por Antônio Carlos Machado em 20/10/2026 às 00:35
Nota: 1  (1 avaliações)

(2) Por causa da compressão do diafragma. O volume de comida no estômago
pressiona a base dos pulmões.
Respondido por você em 20/10/2026 às 00:35
Nota: 0  (0 avaliações)

(A) Responder à pergunta

(R) Retornar

Opção: _
```

### 5. Responder à pergunta

O usuário digita só o texto (vários parágrafos ou código, terminando com uma linha contendo apenas um ponto). Os demais atributos são preenchidos automaticamente.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Responder

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

Resposta (pode ter vários parágrafos ou trechos de código;
termine com uma linha contendo apenas um ponto "."):
Confirma o envio da resposta? (S/N): Resposta registrada com sucesso!

Opção: _
```

### 6. Resposta de outro usuário

Opções de concordar (+1 na nota) ou discordar (−1 na nota). Cada usuário avalia uma resposta uma única vez.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

RESPOSTA
--------

Porque o corpo gasta energia para fazer a digestão.
Respondido por Antônio Carlos Machado em 20/10/2026 às 00:35
Nota: 0  (0 avaliações)

(A) Concordar com a resposta
(B) Discordar da resposta

(R) Retornar ao menu anterior

Opção: _
```

### 7. Resposta do próprio usuário

A única opção é arquivar; a resposta não pode ser alterada.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

RESPOSTA
--------

Por causa da compressão do diafragma. O volume de comida no estômago
pressiona a base dos pulmões.
Respondido por você em 20/10/2026 às 00:35
Nota: 0  (0 avaliações)

(A) Arquivar resposta

(R) Retornar ao menu anterior

Opção: _
```

### 8. Minha área > Minhas respostas

Lista das respostas ativas escritas pelo usuário. Ao escolher uma, vê-se a pergunta e a resposta, com a opção de arquivar.

```
AJUDA AÍ 1.0
------------

> Início > Minha área > Minhas respostas

(1) Por causa da compressão do diafragma. O volume de comida no estômag...
    Pergunta: Por que sentimos mais sono depois do almoço?
    Respondido em 20/10/2026 às 00:35 - Nota: 0 (0 avaliações)

(R) Retornar ao menu anterior

Opção: _
```

### 9. Minha área > Minhas avaliações

Lista das respostas que o usuário avaliou.

```
AJUDA AÍ 1.0
------------

> Início > Minha área > Minhas avaliações

(1) Porque o corpo gasta energia para fazer a digestão.
    Sua avaliação: positiva

(R) Retornar ao menu anterior

Opção: _
```

### 10. Minhas avaliações > Avaliação

Mostra a pergunta, a resposta e a avaliação feita. A única opção é cancelar a avaliação, que desfaz o efeito na nota e na contagem.

```
AJUDA AÍ 1.0
------------

> Início > Minha área > Minhas avaliações > Avaliação

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

RESPOSTA
--------

Porque o corpo gasta energia para fazer a digestão.
Respondido por Antônio Carlos Machado em 20/10/2026 às 00:35
Nota: 1  (1 avaliações)

Sua avaliação: positiva (você concordou)

(A) Cancelar avaliação

(R) Retornar ao menu anterior

Opção: _
```

### 11. Minha área > Minhas perguntas > Pergunta

O autor vê as respostas dos outros usuários à sua pergunta e pode alterar ou arquivar a pergunta.

```
AJUDA AÍ 1.0
------------

> Início > Minha área > Minhas perguntas > Pergunta

20/10/2026 00:35
Por que sentimos mais sono depois do almoço?
Palavras chave: sono;saude

RESPOSTAS
---------
(1) Porque o corpo gasta energia para fazer a digestão.
Respondido por Antônio Carlos Machado em 20/10/2026 às 00:35
Nota: 1  (1 avaliações)

(2) Por causa da compressão do diafragma. O volume de comida no estômago
pressiona a base dos pulmões.
Respondido por Ana Marina Reis em 20/10/2026 às 00:35
Nota: 0  (0 avaliações)

(A) Alterar dados da pergunta
(B) Arquivar pergunta

(R) Retornar

Opção: _
```

### 12. Autor da pergunta: resposta de outro usuário

O autor da pergunta pode arquivar uma resposta inadequada.

```
AJUDA AÍ 1.0
------------

> Início > Minha área > Minhas perguntas > Pergunta > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Roberto Souza em 20/10/2026 às 00:35

RESPOSTA
--------

Porque o corpo gasta energia para fazer a digestão.
Respondido por Antônio Carlos Machado em 20/10/2026 às 00:35
Nota: 1  (1 avaliações)

(A) Arquivar resposta

(R) Retornar ao menu anterior

Opção: _
```

## Checklist

1. **Há uma listagem paginada de perguntas, que mostra as perguntas em ordem decrescente de data a partir de uma Árvore B+ acrescentada ao CRUD de Perguntas?**
   **Sim.** `ArquivoPergunta.indiceData` (B+ de `ParLongInt`) e `listarRecentes(pagina)`, 5 perguntas por página, exibidas por `MenuBusca`.

2. **Há uma listagem das respostas a uma pergunta selecionada, em ordem decrescente de nota?**
   **Sim.** `ArquivoResposta.readByPergunta` ordena por nota decrescente e `MenuResposta.imprimirRespostas` exibe a lista.

3. **O usuário pode responder a uma pergunta?**
   **Sim.** Em Buscar perguntas, abre-se a pergunta e escolhe-se "Responder à pergunta" (`MenuResposta.responder`).

4. **O usuário pode arquivar a própria resposta?**
   **Sim.** Pela tela da resposta e por Minha área > Minhas respostas (`MenuResposta.arquivarResposta`).

5. **O usuário pode avaliar uma resposta?**
   **Sim.** Concordar ou discordar (`MenuResposta.avaliar`), uma vez por resposta, e nunca na própria.

6. **Na área do usuário, o usuário pode visualizar as respostas às suas perguntas?**
   **Sim.** Minha área > Minhas perguntas > escolher a pergunta (`MenuPerguntas.abrirPergunta`).

7. **Na área do usuário, o usuário pode arquivar uma resposta de outro usuário a uma das suas perguntas?**
   **Sim.** Na tela da pergunta, escolhe-se a resposta e "Arquivar resposta".

8. **Na área do usuário, o usuário pode visualizar e arquivar as respostas que escreveu?**
   **Sim.** Minha área > Minhas respostas (`MenuResposta.minhasRespostas`).

9. **Na área do usuário, o usuário pode visualizar e arquivar a avaliação que realizou em uma resposta?**
   **Sim, com uma ressalva de nome.** Em Minhas avaliações o usuário vê a pergunta, a resposta e a sua avaliação e pode **cancelá-la**, como pede o enunciado. O cancelamento remove o registro da avaliação (não é um arquivamento lógico) e desfaz o efeito na nota e na contagem da resposta.

10. **A integridade do relacionamento entre perguntas e respostas está mantida em todas as operações?**
    **Sim.** `ArquivoResposta.create` exige usuário existente e pergunta existente e ativa; as duas árvores B+ são atualizadas no `create` e no `delete`; o arquivamento é lógico e não rompe os vínculos; a interface não exclui perguntas, respostas ou usuários fisicamente.

11. **A integridade do relacionamento entre respostas e avaliações está mantida em todas as operações?**
    **Sim.** `ArquivoAvaliacao.create` exige usuário e resposta existentes e ativa, recusa voto repetido e autoavaliação, e ajusta a nota e a contagem da `Resposta`; o `delete` desfaz esse ajuste e remove o registro das duas árvores B+.

12. **O trabalho compila corretamente?**
    **Sim.** O projeto compila com `javac`, sem erros, usando os comandos da seção "Como compilar e executar".

13. **O trabalho está completo e funcionando sem erros de execução?**
    **Sim.** Todas as operações do enunciado (listagem paginada, resposta, avaliação, cancelamento e as telas da Minha área) foram executadas no sistema sem erros de execução.

14. **O trabalho é original e não a cópia de um trabalho de outro grupo?**
    **Sim.** O trabalho foi desenvolvido pelo nosso grupo.

## Vídeo de demonstração

Link do vídeo: *(inserir aqui)*