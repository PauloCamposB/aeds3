# Ajuda Aí 1.0 — TP3 de AEDS3 (índice invertido e busca TFxIDF)

## Participantes

Enzo Russo, Henrique Paes, Otoniel Goulart, Paulo Campos

## O que o sistema faz

O **Ajuda Aí** é um sistema de perguntas e respostas em modo texto (Java). Cada usuário pode:

- criar a conta, entrar no sistema e gerenciar seus dados (TP1);
- publicar, alterar e arquivar perguntas (TP1);
- **buscar as perguntas mais recentes**, em páginas de 5, da mais nova para a mais antiga;
- **buscar perguntas por palavras-chave** (TP3): o usuário digita um ou mais termos e recebe as perguntas ordenadas pela relevância **TFxIDF**, em páginas de 5;
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
| `util` | `ProcessadorTexto` | TP3: transforma texto em termos (minúsculas, sem acentos, sem stop words) e calcula o TF |
| `util` | `ResultadoBusca` | TP3: par (id da pergunta, score TFxIDF); ordem natural = score decrescente |
| `estruturas` | `ListaInvertida`, `ElementoLista` | TP3: listas invertidas fornecidas pelo professor (código não alterado) |
| `dados` | `IndiceInvertido` | TP3: índice invertido das palavras-chave, construído sobre a `ListaInvertida` |
| `dados` | `BuscaPerguntas` | TP3: busca por termos com ranking TFxIDF |
| `visao` | `MenuAcesso` | Login, menu principal e Minha área |
| `visao` | `MenuBusca` | Buscar perguntas: perguntas mais recentes (A) e busca por palavras-chave com TFxIDF (B), paginadas |
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

**7. Índice invertido das palavras-chave (TP3).**
`IndiceInvertido` usa a classe `ListaInvertida` do professor, sem alterações. As palavras-chave de cada pergunta passam por `ProcessadorTexto.extrairTermos`: o texto é separado em palavras (`;` e espaços separam termos, então "inteligência artificial" são 2 termos), convertido para minúsculas, sem acentos, e as *stop words* e os numerais são descartados (a lista de stop words foi adaptada ao domínio, incluindo "como", "qual", "pergunta", "dúvida"). O TF de cada termo é `ocorrências / total de termos válidos` da pergunta (`ProcessadorTexto.calcularTF`). Em `IndiceInvertido.indexar`, para cada termo usamos exatamente a lógica do enunciado: `read(termo, id)`; se `null`, `create`; senão `update` somando a frequência. A cada pergunta indexada chamamos `incrementaEntidades()`, e esse contador é o N do IDF.

**8. Índice sempre consistente com as perguntas (TP3).**
- `ArquivoPergunta.create` indexa a pergunta nova.
- `ArquivoPergunta.update` compara as palavras-chave antigas e novas: os termos que saíram são removidos da lista (`delete`), os novos são inseridos e o TF dos que ficam é atualizado (`IndiceInvertido.atualizar`); o N não muda. Por isso uma busca feita antes e depois de alterar as palavras-chave dá resultados diferentes.
- `ArquivoPergunta.arquivar` retira a pergunta de todos os termos (`desindexar`) e chama `decrementaEntidades()`; se uma pergunta arquivada for reativada, ela é indexada de novo.
- Se o índice estiver vazio ao abrir o sistema (perguntas criadas antes do TP3), `reconstruirIndicePalavras` indexa todas as perguntas ativas já gravadas.

**9. Busca com ranking TFxIDF (TP3).**
`BuscaPerguntas.ranquear(consulta)` trata a consulta com o mesmo `ProcessadorTexto` e remove termos repetidos. Para cada termo, lê a lista invertida (`IndiceInvertido.lista`), calcula `IDF = log10(N / tamanho da lista do termo)` e, para cada pergunta da lista, `score += TF x IDF`. Os scores de um mesmo id são somados e o resultado é ordenado por score decrescente (`ResultadoBusca.compareTo`; empate: menor id primeiro). `BuscaPerguntas.buscar` aplica o ranking e mantém só as perguntas **ativas**. Verificação com os 4 livros do enunciado: a busca "Inteligência Artificial" retorna `(1; 0.142), (3; 0.110), (2; 0.042)`, na ordem 1, 3, 2.

**10. Tela de busca por palavras-chave (TP3).**
`MenuBusca`, opção (B): o usuário digita os termos, vê os resultados de 5 em 5 (com as palavras-chave e a relevância de cada pergunta), pode navegar entre as páginas, abrir uma pergunta para ver e dar respostas, ou fazer uma nova busca.

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

### 13. Busca por palavras-chave com mais de um termo (TP3)

Busca "sono saúde" em 7 perguntas cadastradas (N = 7). "sono" aparece em 4 perguntas (IDF = log(7/4) = 0.243) e "saúde" em 3 (IDF = log(7/3) = 0.368). A primeira pergunta tem os dois termos como únicas palavras-chave (TF = 0.5 cada): 0.5 x 0.243 + 0.5 x 0.368 = 0.306.

```
AJUDA AÍ 1.0
------------

> Início > Buscar perguntas > Busca por palavras-chave > Resultados
Busca: "sono saúde" - 4 resultado(s)
Página 1 de 1

(1) Por que sentimos sono depois do almoço?
    Palavras chave: sono;saúde
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.306

(2) O café perde o efeito com o uso diário?
    Palavras chave: sono;café;saúde
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.204

(3) Cochilo depois do almoço faz bem?
    Palavras chave: sono almoço;saúde
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.204

(4) Dormir pouco atrapalha a digestão?
    Palavras chave: sono;digestão
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.122

(N) Nova busca

(R) Retornar

Opção: _
```

