package plp.oo1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import plp.orientadaObjetos1.Programa;
import plp.orientadaObjetos1.expressao.valor.ValorBooleano;
import plp.orientadaObjetos1.expressao.valor.ValorInteiro;
import plp.orientadaObjetos1.memoria.AmbienteCompilacaoOO1;
import plp.orientadaObjetos1.memoria.AmbienteExecucaoOO1;
import plp.orientadaObjetos1.memoria.ContextoCompilacaoOO1;
import plp.orientadaObjetos1.memoria.ContextoExecucaoOO1;
import plp.orientadaObjetos1.memoria.colecao.ListaValor;
import plp.orientadaObjetos1.parser.OO1Parser;
import plp.orientadaObjetos1.parser.ParseException;

public class TesteGC {

	static OO1Parser parser;
	String input;
	private String resultado;
	private boolean aceitoTipo;
	private boolean aceitoValor;
	private boolean aceitoExcecao;
	private ListaValor entrada;

	
	public TesteGC(){
		
	}
	public TesteGC(String input, String resultado, boolean aceitoTipo,
			boolean aceitoValor, boolean aceitoExcecao) {
		super();
		this.input = input;
		this.resultado = resultado;
		this.aceitoTipo = aceitoTipo;
		this.aceitoValor = aceitoValor;
		this.aceitoExcecao = aceitoExcecao;
		entrada = new ListaValor(new ValorInteiro(1), new ListaValor(
				new ValorBooleano(true)));
	}

	@Before
	public void setup() {
		try {
			parser = new OO1Parser(new FileInputStream("TesteGc.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testGc() throws Exception{

			AmbienteCompilacaoOO1 ambComp = new ContextoCompilacaoOO1(entrada);
			Programa programa = parser.processaEntrada();

			boolean tipoOK = programa.checaTipo(ambComp);
			assertThat("Erro de Tipo no programa: \n" + input, tipoOK,
					is(this.aceitoTipo));

			if (tipoOK) {
				AmbienteExecucaoOO1 ambExec = new ContextoExecucaoOO1(entrada);
				String valor = programa.executar(ambExec).toString();
				boolean valorOK = valor.equalsIgnoreCase(resultado);
				assertThat("Resultado errado para a avaliação de:\n" + input,
						valorOK, is(this.aceitoValor));
			}

			if (this.aceitoExcecao) {
				fail("Deveria lançar Excecao");
			}
		
	}

}
