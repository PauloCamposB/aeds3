package util;

import estruturas.RegistroHashExtensivel;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;



public class ParEmailID implements RegistroHashExtensivel<ParEmailID> {
    private String email;
    private int id;

    private static final int TAMANHO_EMAIL = 128;
    private static final int TAMANHO_REGISTRO = 132;

    
    @Override
    public ParEmailID clone() {
        return new ParEmailID(this.email, this.id);
    }
    

    public ParEmailID() {
        this("", -1);
    }

    public ParEmailID(String email, int id) {
        this.email = email;
        this.id = id;
    }

    public String getEmail() { return email; }
    public int getId() { return id; }

    @Override
    public int hashCode() {
        return hash(this.email);
    }

    public static int hash(String email) {
        return email.trim().toLowerCase(Locale.ROOT).hashCode() & 0x7FFFFFFF;
    }

    @Override
    public short size() {
        return (short) TAMANHO_REGISTRO;
    }

    @Override
    public byte[] toByteArray() throws Exception {
        String emailTratado = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);
        byte[] emailBytes = emailTratado.getBytes(StandardCharsets.UTF_8);

        if (emailBytes.length > TAMANHO_EMAIL) {
            throw new IllegalArgumentException("E-mail muito grande");
        }

        ByteBuffer buffer = ByteBuffer.allocate(TAMANHO_REGISTRO);
        buffer.put(Arrays.copyOf(emailBytes, TAMANHO_EMAIL));
        buffer.putInt(id);

        return buffer.array();
    }

    @Override
    public void fromByteArray(byte[] ba) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(ba);

        byte[] emailBytes = new byte[TAMANHO_EMAIL];
        buffer.get(emailBytes);

        int tamanho = 0;
        while (tamanho < TAMANHO_EMAIL && emailBytes[tamanho] != 0) {
            tamanho++;
        }

        this.email = new String(emailBytes, 0, tamanho, StandardCharsets.UTF_8);
        this.id = buffer.getInt();
    }
}