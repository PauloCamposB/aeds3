# Ajuda Aí 1.0 — TP2 de AEDS3 (Respostas, Avaliações e Árvore B+)

## Participantes

- Enzo Russo
- Henrique Paes
- Otoniel Goulart
- Paulo Campos

## O que o sistema faz

O **Ajuda Aí** é um sistema de perguntas e respostas em modo texto, escrito em Java, que guarda tudo em arquivos binários próprios (sem banco de dados). No TP1 o sistema já permitia criar a conta, entrar, gerenciar os dados do usuário e publicar, alterar e arquivar perguntas.

No **TP2** acrescentamos ao sistema:

- uma **listagem paginada das perguntas** de todos os usuários, da mais recente para a mais antiga, feita a partir de uma **Árvore B+ indexada pela data de criação**;
- a entidade **Resposta**, que liga um usuário a uma pergunta (relacionamento N:N entre usuários e perguntas). Qualquer usuário pode responder a qualquer pergunta ativa;
- a entidade **Avaliação**, que liga um usuário a uma resposta. O usuário pode **concordar** (+1 na nota) ou **discordar** (−1 na nota) de uma resposta de outro usuário, uma única vez por resposta;
- a listagem das respostas de uma pergunta **em ordem decrescente de nota**;
- a **Minha área** ampliada: o usuário vê as respostas às suas perguntas (e pode arquivar as inadequadas), vê e arquiva as respostas que escreveu e vê e cancela as avaliações que fez.

A interface nunca exclui registros fisicamente: perguntas e respostas são **arquivadas** (`ativa = false`) e deixam de aparecer nas listagens; os vínculos com as outras entidades continuam válidos.

## Como compilar e executar

Na raiz do repositório (Linux/macOS):

```
javac -encoding UTF-8 -d build $(find aeds3/src -name "*.java")
java -Dfile.encoding=UTF-8 -cp build visao.MenuAcesso
```

No PowerShell (Windows):

```
$arquivos = Get-ChildItem -Recurse -Path aeds3\src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d build $arquivos
java -Dfile.encoding=UTF-8 -cp build visao.MenuAcesso
```

Os arquivos de dados são criados na pasta `dados/` na primeira execução (`dados/perguntas`, `dados/respostas`, `dados/avaliacoes`).

## Classes do projeto

As classes **criadas ou alteradas no TP2** estão em negrito.

| Pacote | Classe | Papel |
|---|---|---|
| `entidades` | `Usuario`, `Pergunta` | Entidades do TP1 |
| `entidades` | **`Resposta`** | Atributos `idResposta`, `idPergunta`, `idUsuario`, `criacao`, `resposta`, `nota`, `avaliacoes`, `ativa`; serialização em `toByteArray` / `fromByteArray` |
| `entidades` | **`Avaliacao`** | Atributos `idAvaliacao`, `idUsuario`, `idResposta`, `avaliacao` (`true` = concordou, `false` = discordou) |
| `dados` | `ArquivoUsuarios` | CRUD de usuários (TP1), com Hash Extensível pelo e-mail |
| `dados` | **`ArquivoPergunta`** | CRUD de perguntas; no TP2 ganhou a Árvore B+ `indiceData` e os métodos `listarRecentes` e `totalPaginasRecentes` |
| `dados` | **`ArquivoResposta`** | CRUD de respostas + 2 Árvores B+ (por pergunta e por usuário) + arquivamento |
| `dados` | **`ArquivoAvaliacao`** | CRUD de avaliações + 2 Árvores B+ (por resposta e por usuário) + ajuste da nota da resposta |
| `estruturas` | `Arquivo`, `ArvoreBMais`, `HashExtensivel` | CRUD genérico e estruturas de índice |
| `util` | `ParIntInt` | Par de inteiros (chave, id) usado nas árvores de relacionamento 1:N |
| `util` | **`ParLongInt`** | Par (data de criação, idPergunta) da árvore de perguntas recentes, com `compareTo` em ordem **decrescente** de data |
| `visao` | **`MenuAcesso`** | Login, menu principal e Minha área (novas opções *Minhas respostas* e *Minhas avaliações*) |
| `visao` | **`MenuBusca`** | Buscar perguntas > Perguntas mais recentes, paginadas de 5 em 5 |
| `visao` | **`MenuPerguntas`** | Minhas perguntas; ao abrir uma pergunta, mostra as respostas dos outros usuários |
| `visao` | **`MenuResposta`** | Responder, avaliar, arquivar resposta, Minhas respostas e Minhas avaliações |

