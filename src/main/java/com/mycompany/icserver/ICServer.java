
package com.mycompany.icserver;


import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ICServer
{
    //inicializa as sockets e as streams
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;
    private DataOutputStream out = null;
    //private Connection connection = null;
 
    // construtor que inicializa a socket server
    public ICServer(int port)
    {
        
        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started");
 
            System.out.println("Waiting for a client ...");
 
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        
        serverDefault();
    }
    //função que permanece ouvindo para novas conexões
    public void serverDefault(){
        
        String comandoBD = ""; 
        try {
                while(true) {
                    socket = server.accept();
 
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
                        String line = readBD(comandoBD, comandoBDSplit[3]);
                        out.writeUTF(line);
                        out.flush();
                        System.out.println(line);
                    }

                    System.out.println(comandoBD);
                    try {
                        // close connection
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException i) {
                        System.out.println(i);
                    }
                }
                

            }
            catch(IOException i) {
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
               
                if("usuarios".equals(table)){
                    String[] usuarioCredencial = comandoBD.split(" ");
                    String loginCredencial = usuarioCredencial[6];
                    String senhaCredencial = usuarioCredencial[7];
                    String novoComandoBD = usuarioCredencial[0] +" "+  usuarioCredencial[1] +" "+ usuarioCredencial[2]+" "+ usuarioCredencial[3] +" "+ usuarioCredencial[4] +" "+ usuarioCredencial[5];
                    ResultSet rs = st.executeQuery(novoComandoBD);
                    String login = "";
                    String senha = "";
                    while (rs.next()) {
                    login = rs.getString("login");
                    senha = rs.getString("senha");
                    String codigoFuncao = rs.getString("codigoFuncao");
                        if (login.equals(loginCredencial) && senha.equals(senhaCredencial)) {
                            registros.add("ok");
                            System.out.println("ok");
                        }
                    }
                } else if ("consultas".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                    String cpf = rs.getString("CPF");
                    String cdgMedico = rs.getString("codigoMedico");
                    String data = rs.getString("dataConsulta");
                    String obs = rs.getString("obs");

                    registros.add(cpf+"!"+cdgMedico+"!"+data+"!"+obs);
                    }
                } else if ("pacientes".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                    String cpf = rs.getString("CPF");
                    String pacienteNome = rs.getString("pacienteNome");
                    String pacienteSobrenome = rs.getString("pacienteSobrenome");

                    registros.add(cpf+"!"+pacienteNome+"!"+pacienteSobrenome);
                    }
                } else if ("quartosOcupados".equals(table)) {
                    ResultSet rs = st.executeQuery(comandoBD);
                    while (rs.next()) {
                    String numeroQuarto = rs.getString("numeroQuarto");
                    String cpf = rs.getString("CPF");
                    String dataOcupacao = rs.getString("dataOcupacao");

                    registros.add(numeroQuarto+"!"+cpf+"!"+dataOcupacao);
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
 
    public static void main(String args[])
    {
        ICServer server = new ICServer(5001);
    }
}