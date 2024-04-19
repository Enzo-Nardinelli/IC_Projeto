package com.mycompany.icserver;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyAdministrator {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    private String login;
    private String comandoBD;

    public PolicyAdministrator(String login, String comandoBD) {
        this.login = login;
        this.comandoBD = comandoBD;
    }

    public boolean tokenVerificacao() {
        ResultSet rs = null;
        boolean registoVazio = false;
        String url = "jdbc:mysql://localhost:3306/hospital";
        String username = "java";
        String password = "password";
        System.out.println("Connecting database ...");
        String token = "";
        String validade = "";
        String loginToken = "";
        String novoToken = "";
        boolean retorno = false;
        LocalDateTime ldt1 = null;
        LocalDateTime ldt2 = null;
        int diff = 0;
        
        System.out.println(comandoBD);
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            try {
                Statement st = connection.createStatement();
                rs = st.executeQuery(comandoBD);
                rs.last();
                token = rs.getString("token");
                validade = rs.getString("validade");
                loginToken = rs.getString("login");
                
            } catch (SQLException i) {
                System.out.println(i);
                System.out.println("Não há token registrados por esse login");
                registoVazio = true;
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }

        if (!"".equals(validade)) {
            System.out.println(validade + "oioi");
            String[] validadeFormatada = validade.split(" ");
            validade = validadeFormatada[0] + "T" + validadeFormatada[1];
            ldt1 = LocalDateTime.parse(validade);
            ldt2 = LocalDateTime.now();

            diff = ldt1.compareTo(ldt2);
        }

        if (diff > 0 || registoVazio == true) { // bug aqui
            novoToken = generateNewToken();
            System.out.println("Token gerado!");
        }

        String line = "";

        if (!"".equals(novoToken)) {
            line = "INSERT INTO tokens(token,validade,login) VALUES ('" + novoToken + "','" + LocalDateTime.now().plusHours(12) + "','" + login + "')";
        } else {
            retorno = true; //token existente ainda está dentro da validade
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");

            try {
                Statement st = connection.createStatement();
                st.executeUpdate(line);
                retorno = true;

            } catch (SQLException i) {
                System.out.println(i);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        return retorno;
    }

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

}
