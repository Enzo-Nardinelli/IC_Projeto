package com.mycompany.icserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PolicyEngine {

    private String login;
    private String senha;
    private String loginCredencial;
    private String senhaCredencial;
    private int codigoFuncao;
    private String appAcesso;
    private String comandoBD;
    private String token;

    public PolicyEngine(String token, String comandoBD) {
        String url = "jdbc:mysql://localhost:3306/hospital";
        String username = "java";
        String password = "password";
        System.out.println("Connecting database ...");
        List<String> registros = new ArrayList<String>();
        
        this.comandoBD = comandoBD;
        this.token = token;
        
        if (!"".equals(comandoBD)) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Database connected!");
                try {
                    Statement st = connection.createStatement();
                    String[] usuarioCredencial = comandoBD.split(" ");
                    login = usuarioCredencial[6];
                    senha = usuarioCredencial[7];
                    appAcesso = usuarioCredencial[8];
                    String novoComandoBD = usuarioCredencial[0] + " " + usuarioCredencial[1] + " " + usuarioCredencial[2] + " " + usuarioCredencial[3] + " " + usuarioCredencial[4] + " " + usuarioCredencial[5];
                    ResultSet rs = st.executeQuery(novoComandoBD);
                    while (rs.next()) {
                        loginCredencial = rs.getString("login");
                        senhaCredencial = rs.getString("senha");
                        codigoFuncao = rs.getInt("codigoFuncao");
                    }
                } catch (SQLException i) {
                    System.out.println(i);
                }
                System.out.println(comandoBD);
                System.out.println("Closing connection");

            } catch (SQLException e) {
                throw new IllegalStateException("Cannot connect the database!", e);
            }
        }

    }

    public boolean permitirAcessoUsuario() {
        boolean permissao = false;
        comandoBD = "SELECT * FROM tokens WHERE login='" + login + "' ORDER BY validade ASC";
        PolicyAdministrator pa = new PolicyAdministrator(login, comandoBD, token);
        if (login.equals(loginCredencial) && senha.equals(senhaCredencial)) {
            if ("Client".equals(appAcesso)) { // usado para checar qual aplicação foi usada (por enquanto só existe uma) 
                if (codigoFuncao == 1) {
                    token = pa.acharUltimoToken();
                    permissao = true;
                }
            }
        }
        return permissao;
    }
    
    public boolean permitirAcessoRequisicao() {
        PolicyAdministrator pa = new PolicyAdministrator(login, comandoBD, token);
        return pa.tokenVerificacao(token);
    }
    
    public String retornarToken () {
        return token;
    }
}
