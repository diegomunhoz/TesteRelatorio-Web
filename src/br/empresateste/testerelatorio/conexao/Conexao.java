package br.empresateste.testerelatorio.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * Classe que armazena uma conexão com o banco. A conexão é aberta no construtor da
 * classe.
 * 
 * @author Pablo Nóbrega
 *
 */
public class Conexao {
	private Connection conexao;

	public Conexao() throws ClassNotFoundException, SQLException {
		criaConexao();
	}

	public void criaConexao() throws ClassNotFoundException, SQLException {
		String endereco = "localhost";
		String porta = "3306";
		String banco = "academico";
		String usuario = "root";
		String senha = "root";

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conexao = DriverManager.getConnection("jdbc:mysql://" + endereco
					+ ":" + porta + "/" + banco + "?user=" + usuario
					+ "&password=" + senha);

		} catch (ClassNotFoundException ex) {
			throw ex;
		} catch (SQLException ex) {
			throw ex;
		}

	}

	public void fechaConexao() throws SQLException {
		conexao.close();
		conexao = null;
	}

	public boolean isFechada() {
		try {
			return conexao.isClosed();
		} catch (SQLException ex) {
			return false;
		}
	}

	public Connection getConexao() {
		return conexao;
	}

}
