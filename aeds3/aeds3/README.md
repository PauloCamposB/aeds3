# Trabalho Prático 1 (AEDs III)

## Participantes

- Enzo Russo
- Henrique Paes
- Otoniel Goulart
- Paulo Campos

---

## 1. O que o sistema faz

O **AJUDA AÍ 1.0** é uma versão simplificada do StackOverflow, feita em Java com interface de texto (console). Todos os dados são gravados em **arquivos binários próprios**, sem banco de dados, usando o CRUD genérico (`Arquivo`), a Tabela Hash Extensível (`HashExtensivel`) e a Árvore B+ (`ArvoreBMais`) fornecidos em aula.

Neste primeiro trabalho o sistema permite:

- **Cadastrar usuários** (primeiro acesso), com e-mail único, nome, senha, pergunta secreta e resposta secreta.
- **Fazer login** com e-mail e senha, validados juntos.
- **Recuperar a senha** por meio da pergunta secreta.
- **Alterar os dados do usuário** (nome, e-mail, senha, pergunta e resposta secretas).
- **Gerenciar as próprias perguntas**: listar, incluir, alterar e arquivar. Cada pergunta pertence a exatamente um usuário.

As opções "Buscar perguntas", "Minhas respostas" e "Meus votos" aparecem nos menus, mas ficam para as próximas etapas do projeto, como previsto no enunciado.

## 2. Como compilar e executar

Requer JDK 17 ou superior (compilado e testado com OpenJDK 21). A partir da **raiz do repositório**:

```bash
# Linux / macOS / Git Bash
javac -encoding UTF-8 -d bin $(find aeds3/src -name "*.java")
java -cp bin visao.Main
```

```powershell
# Windows PowerShell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse aeds3/src -Filter *.java).FullName
java -cp bin visao.Main
```

A pasta `dados/` (arquivos e índices) é criada automaticamente no diretório de onde o programa é executado.

## 3. Classes criadas

| Pacote | Classe | Função |
|---|---|---|
| `entidades` | `Usuario` | Entidade usuário (id, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta) com `toByteArray`/`fromByteArray`. |
| `entidades` | `Pergunta` | Entidade pergunta (idPergunta, idUsuario, criacao, alteracao, nota, pergunta, palavrasChave, ativa) com serialização. |
| `dados` | `ArquivoUsuarios` | CRUD de usuários. **Estende `Arquivo<Usuario>`** e acrescenta o índice indireto por e-mail (Hash Extensível). |
| `dados` | `ArquivoPergunta` | CRUD de perguntas. **Estende `Arquivo<Pergunta>`** e acrescenta a Árvore B+ do relacionamento 1:N. |
| `util` | `ParEmailID` | Par (e-mail, id) guardado na Hash Extensível. A chave é o hash do e-mail em minúsculas. |
| `util` | `ParIntInt` | Par (idUsuario, idPergunta) guardado na Árvore B+. |
| `util` | `Seguranca` | Hash SHA-256 e normalização de texto (sem acentos, minúsculas). |
| `visao` | `Main` | Ponto de entrada. |
| `visao` | `MenuAcesso` | Visão e controle de usuários: acesso, novo usuário, recuperação de senha, menu principal, "Minha área" e "Meus dados". |
| `visao` | `MenuPerguntas` | Visão e controle de perguntas: listar, incluir, alterar e arquivar. |
| `estruturas` | `Arquivo`, `HashExtensivel`, `ArvoreBMais`, `Registro*`, `ParIDEndereco` | Código base fornecido em aula (CRUD genérico e índices). `ParIDEndereco` é o par (id, endereço) do índice direto. |

## 4. Como os dados são armazenados

O CRUD genérico grava cada registro como **lápide** (1 byte) + **tamanho** (short) + **vetor de bytes**. O cabeçalho do arquivo guarda o último ID usado e a lista de registros excluídos (o espaço é reaproveitado).

| Arquivo em `dados/` | Estrutura | Conteúdo |
|---|---|---|
| `usuarios.db/usuarios.db.db` | Arquivo de registros | Usuários |
| `usuarios.db/usuarios.db.d.db` e `.c.db` | Hash Extensível (índice **direto**) | id do usuário → endereço no arquivo |
| `usuarios_email.hash.db` e `usuarios_email.cesto.db` | Hash Extensível (índice **indireto**) | e-mail → id do usuário |
| `perguntas/perguntas.db` | Arquivo de registros | Perguntas |
| `perguntas/perguntas.d.db` e `.c.db` | Hash Extensível (índice **direto**) | id da pergunta → endereço no arquivo |
| `perguntas/perguntas_usuario.btree.db` | Árvore B+ (ordem 5) | pares (idUsuario, idPergunta): relacionamento **1:N** |

