package br.empresateste.testerelatorio.bean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import br.empresateste.testerelatorio.conexao.Conexao;
import br.empresateste.testerelatorio.dao.AlunoDao;
import br.empresateste.testerelatorio.model.Aluno;

/**
 * Managed Bean que gera o relatório das três formas: passando conexão, passando 
 * ResultSet e passando uma lista de objetos. O escopo é de sessão por 
 * conta da variável de instância saida ser avaliada na página de exibição dos
 * relatórios para mostrar se o relatório foi gerado com sucesso ou não (óbvio que 
 * haviam outras soluções, mas o objetivo da aplicação não era esse).
 *  
 * @author Pablo Nóbrega
 *
 */
public class RelatorioBean {

	private String saida;
	
	/**
	 * Esse tipo de geração de relatório é útil quando precisamos apenas da conexão
	 * com o banco e quando o JasperReports precisa de nenhum ou de poucos 
	 * parâmetros para realizar a query (exemplo: o id de um objeto que está no banco).
	 * 
	 * @return String navigation rule que exibe o relatório 
	 */	
	public String geraRelatorioPassandoConexao() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno.jasper");
		Connection conexao = null;

		try {
			// Abro a conexão com o banco que será passada para o JasperReports
			conexao = new Conexao().getConexao();
			// Mando o jasper gerar o relatório
			JasperPrint print = JasperFillManager.fillReport(jasper, null, conexao);
			// Gero o PDF
			preenchePdf(print);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Sempre mando fechar a conexão, mesmo que tenha dado erro
				if (conexao != null)
					conexao.close();
			} catch (SQLException e) {
				
			}
		}
		
		return "exibeRelatorio";
	}
	
	/**
	 * Esse tipo de geração de relatório é útil quando a query com o banco pode mudar
	 * dinamicamente. Exemplo: utilização de um filtro.
	 * 
	 * @return String navigation rule que exibe o relatório 
	 */		
	public String geraRelatorioPassandoResultSet() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno.jasper");
		Connection conexao = null;

		try {
			// Abro a conexão com o banco
			conexao = new Conexao().getConexao();
			// Gero o ResultSet que será enviado para o Jasper a partir da conexão aberta
			JRResultSetDataSource jrsds = new JRResultSetDataSource(getResultSet(conexao));
			// Mando o jasper gerar o relatório
			JasperPrint print = JasperFillManager.fillReport(jasper, null, jrsds);
			// Gero o PDF
			preenchePdf(print);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Sempre mando fechar a conexão, mesmo que tenha dado erro
				if (conexao != null)
					conexao.close();
			} catch (SQLException e) {
				
			}
		}
		
		return "exibeRelatorio";
	}

	/**
	 * Esse tipo de geração de relatório é uma alternativa aos outros dois. Nesse 
	 * exemplo utilizo um subrelatório param mostrar essa técnica que também pode 
	 * ser empregada. 
	 * 
	 * @return String navigation rule que exibe o relatório 
	 */	
	public String geraRelatorioPassandoListaDeObjetos() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno_com_lista.jasper");
		Connection conexao = null;
		
		try {
			// Conexão com o banco para o segundo relatório
			conexao = new Conexao().getConexao();
			// criação dos parametros
			Map<String, Object> map = new HashMap<String, Object>();
			// conexão com o banco que será utilizada pelo subrelatório
			map.put("REPORT_CONNECTION", conexao);
			// pego o caminho do diretório onde se encontra o subrelatório
			map.put("SUBREPORT_DIR", getDiretorioReal("/jasper/") + "/");
			ArrayList<Aluno> alunos = getListaAlunos(conexao);

			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(alunos);
			/*
			 * Mando o jasper gerar o relatório. Nesse caso passo o map, 
			 * já que ele tem dois parâmetros que serão utilizados
			 */ 
			JasperPrint print = JasperFillManager.fillReport(jasper, map, ds);
			// Gero o PDF
			preenchePdf(print);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "exibeRelatorio";
	}
	
	/**
	 * Esse método gera o ResultSet que será passado para o Jasper. Cada
	 * coluna deve ter o mesmo nome utilizado nos fields do relatório.
	 * 
	 * @param conexao Connection conexão com o banco de dados
	 * @return ResultSet será utilizado pelo JasperReports para gerar o relatório
	 *  
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private ResultSet getResultSet(Connection conexao) throws SQLException, ClassNotFoundException {
		Statement stmt = conexao.createStatement();
				
		ResultSet rs = stmt.executeQuery("SELECT aluno.nome AS aluno_nome, " +
				"aluno.matricula AS aluno_matricula, professor.nome AS professor_nome, " +
				"aluno.id_aluno AS aluno_id_aluno FROM aluno aluno " +
				"INNER JOIN professores_alunos professores_alunos ON aluno.id_aluno = professores_alunos.id_aluno " +
				"INNER JOIN `professor` professor ON professores_alunos.id_professor = professor.id_professor");
		
		return rs;
	}
	
	private ArrayList<Aluno> getListaAlunos(Connection conexao) throws SQLException {
		return (ArrayList<Aluno>) new AlunoDao().loadAll(conexao);
	}
	
	/**
	 * Método para preencher o PDF do relatório
	 * 
	 * @param print JasperPrint
	 * @throws JRException
	 */
	private void preenchePdf(JasperPrint print) throws JRException {
		// Pego o caminho completo do PDF desde a raiz
		saida = getDiretorioReal("/pdf/relatorio.pdf");
		// Exporto para PDF
		JasperExportManager.exportReportToPdfFile(print, saida);
		/*
		 * Jogo na variável saída o nome da aplicação mais o 
		 * caminho para o PDF. Essa variável será utilizada pela view 
		 */
		saida = getContextPath() + "/pdf/relatorio.pdf";
	}
	
	/**
	 * Método para retornar o caminho completo do diretório onde se encontra o 
	 * arquivo 'jasper' e o arquivo 'pdf'
	 *  
	 * @param diretorio String diretório a ser localizado na aplicação
	 * @return String caminho completo
	 */
	private String getDiretorioReal(String diretorio) {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getServletContext().getRealPath(diretorio);
	}
	
	/**
	 * Método para retornar o nome da aplicação
	 *  
	 * @return String nome da aplicacao 
	 */
	private String getContextPath() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getServletContext().getContextPath();
	}
	
	public String getSaida() {
		return saida;
	}

	public void setSaida(String saida) {
		this.saida = saida;
	}
	
}