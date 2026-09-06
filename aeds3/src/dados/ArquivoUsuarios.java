package dados;

import util.ParEmailID;
import entidades.Usuario;
import estruturas.Arquivo;
import estruturas.HashExtensivel;
import util.Seguranca;

public class ArquivoUsuarios extends Arquivo<Usuario> {

    private HashExtensivel<ParEmailID> indiceEmail;

    public ArquivoUsuarios() throws Exception {
        // Passa o construtor da entidade Usuario e o nome do arquivo .db
       super("usuarios.db", Usuario.class.getConstructor());
        // Inicializa a Tabela Hash Extensivel para e-mails
        indiceEmail = new HashExtensivel<>(
            ParEmailID.class.getConstructor(),
            4,
            "dados/usuarios_email.hash.db",
            "dados/usuarios_email.cesto.db"
        );
    }

    @Override
    public int create(Usuario usuario) throws Exception {
        // 1. Verifica se e-mail ja existe na Tabela Hash
        ParEmailID existe = indiceEmail.read(ParEmailID.hash(usuario.getEmail()));
        if (existe != null && existe.getId() != -1) {
            throw new Exception("E-mail ja cadastrado!");
        }

        // 2. Aplica Hash SHA-256 na senha
        String senhaHash = Seguranca.hashSHA256(usuario.getHashSenha());
        usuario.setHashSenha(senhaHash);

        // 3. Salva no arquivo de dados principal
        int id = super.create(usuario);
        usuario.setId(id);

        // 4. Cadastra a relacao (Email -> ID) na Hash Extensivel
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
}