## Operações especiais implementadas

### 1. Listagem paginada das perguntas por data decrescente (Árvore B+)

`ArquivoPergunta` mantém a Árvore B+ `indiceData`, cujas chaves são `ParLongInt(criacao, idPergunta)`. O `compareTo` de `ParLongInt` compara as datas ao contrário (`Long.compare(o.data, this.data)`), então a própria árvore já guarda as perguntas **da mais nova para a mais antiga**; em caso de mesma data, o desempate é pelo id.

- `ArquivoPergunta.create` insere o par na árvore toda vez que uma pergunta é criada.
- `recentesAtivas()` percorre a árvore em ordem (`indiceData.read(null)`), lê cada pergunta no arquivo principal e descarta as arquivadas.
- `listarRecentes(pagina)` devolve as 5 perguntas da página pedida (`TAMANHO_PAGINA = 5`) e `totalPaginasRecentes()` calcula o número de páginas.
- Se o sistema for aberto com perguntas criadas antes do TP2, o construtor reconstrói o índice (`reconstruirIndiceData`).

A tela fica em `MenuBusca.perguntasRecentes()`: mostra "Página X de Y", as opções **(A) Página anterior** e **(B) Próxima página** e associa o número digitado (1 a 5) ao id real da pergunta.

### 2. Relacionamento entre perguntas e respostas

`ArquivoResposta` mantém duas Árvores B+ de `ParIntInt`:

- `respostas_pergunta.btree.db`: `(idPergunta, idResposta)` — usada por `readByPergunta`, que lista as respostas de uma pergunta;
- `respostas_usuario.btree.db`: `(idUsuario, idResposta)` — usada por `readByUsuario`, que lista as respostas de um usuário (*Minhas respostas*).

`create` verifica se o usuário existe e se a pergunta existe e está ativa antes de gravar; depois insere o par nas duas árvores. `delete` remove das duas árvores. `update` proíbe trocar a pergunta ou o autor da resposta, porque esses campos são as chaves dos índices.

### 3. Respostas em ordem decrescente de nota

`readByPergunta(idPergunta, apenasAtivas)` carrega as respostas pela árvore e ordena a lista por **nota decrescente**; no empate, a resposta mais antiga aparece primeiro. Com `apenasAtivas = true`, as respostas arquivadas são omitidas.

### 4. Escrita de uma resposta

Em `MenuResposta.responder`, o usuário digita **apenas o texto** da resposta (pode ter várias linhas; termina com uma linha vazia) e confirma. Os demais atributos são preenchidos automaticamente: o id é gerado pelo `Arquivo`, `idPergunta` vem da pergunta aberta, `idUsuario` do usuário logado, `criacao` da hora atual, `nota = 0`, `avaliacoes = 0` e `ativa = true`. O texto é limitado a 5000 caracteres.

### 5. Avaliações

`ArquivoAvaliacao` mantém duas Árvores B+: `(idResposta, idAvaliacao)` e `(idUsuario, idAvaliacao)`.

- `create` recusa: usuário ou resposta inexistente, resposta arquivada, avaliar a **própria** resposta e avaliar **a mesma resposta duas vezes** (`jaAvaliou`). Depois de gravar, chama `ajustarResposta`, que soma +1 ou −1 à `nota` e +1 ao contador `avaliacoes` da resposta.
- `delete` (cancelar avaliação) remove o registro das duas árvores e **desfaz** o efeito na resposta (−1 ou +1 na nota e −1 no contador).
- `update` mantém a nota correta se o voto mudar de valor (diferença de ±2). Na interface, para mudar o voto o usuário cancela a avaliação em *Minhas avaliações* e avalia de novo.

### 6. Arquivamento de respostas

`ArquivoResposta.arquivar` faz `ativa = false`. A resposta continua no arquivo e nos índices (por isso as avaliações ligadas a ela não ficam "órfãs"), mas some da listagem da pergunta e de *Minhas respostas*. Podem arquivar uma resposta: o **autor da resposta** e o **autor da pergunta** (respostas inadequadas). O sistema pede confirmação antes.

### 7. Regras de permissão na tela da resposta

`MenuResposta.abrirResposta` decide as opções mostradas:

| Quem abre a resposta | Opções |
|---|---|
| Autor da resposta | (A) Arquivar resposta |
| Autor da pergunta, pela Minha área | (A) Arquivar resposta |
| Outro usuário | (A) Concordar / (B) Discordar |
| Resposta arquivada | Só retornar |

