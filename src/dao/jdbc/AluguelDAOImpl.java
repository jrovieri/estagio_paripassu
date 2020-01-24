package dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import dao.AluguelDAO;
import dao.ClienteDAO;
import dao.FilmeDAO;
import entidades.Aluguel;
import entidades.Cliente;
import entidades.Filme;

public class AluguelDAOImpl implements AluguelDAO {

	@Override
	public void insert(Connection conn, Aluguel aluguel) throws Exception {
		
		PreparedStatement myStmt0 = conn.prepareStatement("insert into en_aluguel (id_aluguel, "
				+ "id_cliente, data_aluguel, valor) values (?, ?, ?, ?)");
		
		Integer idAluguel = this.getNextId(conn);
		
		myStmt0.setInt(1, idAluguel);
		myStmt0.setInt(2, aluguel.getCliente().getIdCliente());
		myStmt0.setDate(3, new java.sql.Date(aluguel.getDataAluguel().getTime()));
		myStmt0.setFloat(4, aluguel.getValor());
		myStmt0.execute();
		
		StringBuilder sb = new StringBuilder();
		sb.append("insert into re_aluguel_filme (id_aluguel, id_filme) "
				+ "select a.id_aluguel, f.id_filme "
        		+ "from (values (?)) a(id_aluguel) cross join (values ");
		
        for (Filme f : aluguel.getFilmes()) {
        	sb.append("(" + f.getIdFilme() + "),");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" ) f(id_filme)");
        
        PreparedStatement myStmt1 = conn.prepareStatement(sb.toString());       
        myStmt1.setInt(1, idAluguel);
        myStmt1.execute();
        
        conn.commit();
        
        aluguel.setIdAluguel(idAluguel);
	}

	@Override
	public Integer getNextId(Connection conn) throws Exception {
		PreparedStatement myStmt = conn.prepareStatement("select nextval('seq_en_aluguel')");
        ResultSet rs = myStmt.executeQuery();
        rs.next();
        return rs.getInt(1);
	}

	@Override
	public void edit(Connection conn, Aluguel aluguel) throws Exception {
		
		// Exclui a relação aluguel - filme
		PreparedStatement myStmt = conn.prepareStatement("delete from re_aluguel_filme where id_aluguel=?");
		myStmt.setInt(1, aluguel.getIdAluguel());
		myStmt.execute();
		
		// Atualiza a entrada aluguel
		myStmt = conn.prepareStatement("update en_aluguel set id_cliente = ?, data_aluguel = ?, "
				+ "valor = ? where id_aluguel = ?");
		myStmt.setInt(1, aluguel.getCliente().getIdCliente());
		myStmt.setDate(2, new java.sql.Date(aluguel.getDataAluguel().getTime()));
		myStmt.setFloat(3, aluguel.getValor());
		myStmt.setInt(4, aluguel.getIdAluguel());
		myStmt.execute();
		
		// Atualiza o aluguel
		StringBuilder sb = new StringBuilder();
		sb.append("insert into re_aluguel_filme (id_aluguel, id_filme) "
				+ "select a.id_aluguel, f.id_filme "
        		+ "from (values (?)) a(id_aluguel) cross join (values ");
		
        for (Filme f : aluguel.getFilmes()) {
        	sb.append("(" + f.getIdFilme() + "),");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" ) f(id_filme)");
        
        myStmt = conn.prepareStatement(sb.toString());       
        myStmt.setInt(1, aluguel.getIdAluguel());
        myStmt.execute();
        
        conn.commit();
	}

	@Override
	public void delete(Connection conn, Integer idAluguel) throws Exception {
		
		PreparedStatement myStmt = conn.prepareStatement("delete from re_aluguel_filme where id_aluguel = ?");
        myStmt.setInt(1, idAluguel);

        myStmt.execute();
        conn.commit();

	}

	@Override
	public Aluguel find(Connection conn, Integer idAluguel) throws Exception {
		
		// Busca o aluguel
		PreparedStatement myStmt = conn.prepareStatement("select * from en_aluguel where id_aluguel = ?");
        myStmt.setInt(1, idAluguel);
        ResultSet myRs = myStmt.executeQuery();

        if (!myRs.next()) {
            return null;
        }
        
        Integer idCliente = myRs.getInt("id_cliente");
        Date dataAluguel = myRs.getDate("data_aluguel");
        Float valor = myRs.getFloat("valor");
        
        // Busca e cria o objeto Cliente
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        Cliente cliente = clienteDAO.find(conn, idCliente);
        
        // Busca e cria os objetos Filme
        myStmt = conn.prepareStatement("select id_filme from re_aluguel_filme where id_aluguel = ?");
        myStmt.setInt(1, idAluguel);
        myRs = myStmt.executeQuery();

        FilmeDAO filmeDAO = new FilmeDAOImpl();
        List<Filme> filmes = new ArrayList<Filme>();
        
        while(myRs.next()) {
        	Integer idFilme = myRs.getInt("id_filme");
        	Filme filme = filmeDAO.find(conn, idFilme);
        	filmes.add(filme);
        }
        
        return new Aluguel(idAluguel, filmes, cliente, dataAluguel, valor);
	}

	@Override
	public Collection<Aluguel> list(Connection conn) throws Exception {
		
		PreparedStatement myStmt = conn.prepareStatement("select * from en_aluguel order by id_aluguel");
        ResultSet myRs = myStmt.executeQuery();

        Collection<Aluguel> items = new ArrayList<>();
        FilmeDAO filmeDAO = new FilmeDAOImpl();
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        
        while (myRs.next()) {
            Integer idAluguel = myRs.getInt("id_aluguel");
            Integer idCliente = myRs.getInt("id_cliente");
            Date dataAluguel = myRs.getDate("data_aluguel");
            float valor = myRs.getFloat("valor");
            
            myStmt = conn.prepareStatement("select * from re_aluguel_filme where id_aluguel = ?");
            myStmt.setInt(1, idAluguel);
            ResultSet rs = myStmt.executeQuery();
            
            List<Filme> filmes = new ArrayList<>();
            while (rs.next()) {
            	Integer idFilme = rs.getInt("id_filme");
                filmes.add(filmeDAO.find(conn, idFilme));
            }
            
            Cliente cliente = clienteDAO.find(conn, idCliente);
            items.add(new Aluguel(idAluguel, filmes, cliente, dataAluguel, valor));
        }

        return items;
	}
}