## 5. Operações especiais implementadas

**Senha e resposta secreta não são guardadas.** `Seguranca.hashSHA256` gera o hash SHA-256 e só ele é gravado (`hashSenha` e `hashRespostaSecreta`).

**Normalização da resposta secreta.** Antes de gerar o hash, `Seguranca.normalizar` remove acentos, coloca tudo em minúsculas e tira espaços das pontas. Assim, "Rex", "  REX " e "rex" valem a mesma resposta (isso é usado no cadastro, na recuperação de senha e na alteração da pergunta secreta).

**E-mail único e login por índice.** `ArquivoUsuarios.create` consulta a Hash Extensível antes de gravar e lança exceção se o e-mail já existir. O `ParEmailID.hash` normaliza o e-mail (trim + minúsculas), então `Maria@Email.com` e `maria@email.com` são o mesmo e-mail. O login (`autenticar`) busca o usuário pelo e-mail no índice e compara o hash da senha.

**Alteração de e-mail** (`ArquivoUsuarios.atualizarEmail`). Como o e-mail é a chave do índice, alterá-lo exige atualizar o índice além do registro:

```java
public boolean atualizarEmail(Usuario u, String emailAntigo, String novoEmail) throws Exception {
    // 1) verifica se o novo e-mail já pertence a OUTRO usuário
    ParEmailID existe = indiceEmail.read(ParEmailID.hash(novoEmail));
    if (existe != null && existe.getId() != -1 && existe.getId() != u.getId()) {
        throw new Exception("O novo e-mail ja esta em uso por outro usuario!");
    }
    // 2) remove a chave antiga da Tabela Hash
    indiceEmail.delete(ParEmailID.hash(emailAntigo));
    // 3) atualiza o registro no arquivo principal
    u.setEmail(novoEmail);
    boolean ok = super.update(u);
    // 4) insere a nova chave na Tabela Hash
    if (ok) indiceEmail.create(new ParEmailID(novoEmail, u.getId()));
    return ok;
}
```

Depois da troca, o e-mail antigo deixa de funcionar no login e o novo passa a funcionar (ver tela na seção 6).

**Relacionamento 1:N com Árvore B+.** A pergunta guarda o `idUsuario` (chave estrangeira). Para o caminho inverso (quais perguntas são de um usuário) usamos a Árvore B+ com o par `(idUsuario, idPergunta)`:

- `ArquivoPergunta.create` grava a pergunta no arquivo e insere o par na árvore.
- `ParIntInt.compareTo` ordena primeiro por `idUsuario` e depois por `idPergunta`. Um `idPergunta = -1` funciona como **coringa**, então `new ParIntInt(idUsuario, -1)` recupera todas as perguntas daquele usuário sem percorrer o arquivo inteiro.
- `ArquivoPergunta.readByUsuario` faz essa busca na árvore e lê cada pergunta pelo índice direto.

**Integridade referencial.**
- `ArquivoPergunta.create` recusa a pergunta se o `idUsuario` não existir no arquivo de usuários.
- `ArquivoPergunta.excluirUsuario(idUsuario)` faz a **exclusão em cascata**: apaga todas as perguntas do usuário (arquivo e Árvore B+) e depois o usuário, e `ArquivoUsuarios.delete` também remove o e-mail do índice. Esse método foi testado, mas **não há opção de excluir conta nos menus** (o enunciado não pede essa tela).

**Listagem numerada.** `MenuPerguntas.mostrarLista` mostra as perguntas com número sequencial (1, 2, 3…), sem exibir IDs, e guarda um vetor `idsListados` que associa o número da tela ao ID real. Perguntas arquivadas aparecem marcadas com `ARQUIVADA`.

**Alteração** (`MenuPerguntas.alterar`). O usuário escolhe o número, e o código converte para o ID real pelo vetor. Perguntas arquivadas não podem ser alteradas. Os setters de `Pergunta` atualizam o campo `alteracao` automaticamente.