## Telas

Capturas do terminal obtidas executando o sistema com três usuários de teste: **Ana** (autora das perguntas), **Bruno** e **Carla** (que respondem e avaliam).

> No menu *Buscar perguntas*, a opção (B) Busca por palavras-chave foi acrescentada no TP3 e não faz parte deste relatório.

### 1. Menu principal e Minha área

```
> Início

(A) Minha área
(B) Buscar perguntas

(S) Sair
```

```
> Início > Minha área

(A) Meus dados
(B) Minhas perguntas
(C) Minhas respostas
(D) Minhas avaliações

(R) Retornar ao menu anterior
```

### 2. Perguntas mais recentes — página 1

Cinco perguntas por página, da mais recente para a mais antiga. O número digitado abre a pergunta.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Perguntas mais recentes
Página 1 de 2

(1) Por que sentimos mais sono depois do almoço?
    Criada por Ana Souza em 23/11/2026 às 18:40

(2) Como funciona o fuso horário?
    Criada por Ana Souza em 23/11/2026 às 18:40

(3) Por que o café perde o efeito com o uso diário?
    Criada por Ana Souza em 23/11/2026 às 18:40

(4) É necessário fechar os aplicativos em segundo plano?
    Criada por Ana Souza em 23/11/2026 às 18:40

(5) Por que algumas pessoas trabalham melhor ouvindo música?
    Criada por Ana Souza em 23/11/2026 às 18:40

(B) Próxima página

(R) Retornar

Opção: _
```

### 3. Perguntas mais recentes — página 2

A primeira pergunta cadastrada aparece por último.

```
> Início > Buscar perguntas > Perguntas mais recentes
Página 2 de 2

(1) Carregar o celular durante a noite prejudica a bateria?
    Criada por Ana Souza em 23/11/2026 às 18:40

(A) Página anterior

(R) Retornar

Opção: _
```

### 4. Responder à pergunta (Bruno)

O usuário digita só o texto; os outros atributos são automáticos.

```
> Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Responder

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

Resposta (termine com uma linha vazia):
Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.

Confirma o envio da resposta? (S/N): S
Resposta registrada com sucesso!
```

### 5. Pergunta com as respostas, em ordem decrescente de nota

Visão de Bruno depois que Carla também respondeu e concordou com a resposta dele. A resposta de Bruno (nota 1) vem antes da de Carla (nota 0).

```
> Início > Buscar perguntas > Perguntas mais recentes > Pergunta

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

RESPOSTAS
---------
(1) Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.
Respondido por você em 23/11/2026 às 18:40
Nota: 1  (1 avaliações)

(2) Também tem relação com o ritmo circadiano, que tem uma queda natural no início da tarde.
Respondido por Carla Reis em 23/11/2026 às 18:40
Nota: 0  (0 avaliações)

(A) Responder à pergunta

(R) Retornar

Opção: _
```

### 6. Avaliar a resposta de outro usuário (Bruno discorda da resposta de Carla)

```
> Início > Buscar perguntas > Perguntas mais recentes > Pergunta > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

RESPOSTA
--------

Também tem relação com o ritmo circadiano, que tem uma queda natural no início da tarde.
Respondido por Carla Reis em 23/11/2026 às 18:40
Nota: 0  (0 avaliações)

(A) Concordar com a resposta
(B) Discordar da resposta

(R) Retornar ao menu anterior

Opção: B
Você discordou da resposta.
```

Depois da avaliação a nota passa a −1. Uma segunda tentativa é recusada:

```
Nota: -1  (1 avaliações)

(A) Concordar com a resposta
(B) Discordar da resposta

(R) Retornar ao menu anterior

Opção: B
Você já avaliou esta resposta. Para mudar a avaliação, cancele-a em
Minha área > Minhas avaliações e avalie novamente.
```

### 7. Minha área > Minhas perguntas > Pergunta (Ana)

A autora vê as respostas dos outros usuários à sua pergunta, ordenadas por nota.

```
> Início > Minha área > Minhas perguntas > Pergunta

23/11/2026 18:40
Por que sentimos mais sono depois do almoço?
Palavras chave: teste;duvida

RESPOSTAS
---------
(1) Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.
Respondido por Bruno Lima em 23/11/2026 às 18:40
Nota: 2  (2 avaliações)

