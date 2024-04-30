package com.mycompany.icserver;

import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ICServer {

    //inicializa as sockets e as streams
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    //private Connection connection = null;

    // construtor que inicializa a socket server
    public ICServer(int port) {

        // starts server and waits for a connection
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

        } catch (IOException i) {
            System.out.println(i);
        }
        ProcessHandle.allProcesses().forEach(processHandle
                -> System.out.println("PID: " + processHandle.pid()
                        + ", command: " + processHandle.info().command().orElse("Unknown")));
        policyEnforcementPoint();
    }

    //função que permanece ouvindo para novas conexões
    public void policyEnforcementPoint() {

        String comandoBD = "";
        try {
            while (true) {
                socket = server.accept();
                String line = "";

                // takes input from the client socket
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(
                        socket.getOutputStream());
                comandoBD = in.readUTF();
                String[] comandoBDSplit = comandoBD.split(" ");

                if ("INSERT".equals(comandoBDSplit[0])) {
                    createBD(comandoBD);
                } else if ("SELECT".equals(comandoBDSplit[0])) {
                    line = readBD(comandoBD, comandoBDSplit[3]);
                    out.writeUTF(line);
                    out.flush();
                    System.out.println(line);
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                if ("INSERT".equals(comandoBDSplit[0])) {
                    createBD("INSERT INTO acessos(data,ip,infoAcesso) VALUES ('" + dtf.format(now)
                            + "','" + socket.getRemoteSocketAddress().toString()
                            + "','" + comandoBDSplit[2] + " " + line + "')");
                } else if ("SELECT".equals(comandoBDSplit[0])) {
                    createBD("INSERT INTO acessos(data,ip,infoAcesso) VALUES ('" + dtf.format(now)
                            + "','" + socket.getRemoteSocketAddress().toString()
                            + "','" + comandoBDSplit[3] + " " + line + "')");
                }
                System.out.println(comandoBDSplit[3]);
                try {
                    // close connection
                    socket.close();
                    in.close();
                    out.close();
                } catch (IOException i) {
                    System.out.println(i);
                }
            }

        } catch (IOException i) {
            System.out.println(i);
        }

    }

    //função que faz o comando INSERT para o Banco de Dados
    public void createBD(String comandoBD) {
        String url = "jdbc:mysql://localhost:3306/hospital";
        String username = "java";
        String password = "password";
        System.out.println("Connecting database ...");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");

            try {
                Statement st = connection.createStatement();
                st.executeUpdate(comandoBD);

            } catch (SQLException i) {
                System.out.println(i);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    // função que faz o comando SELECT para o Banco de Dados
    public String readBD(String comandoBD, String table) {
        String url = "jdbc:mysql://localhost:3306/hospital";
        String username = "java";
        String password = "password";
        System.out.println("Connecting database ...");
        List<String> registros = new ArrayList<String>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected!");
            try {
                Statement st = connection.createStatement();

                if ("usuarios".equals(table)) {
                    String token ="";
                    PolicyEngine pe = new PolicyEngine(token, comandoBD);
                    if (pe.permitirAcessoUsuario()== true) {
                        registros.add("1" + " " + pe.retornarToken());
                    } else {
                        registros.add("0");
                    }
                } else if ("consultas".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                        String cpf = rs.getString("CPF");
                        String cdgMedico = rs.getString("codigoMedico");
                        String data = rs.getString("dataConsulta");
                        String obs = rs.getString("obs");

                        registros.add(cpf + "!" + cdgMedico + "!" + data + "!" + obs);
                    }
                } else if ("pacientes".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                        String cpf = rs.getString("CPF");
                        String pacienteNome = rs.getString("pacienteNome");
                        String pacienteSobrenome = rs.getString("pacienteSobrenome");

                        registros.add(cpf + "!" + pacienteNome + "!" + pacienteSobrenome);
                    }
                } else if ("quartosOcupados".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                        String numeroQuarto = rs.getString("numeroQuarto");
                        String cpf = rs.getString("CPF");
                        String dataOcupacao = rs.getString("dataOcupacao");

                        registros.add(numeroQuarto + "!" + cpf + "!" + dataOcupacao);
                    }
                }
            } catch (SQLException i) {
                System.out.println(i);
            }
            System.out.println(comandoBD);
            System.out.println("Closing connection");

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }

        //serverDefault();
        String registrosString = "";
        for (int i = 0; i < registros.size(); i++) {
            if (i != registros.size() - 1) {
                registrosString += registros.get(i) + "=";
            } else {
                registrosString += registros.get(i);
            }
        }
        System.out.println(registrosString);
        return registrosString;
    }

    public void updateBD() {

    }

    public void deleteBD() {

    }

    public static void main(String args[]) {
        ICServer server = new ICServer(5001);
    }
}