### 14. Busca antes e depois de alterar as palavras-chave (TP3)

Busca "bateria" antes de alterar: só a pergunta sobre o celular tem esse termo.

```
Busca: "bateria" - 1 resultado(s)

(1) Carregar o celular à noite prejudica a bateria?
    Palavras chave: bateria;celular
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.423
```

Em Minha área, as palavras-chave de "Por que sentimos sono depois do almoço?" são alteradas de `sono;saúde` para `bateria;celular`. A mesma busca agora encontra as duas perguntas, e uma busca por "sono" não retorna mais essa pergunta.

```
Busca: "bateria" - 2 resultado(s)

(1) Por que sentimos sono depois do almoço?
    Palavras chave: bateria;celular
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.272

(2) Carregar o celular à noite prejudica a bateria?
    Palavras chave: bateria;celular
    Criada por Roberto Souza em 08/11/2026 às 19:26 | relevância (TFxIDF): 0.272
```

### 15. Busca sem resultados e resultados paginados (TP3)

Termos inexistentes ou formados só por stop words não retornam nada. Quando há mais de 5 resultados, eles são divididos em páginas.

```
Digite os termos de busca (ENTER para retornar): xyz

Nenhuma pergunta encontrada para "xyz".
(termos muito comuns, como artigos e preposições, são ignorados)
```

```
> Início > Buscar perguntas > Busca por palavras-chave > Resultados
Busca: "teste" - 7 resultado(s)
Página 1 de 2

(1) Pergunta de teste 1
    Palavras chave: teste
    ...

(B) Próxima página
(N) Nova busca

(R) Retornar
```

### 16. Por que o ranking põe uma pergunta na frente da outra (TP3)

Busca "O maior time de futebol?". Os termos "o" e "de" são stop words e saem; "time" não está nas palavras-chave de nenhuma pergunta e não pontua. Pontuam "futebol" (IDF = log(7/3) = 0.368) e "maior" (IDF = log(7/2) = 0.544). As duas primeiras perguntas têm os mesmos termos e, portanto, o mesmo IDF; a diferença vem do TF: na primeira, cada termo vale 1/4 = 0.25 (4 palavras-chave) e, na segunda, 1/5 = 0.20 (5 palavras-chave, pois "real madrid" conta como 2 termos). Assim: 0.25 x (0.368 + 0.544) = 0.228 e 0.20 x (0.368 + 0.544) = 0.182. A terceira tem só "futebol": 0.25 x 0.368 = 0.092.

```
> Início > Buscar perguntas > Busca por palavras-chave > Resultados
Busca: "O maior time de futebol?" - 3 resultado(s)
Página 1 de 1

(1) qual o maior time de minas?
    Palavras chave: galo;minas;futebol;maior
    Criada por paulo em 08/11/2026 às 16:57 | relevância (TFxIDF): 0,228

(2) Qual o maior time do mundo?
    Palavras chave: real madrid;futebol;maior;espanha
    Criada por paulo em 08/11/2026 às 17:16 | relevância (TFxIDF): 0,182

(3) Qual o ultimo pais campeao da copa do mundo?
    Palavras chave: espanha;futebol;copa do mundo
    Criada por paulo em 08/11/2026 às 17:17 | relevância (TFxIDF): 0,092
```

## Checklist

1. **O índice invertido com palavras-chave foi criado usando a classe ListaInvertida?**
   **Sim.** `IndiceInvertido` cria e consulta as listas por meio da `ListaInvertida` do professor (`create`, `read(termo, id)`, `update`, `delete`, `incrementaEntidades`, `decrementaEntidades`, `numeroEntidades`), com os termos extraídos das palavras-chave das perguntas por `ProcessadorTexto`.

2. **É possível buscar perguntas por palavras-chave no menu de buscar perguntas?**
   **Sim.** Em Buscar perguntas > (B) Busca por palavras-chave (`MenuBusca.buscaPorPalavrasChave`), aceitando um ou mais termos. O índice é mantido atualizado quando a pergunta é criada, alterada ou arquivada (`ArquivoPergunta.create`, `update` e `arquivar`).

3. **As perguntas retornadas estão corretamente classificadas pelo modelo TFxIDF?**
   **Sim.** `BuscaPerguntas.ranquear` soma TF x IDF (IDF = log10(N / tamanho da lista)) por pergunta e ordena do maior para o menor score. Com os 4 livros do enunciado, a busca "Inteligência Artificial" retorna `(1; 0.142), (3; 0.110), (2; 0.042)`, na ordem 1, 3, 2, igual ao exemplo do enunciado.

4. **O trabalho compila corretamente?**
   **Sim.** O projeto compila com `javac`, sem erros, usando os comandos da seção "Como compilar e executar".

5. **O trabalho está completo e funcionando sem erros de execução?**
   **Sim.** Foram executadas, sem erros de execução, a busca com mais de um termo, a busca antes e depois de alterar as palavras-chave, a busca depois de arquivar uma pergunta, a busca sem resultados e a paginação dos resultados.

6. **O trabalho é original e não a cópia de um trabalho de outro grupo?**
   **Sim.** O trabalho foi desenvolvido pelo nosso grupo.

## Vídeo de demonstração

Link do vídeo: **https://youtu.be/uexELklcj94**

O vídeo (até 5 minutos, com narração) mostra:

1. a busca de perguntas por mais de uma palavra-chave, com o ranking por relevância;
2. a busca de uma pergunta antes e depois de alterar as palavras-chave dela;
3. o código responsável pelo ranking (`BuscaPerguntas.ranquear`), com o cálculo de TF x IDF, e o `IndiceInvertido.indexar`, que usa a `ListaInvertida`.