(2) Também tem relação com o ritmo circadiano, que tem uma queda natural no início da tarde.
Respondido por Carla Reis em 23/11/2026 às 18:40
Nota: -1  (1 avaliações)

(A) Alterar dados da pergunta
(B) Arquivar pergunta

(R) Retornar

Opção: _
```

### 8. Autora da pergunta arquiva a resposta de outro usuário

```
> Início > Minha área > Minhas perguntas > Pergunta > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

RESPOSTA
--------

Também tem relação com o ritmo circadiano, que tem uma queda natural no início da tarde.
Respondido por Carla Reis em 23/11/2026 às 18:40
Nota: -1  (1 avaliações)

(A) Arquivar resposta

(R) Retornar ao menu anterior

Opção: A
ATENÇÃO: o arquivamento é definitivo e não pode ser desfeito.
Confirma o arquivamento da resposta? (S/N): S
Resposta arquivada com sucesso!
```

Ao voltar para a pergunta, só a resposta de Bruno continua na lista:

```
RESPOSTAS
---------
(1) Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.
Respondido por Bruno Lima em 23/11/2026 às 18:40
Nota: 2  (2 avaliações)
```

### 9. Minha área > Minhas respostas (Bruno)

```
> Início > Minha área > Minhas respostas

(1) Por causa da digestão: o corpo direciona mais sangue para o estômag...
    Pergunta: Por que sentimos mais sono depois do almoço?
    Respondido em 23/11/2026 às 18:40 - Nota: 1 (1 avaliações)

(R) Retornar ao menu anterior

Opção: _
```

Ao escolher a resposta, o usuário vê a pergunta e a resposta completa, com a opção de arquivá-la:

```
> Início > Minha área > Minhas respostas > Resposta

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

RESPOSTA
--------

Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.
Respondido por você em 23/11/2026 às 18:40
Nota: 1  (1 avaliações)

(A) Arquivar resposta

(R) Retornar ao menu anterior

Opção: _
```

Para Carla, cuja resposta foi arquivada pela autora da pergunta, a lista fica vazia:

```
> Início > Minha área > Minhas respostas

Você ainda não possui respostas ativas.
```

### 10. Minha área > Minhas avaliações e cancelamento (Carla)

```
> Início > Minha área > Minhas avaliações

(1) Por causa da digestão: o corpo direciona mais sangue para o estômag...
    Sua avaliação: positiva

(R) Retornar ao menu anterior

Opção: 1
```

```
> Início > Minha área > Minhas avaliações > Avaliação

Por que sentimos mais sono depois do almoço?
Criada por Ana Souza em 23/11/2026 às 18:40

RESPOSTA
--------

Por causa da digestão: o corpo direciona mais sangue para o estômago
e a glicemia sobe depois da refeição.
Respondido por Bruno Lima em 23/11/2026 às 18:40
Nota: 2  (2 avaliações)

Sua avaliação: positiva (você concordou)

(A) Cancelar avaliação

(R) Retornar ao menu anterior

Opção: A
Confirma o cancelamento da avaliação? (S/N): S
Avaliação cancelada. A nota da resposta foi ajustada.
```

Depois do cancelamento, a resposta de Bruno volta de `Nota: 2 (2 avaliações)` para `Nota: 1 (1 avaliações)`.

### 11. Avaliação de uma resposta arquivada (Bruno)

A avaliação continua ligada à resposta mesmo depois do arquivamento, e a tela indica que a resposta foi arquivada.

```
> Início > Minha área > Minhas avaliações

(1) Também tem relação com o ritmo circadiano, que tem uma queda natura... [ARQUIVADA]
    Sua avaliação: negativa
