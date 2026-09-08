package util;

import java.math.BigInteger;
import java.security.MessageDigest;


public class Seguranca {

    public static String hashSHA256(String texto) throws Exception{
        if(texto == null){
            return null;
        }

        try{
            //aqui a gnt vai criar um objeto configurado para utilizar o SHA-256
            //que  basicamente vamos transformar qualquer dado em uma sequencia de 256 bits 
            // que serve basicamente pra pra nao guardar diretamente a senha ou resposta secreta 
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            //converte o texto para byte usando o UTF-8 e calcula o hash 
            byte[] hash = md.digest(texto.getBytes("UTF-8"));

            //vai converter o array de bytes em um numero e o valor 1 indica que o numero deve ser positivo
            BigInteger number = new BigInteger(1, hash);
            return number.toString(16);
        }catch (Exception e){
            throw new Exception("Erro ao calcular o hash", e);
        }
    }
}