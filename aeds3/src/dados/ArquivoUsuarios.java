package dados;

import util.ParEmailID;
import entidades.Usuario;
import estruturas.Arquivo;
import estruturas.HashExtensivel;
import util.Seguranca;

public class ArquivoUsuarios extends Arquivo<Usuario> {

    private HashExtensivel<ParEmailID> indiceEmail;

    public ArquivoUsuarios() throws Exception {
        super("usuarios.db", Usuario.class.getConstructor());
        indiceEmail = new HashExtensivel<>(
            ParEmailID.class.getConstructor(),
            4,
            "dados/usuarios_email.hash.db",
            "dados/usuarios_email.cesto.db"
        );
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        //Verifica se e-mail ja existe na Tabela Hash
        ParEmailID existe = indiceEmail.read(ParEmailID.hash(usuario.getEmail()));
        if (existe != null && existe.getId() != -1) {
            throw new Exception("E-mail ja cadastrado!");
        }

        //Criptografa senha e resposta secreta com SHA-256
        usuario.setHashSenha(Seguranca.hashSHA256(usuario.getHashSenha()));
        usuario.setHashRespostaSecreta(Seguranca.hashSHA256(usuario.getHashRespostaSecreta()));

        //Salva no arquivo principal e insere no indice Hash
        int id = super.create(usuario);
        usuario.setId(id);
        indiceEmail.create(new ParEmailID(usuario.getEmail(), id));

        return id;
    }

    public Usuario readByEmail(String email) throws Exception {
        ParEmailID par = indiceEmail.read(ParEmailID.hash(email));
        if (par != null && par.getId() != -1) {
            return super.read(par.getId());
        }
        return null;
    }

    public Usuario autenticar(String email, String senhaDigitada) throws Exception {
        Usuario u = readByEmail(email);
        if (u != null) {
            String senhaHash = Seguranca.hashSHA256(senhaDigitada);
            if (u.getHashSenha().equals(senhaHash)) {
                return u;
            }
        }
        return null;
    }

    // Atualiza o e-mail na Tabela Hash se o e-mail tiver mudado
    public boolean atualizarEmail(Usuario u, String emailAntigo, String novoEmail) throws Exception {
        // Verifica se o novo email ja pertence a outro usuario
        ParEmailID existe = indiceEmail.read(ParEmailID.hash(novoEmail));
        if (existe != null && existe.getId() != -1 && existe.getId() != u.getId()) {
            throw new Exception("O novo e-mail ja esta em uso por outro usuario!");
        }

        // Remove a chave antiga da Tabela Hash
        indiceEmail.delete(ParEmailID.hash(emailAntigo));

        // Atualiza a entidade
        u.setEmail(novoEmail);
        boolean ok = super.update(u);

        // Insere a nova chave na Tabela Hash
        if (ok) {
            indiceEmail.create(new ParEmailID(novoEmail, u.getId()));
        }
        return ok;
    }

    // Atualiza senha criptografando novamente
    public boolean atualizarSenha(Usuario u, String novaSenha) throws Exception {
        u.setHashSenha(Seguranca.hashSHA256(novaSenha));
        return super.update(u);
    }

    // Atualiza Pergunta e Resposta Secreta
    public boolean atualizarPerguntaEAntena(Usuario u, String pergunta, String resposta) throws Exception {
        u.setPerguntaSecreta(pergunta);
        u.setHashRespostaSecreta(Seguranca.hashSHA256(resposta));
        return super.update(u);
    }
}