```

## Checklist

1. **Há uma listagem paginada de perguntas, que mostra as perguntas em ordem decrescente de data a partir de uma Árvore B+ acrescentada ao CRUD de Perguntas?**
   **Sim.** A Árvore B+ `indiceData` (chave `ParLongInt(criacao, idPergunta)`, ordenada por data decrescente) foi acrescentada a `ArquivoPergunta`. `listarRecentes(pagina)` devolve 5 perguntas ativas por página e `MenuBusca.perguntasRecentes()` permite navegar entre as páginas (telas 2 e 3).

2. **Há uma listagem das respostas a uma pergunta selecionada, em ordem decrescente de nota?**
   **Sim.** `ArquivoResposta.readByPergunta` busca as respostas pela Árvore B+ `(idPergunta, idResposta)` e as ordena por nota decrescente (empate: a mais antiga primeiro). Telas 5 e 7.

3. **O usuário pode responder a uma pergunta?**
   **Sim.** Opção (A) Responder à pergunta, na tela da pergunta (`MenuResposta.responder`). Só o texto é digitado; os outros campos são automáticos. Tela 4.

4. **O usuário pode arquivar a própria resposta?**
   **Sim.** Ao abrir uma resposta própria (pela busca ou por *Minhas respostas*), a única opção é (A) Arquivar resposta (`ArquivoResposta.arquivar`). Tela 9.

5. **O usuário pode avaliar uma resposta?**
   **Sim.** Em respostas de outros usuários aparecem (A) Concordar (+1) e (B) Discordar (−1). O sistema impede avaliar a própria resposta e avaliar a mesma resposta duas vezes. Tela 6.

6. **Na área do usuário, o usuário pode visualizar as respostas às suas perguntas?**
   **Sim.** Em Minha área > Minhas perguntas > (A) Listar, ao abrir uma pergunta aparecem as respostas dos outros usuários, ordenadas por nota (`MenuPerguntas.abrirPergunta`). Tela 7.

7. **Na área do usuário, o usuário pode arquivar uma resposta de outro usuário a uma das suas perguntas?**
   **Sim.** Ao abrir a resposta pela Minha área, o autor da pergunta tem a opção (A) Arquivar resposta (`abrirResposta(..., autorDaPergunta = true)`). Tela 8.

8. **Na área do usuário, o usuário pode visualizar e arquivar as respostas que escreveu?**
   **Sim.** Minha área > (C) Minhas respostas lista as respostas ativas do usuário pela Árvore B+ `(idUsuario, idResposta)`; ao escolher uma, ele vê a pergunta e a resposta e pode arquivá-la (`MenuResposta.minhasRespostas`). Tela 9.

9. **Na área do usuário, o usuário pode visualizar e arquivar a avaliação que realizou em uma resposta?**
   **Sim.** Minha área > (D) Minhas avaliações lista as avaliações do usuário pela Árvore B+ `(idUsuario, idAvaliacao)`; ao escolher uma, ele vê a pergunta, a resposta e o voto, e pode **cancelá-la** (`ArquivoAvaliacao.delete`), o que desfaz o efeito na nota e na contagem. Tela 10.

10. **A integridade do relacionamento entre perguntas e respostas está mantida em todas as operações?**
    **Sim.** Uma resposta só é criada se o usuário e a pergunta existirem e a pergunta estiver ativa; uma pergunta arquivada não aceita novas respostas e sai da listagem. As duas Árvores B+ são atualizadas no `create` e no `delete`, e o `update` não deixa trocar a pergunta nem o autor da resposta (campos usados nos índices). Como a interface só arquiva (nunca exclui fisicamente), nenhuma resposta fica apontando para uma pergunta inexistente.

11. **A integridade do relacionamento entre respostas e avaliações está mantida em todas as operações?**
    **Sim.** Uma avaliação só é criada se o usuário e a resposta existirem, se a resposta estiver ativa, se não for do próprio usuário e se ele ainda não a tiver avaliado. Os campos `nota` e `avaliacoes` da resposta são ajustados em todas as operações: `create` (+1/−1 e +1), `update` (±2 quando o voto muda) e `delete` (desfaz o voto). As avaliações de uma resposta arquivada continuam válidas e aparecem marcadas como `[ARQUIVADA]` (tela 11). No teste, a nota da resposta de Bruno foi de 0 → 1 → 2 com duas avaliações positivas e voltou a 1 quando uma delas foi cancelada.

12. **O trabalho compila corretamente?**
    **Sim.** O projeto compila com `javac` sem erros, com os comandos da seção "Como compilar e executar".

13. **O trabalho está completo e funcionando sem erros de execução?**
    **Sim.** Todas as operações acima foram executadas com três usuários de teste (criação de perguntas, paginação, respostas, avaliações, voto repetido, arquivamento pelo autor da resposta e pelo autor da pergunta, cancelamento de avaliação) sem erros de execução.

14. **O trabalho é original e não a cópia de um trabalho de outro grupo?**
    **Sim.** O trabalho foi desenvolvido pelo nosso grupo. As classes `Arquivo`, `ArvoreBMais` e `HashExtensivel` são as estruturas usadas em aula.

## Vídeo de demonstração

Link do vídeo: **https://youtu.be/6n8bSLJymEI?is=qFZHbTeYtllLj104**
