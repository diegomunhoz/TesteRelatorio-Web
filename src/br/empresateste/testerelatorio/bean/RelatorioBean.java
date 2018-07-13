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
 * Managed Bean que gera o relat�rio das tr�s formas: passando conex�o, passando 
 * ResultSet e passando uma lista de objetos. O escopo � de sess�o por 
 * conta da vari�vel de inst�ncia saida ser avaliada na p�gina de exibi��o dos
 * relat�rios para mostrar se o relat�rio foi gerado com sucesso ou n�o (�bvio que 
 * haviam outras solu��es, mas o objetivo da aplica��o n�o era esse).
 *  
 * @author Pablo N�brega
 *
 */
public class RelatorioBean {

	private String saida;
	
	/**
	 * Esse tipo de gera��o de relat�rio � �til quando precisamos apenas da conex�o
	 * com o banco e quando o JasperReports precisa de nenhum ou de poucos 
	 * par�metros para realizar a query (exemplo: o id de um objeto que est� no banco).
	 * 
	 * @return String navigation rule que exibe o relat�rio 
	 */	
	public String geraRelatorioPassandoConexao() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno.jasper");
		Connection conexao = null;

		try {
			// Abro a conex�o com o banco que ser� passada para o JasperReports
			conexao = new Conexao().getConexao();
			// Mando o jasper gerar o relat�rio
			JasperPrint print = JasperFillManager.fillReport(jasper, null, conexao);
			// Gero o PDF
			preenchePdf(print);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Sempre mando fechar a conex�o, mesmo que tenha dado erro
				if (conexao != null)
					conexao.close();
			} catch (SQLException e) {
				
			}
		}
		
		return "exibeRelatorio";
	}
	
	/**
	 * Esse tipo de gera��o de relat�rio � �til quando a query com o banco pode mudar
	 * dinamicamente. Exemplo: utiliza��o de um filtro.
	 * 
	 * @return String navigation rule que exibe o relat�rio 
	 */		
	public String geraRelatorioPassandoResultSet() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno.jasper");
		Connection conexao = null;

		try {
			// Abro a conex�o com o banco
			conexao = new Conexao().getConexao();
			// Gero o ResultSet que ser� enviado para o Jasper a partir da conex�o aberta
			JRResultSetDataSource jrsds = new JRResultSetDataSource(getResultSet(conexao));
			// Mando o jasper gerar o relat�rio
			JasperPrint print = JasperFillManager.fillReport(jasper, null, jrsds);
			// Gero o PDF
			preenchePdf(print);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Sempre mando fechar a conex�o, mesmo que tenha dado erro
				if (conexao != null)
					conexao.close();
			} catch (SQLException e) {
				
			}
		}
		
		return "exibeRelatorio";
	}

	/**
	 * Esse tipo de gera��o de relat�rio � uma alternativa aos outros dois. Nesse 
	 * exemplo utilizo um subrelat�rio param mostrar essa t�cnica que tamb�m pode 
	 * ser empregada. 
	 * 
	 * @return String navigation rule que exibe o relat�rio 
	 */	
	public String geraRelatorioPassandoListaDeObjetos() {
		saida = null;
		String jasper = getDiretorioReal("/jasper/professores_por_aluno_com_lista.jasper");
		Connection conexao = null;
		
		try {
			// Conex�o com o banco para o segundo relat�rio
			conexao = new Conexao().getConexao();
			// cria��o dos parametros
			Map<String, Object> map = new HashMap<String, Object>();
			// conex�o com o banco que ser� utilizada pelo subrelat�rio
			map.put("REPORT_CONNECTION", conexao);
			// pego o caminho do diret�rio onde se encontra o subrelat�rio
			map.put("SUBREPORT_DIR", getDiretorioReal("/jasper/") + "/");
			ArrayList<Aluno> alunos = getListaAlunos(conexao);

			JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(alunos);
			/*
			 * Mando o jasper gerar o relat�rio. Nesse caso passo o map, 
			 * j� que ele tem dois par�metros que ser�o utilizados
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
	 * Esse m�todo gera o ResultSet que ser� passado para o Jasper. Cada
	 * coluna deve ter o mesmo nome utilizado nos fields do relat�rio.
	 * 
	 * @param conexao Connection conex�o com o banco de dados
	 * @return ResultSet ser� utilizado pelo JasperReports para gerar o relat�rio
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
	 * M�todo para preencher o PDF do relat�rio
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
		 * Jogo na vari�vel sa�da o nome da aplica��o mais o 
		 * caminho para o PDF. Essa vari�vel ser� utilizada pela view 
		 */
		saida = getContextPath() + "/pdf/relatorio.pdf";
	}
	
	/**
	 * M�todo para retornar o caminho completo do diret�rio onde se encontra o 
	 * arquivo 'jasper' e o arquivo 'pdf'
	 *  
	 * @param diretorio String diret�rio a ser localizado na aplica��o
	 * @return String caminho completo
	 */
	private String getDiretorioReal(String diretorio) {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getServletContext().getRealPath(diretorio);
	}
	
	/**
	 * M�todo para retornar o nome da aplica��o
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