**Arquivamento** (`MenuPerguntas.arquivar` + `ArquivoPergunta.arquivar`). Perguntas não são excluídas: apenas o atributo `ativa` muda para `false`, e é definitivo (não existe desarquivar):

```java
// MenuPerguntas: converte o número digitado no ID real usando o vetor de associação
int idPergunta = idsListados.get(numero - 1);
Pergunta p = arqPerguntas.read(idPergunta);
// (confere que a pergunta é do usuário logado, pede confirmação e chama:)
arqPerguntas.arquivar(p.getId());

// ArquivoPergunta
public boolean arquivar(int idPergunta) throws Exception {
    Pergunta p = super.read(idPergunta);
    if (p != null && p.isAtiva()) {
        p.setAtiva(false);
        p.setAlteracao(System.currentTimeMillis());
        return super.update(p);
    }
    return false;
}
```

**Outras regras.** Cada usuário só vê e altera as próprias perguntas. O texto da pergunta aceita várias linhas (termina com linha vazia) e é limitado a 5000 caracteres para não estourar o tamanho de registro do CRUD. Nota inicial 0 e datas de criação/alteração vêm do relógio do computador.

## 6. Telas do sistema

Capturas reais da execução (o que o usuário digita aparece depois de cada prompt).

**Novo usuário, login falho e recuperação de senha**

```text
-----------------------------
AJUDA AÍ 1.0
-----------------------------
1) Acesso ao Sistema (Login)
2) Novo Usuário (Primeiro Acesso)
S) Sair
Opção: 2

--- NOVO USUÁRIO ---
Email: maria@email.com
Nome: Maria Silva
Senha: senha123
Pergunta Secreta (para recuperação): Qual o nome do seu primeiro pet?
Resposta Secreta: Rex

Usuário cadastrado com sucesso (ID 1)! Faça o primeiro acesso.

Opção: 1

--- ACESSO AO SISTEMA ---
Email: maria@email.com
Senha: errada

E-mail ou senha incorretos.
Deseja tentar recuperar a senha? (S/N): S

--- RECUPERAÇÃO DE SENHA ---
Pergunta Secreta: Qual o nome do seu primeiro pet?
Sua Resposta:   REX 
Resposta correta! Digite a nova senha: novasenha
Senha redefinida com sucesso! Faça login novamente.
```

**Login correto e menus**

```text
--- ACESSO AO SISTEMA ---
Email: maria@email.com
Senha: novasenha

Login realizado com sucesso! Bem-vindo(a), Maria Silva.

AJUDA AÍ 1.0
> Inicio
(A) Minha área
(B) Buscar perguntas
(S) Sair
```

**Alteração de e-mail** (o e-mail antigo deixa de funcionar e o novo funciona, mesmo após reiniciar o programa)

```text
> Inicio > Minha área > Meus dados
Opção: B
Novo E-mail: maria.silva@email.com
E-mail e índice de busca atualizados com sucesso!

--- ACESSO AO SISTEMA ---            (nova execução do programa)
Email: maria@email.com
Senha: novasenha
E-mail ou senha incorretos.

--- ACESSO AO SISTEMA ---
Email: maria.silva@email.com
Senha: novasenha
Login realizado com sucesso! Bem-vindo(a), Maria Silva.
```

**Menu de perguntas e inclusão**

```text
> Início > Minha área > Minhas perguntas

(A) Listar
(B) Incluir
(C) Alterar
(D) Arquivar

(R) Retornar ao menu anterior

Opção: B

NOVA PERGUNTA
Pergunta (termine com uma linha vazia): 
É seguro comer pão mofado se você cortar a parte mofada fora?

Palavras chave (separadas por ponto-e-vírgula): pão;mofado;saúde
Confirma a inclusão? (S/N): S
Pergunta incluída com sucesso!
```

**Alteração de pergunta**

```text
Número da pergunta que deseja alterar (0 para cancelar): 2

Pergunta atual: Para quem está começando a programar, qual é a linguagem recomendada?
Nova pergunta (linha vazia para finalizar; ENTER direto mantém a atual): 

Palavras chave atuais: programação;linguagem
Novas palavras chave (ENTER mantém as atuais): programação;linguagem;iniciante
Confirma a alteração? (S/N): S
Pergunta alterada com sucesso!
```

**Arquivamento e listagem final**

