import dao.AluguelDAO;
import dao.ClienteDAO;
import dao.FilmeDAO;
import dao.jdbc.AluguelDAOImpl;
import dao.jdbc.ClienteDAOImpl;
import dao.jdbc.FilmeDAOImpl;
import entidades.Aluguel;
import entidades.Cliente;
import entidades.Filme;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Main {

	public static void main(String[] args) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/estagio", "estagio", "estagio");
            conn.setAutoCommit(false);

            AluguelDAO aluguelDAO = new AluguelDAOImpl();
            ClienteDAO clienteDAO = new ClienteDAOImpl();
            FilmeDAO filmeDAO = new FilmeDAOImpl();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            //
            // CLIENTE
            //
            
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("- TESTE: Cliente                                                                                   -");
            System.out.println("----------------------------------------------------------------------------------------------------");
            
            // Adicionar Cliente
            Cliente cliente = new Cliente("Jhonathan");
            clienteDAO.insert(conn, cliente);
            System.out.println("> INSERT: \n  " + cliente.toString());
            
            // Modificar Cliente
            cliente = new Cliente(cliente.getIdCliente(), "Jonathan");
            clienteDAO.edit(conn, cliente);
            System.out.println("> EDIT: \n  " + cliente.toString());
            
            // Listar Clientes
            Collection<Cliente> clientes = clienteDAO.list(conn);
            System.out.println("> LIST:");
            for (Cliente c : clientes) {
            	System.out.println("  " + c.toString());
            }
            System.out.println();
            
            
            //
            // FILME
            //

            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("- TESTE: Filme                                                                                     -");
            System.out.println("----------------------------------------------------------------------------------------------------");
            
            // Adicionar Filme
            Filme filme = new Filme(dateFormat.parse("2019-11-05"), "Dois Papa", "Comédia");
            filmeDAO.insert(conn, filme);
            System.out.println("> INSERT: \n  " + filme.toString());
            
            // Modificar Filme
            filme = new Filme(filme.getIdFilme(), dateFormat.parse("2019-12-05"), "Dois Papas", "Drama/Comédia");
            filmeDAO.edit(conn, filme);
            System.out.println("> EDIT: \n  " + filme.toString());
            
            // Listar Filmes
            System.out.println("> LIST:");
            Collection<Filme> filmes = filmeDAO.list(conn);            
            for (Filme f : filmes) {
            	System.out.println("  " + f.toString());
            }
            System.out.println();
            
            
            //
            // ALUGUEL
            //
            
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("- TESTE: Aluguel                                                                                   -");
            System.out.println("----------------------------------------------------------------------------------------------------");
            
            // Adicionar Aluguel
            filmes = new ArrayList<Filme>();
            filmes.add(filmeDAO.find(conn, 1));
            filmes.add(filmeDAO.find(conn, 2));
            
            Aluguel aluguel = new Aluguel((List<Filme>) filmes, cliente, dateFormat.parse("2019-12-05"), (float) 15.6);
            aluguelDAO.insert(conn, aluguel);
            System.out.println("> INSERT: \n  " + aluguel.toString());
            
            // Modificar Aluguel
            Cliente outroCliente = clienteDAO.find(conn, 2);
            
            filmes = new ArrayList<Filme>();
            filmes.add(filmeDAO.find(conn, 2));
            filmes.add(filmeDAO.find(conn, 3));
            filmes.add(filmeDAO.find(conn, 4));
            
            aluguel = new Aluguel(8, (List<Filme>) filmes, outroCliente, dateFormat.parse("2020-01-15"), (float) 22.7);
            aluguelDAO.edit(conn, aluguel);
            System.out.println("> EDIT: \n  " + aluguel.toString());
            
            // Listar Aluguel
            System.out.println("> LIST:");
            Collection<Aluguel> alugueis = aluguelDAO.list(conn);          
            for (Aluguel a : alugueis) {
            	System.out.println("  " + a.toString());
            }
            System.out.println();
            
            
            //
            // EXCLUSÂO
            //
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("- TESTE: Exclusão                                                                                   -");
            System.out.println("----------------------------------------------------------------------------------------------------");
            
            aluguelDAO.delete(conn, aluguel.getIdAluguel());
            System.out.println("> DELETE (Aluguel): \n  " + aluguel.toString());
            
            clienteDAO.delete(conn, cliente.getIdCliente());
            System.out.println("> DELETE (Cliente): \n  " + cliente.toString());
            
            filmeDAO.delete(conn, filme.getIdFilme());
            System.out.println("> DELETE (Filme): \n  " + filme.toString());
            
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Fim do teste.");
    }
}