```text
Número da pergunta que deseja arquivar (0 para cancelar): 3

Pergunta: Por que a luz azul das telas atrapalha o nosso sono?
ATENÇÃO: o arquivamento é definitivo e não pode ser desfeito.
Confirma o arquivamento? (S/N): S
Pergunta arquivada com sucesso!

MINHAS PERGUNTAS

(1) 
20/09/2026 17:00
É seguro comer pão mofado se você cortar a parte mofada fora?
Palavras chave: pão;mofado;saúde

(2) 
20/09/2026 17:00
Para quem está começando a programar, qual é a linguagem recomendada?
Palavras chave: programação;linguagem;iniciante

(3) ARQUIVADA
20/09/2026 17:00
Por que a luz azul das telas atrapalha o nosso sono?
Palavras chave: luz azul;sono
```

## 7. Checklist

- **Há um CRUD de usuários (que estende a classe Arquivo, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente?**
  **Sim.** `ArquivoUsuarios` estende `Arquivo<Usuario>`. O índice direto (id → endereço) é a Hash Extensível herdada de `Arquivo`, e acrescentamos um índice indireto por e-mail, também em Hash Extensível. Create, read (por id e por e-mail), update (nome, e-mail, senha, pergunta/resposta secretas) e delete (que também remove o e-mail do índice) foram testados. Não usamos Árvore B+ em usuários porque não há relacionamento que precise dela nessa entidade. O `delete` de usuário não está ligado a nenhuma tela, pois o enunciado não pede exclusão de conta.

- **Há um CRUD de perguntas (que estende a classe Arquivo, acrescentando Tabelas Hash Extensíveis e Árvores B+ como índices diretos e indiretos conforme necessidade) que funciona corretamente?**
  **Sim.** `ArquivoPergunta` estende `Arquivo<Pergunta>`, com índice direto em Hash Extensível (herdado) e a Árvore B+ como índice indireto por usuário. Incluir, listar, alterar e arquivar funcionam pelo menu. No lugar da exclusão física, o enunciado pede o arquivamento (`ativa = false`), que está implementado.

- **As perguntas estão vinculadas aos usuários usando o idUsuario como chave estrangeira?**
  **Sim.** `Pergunta` tem o atributo `idUsuario`, preenchido automaticamente com o usuário logado. `ArquivoPergunta.create` recusa perguntas cujo usuário não exista, e `excluirUsuario` remove as perguntas do usuário em cascata.

- **Há uma árvore B+ que registre o relacionamento 1:N entre usuários e perguntas?**
  **Sim.** O arquivo `perguntas/perguntas_usuario.btree.db` é uma `ArvoreBMais<ParIntInt>` (ordem 5) com o par (idUsuario, idPergunta). Ela é alimentada em `create` e consultada em `readByUsuario` para a listagem, alteração e arquivamento. Testamos com mais perguntas que a capacidade de uma página (7 perguntas de um usuário, ordem 5), e a busca retornou todas.

- **O trabalho compila corretamente?**
  **Sim.** Compila sem erros com JDK 21 usando os comandos da seção 2.

- **O trabalho está completo e funcionando sem erros de execução?**
  **Sim, para o escopo do TP1** (usuários e perguntas). Executamos todo o fluxo pelo console (cadastro, login falho, recuperação de senha, login, troca de e-mail, incluir, listar, alterar e arquivar) e reabrimos o programa para confirmar que os dados persistem. Limitações conhecidas: "Buscar perguntas", "Minhas respostas" e "Meus votos" estão previstos para as próximas etapas e mostram apenas uma mensagem. Se o login falhar, o sistema oferece a recuperação de senha; respondendo "N", o usuário volta ao menu de acesso e pode tentar de novo.

- **O trabalho é original e não a cópia de um trabalho de outro grupo?**
  **Sim.** O código foi desenvolvido pelo nosso grupo, a partir das classes base fornecidas em aula (`Arquivo`, `HashExtensivel`, `ArvoreBMais`).

## 8. Vídeo de demonstração

Link do vídeo (até 5 minutos): https://youtu.be/o-s2ZxtJkSA

O vídeo mostra: cadastro de usuário; login falhando e recuperação de senha; login correto; atualização de e-mail (com explicação do código); cadastro, listagem e atualização de pergunta; arquivamento de pergunta (com explicação do código).
