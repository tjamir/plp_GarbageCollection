package plp.oo1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import plp.UtilCaixaPreta;
import plp.orientadaObjetos1.Programa;
import plp.orientadaObjetos1.expressao.valor.ValorBooleano;
import plp.orientadaObjetos1.expressao.valor.ValorInteiro;
import plp.orientadaObjetos1.memoria.AmbienteCompilacaoOO1;
import plp.orientadaObjetos1.memoria.AmbienteExecucaoOO1;
import plp.orientadaObjetos1.memoria.ContextoCompilacaoOO1;
import plp.orientadaObjetos1.memoria.ContextoExecucaoOO1;
import plp.orientadaObjetos1.memoria.colecao.ListaValor;
import plp.orientadaObjetos1.parser.OO1Parser;

@RunWith(Parameterized.class)
public class CaixaPretaGCTest {

	private String input;
	private long referenciasColetadas;
	private ListaValor entrada;

	public CaixaPretaGCTest(String input, long referenciasColetadas) {
		super();
		this.input = input;
		this.referenciasColetadas = referenciasColetadas;
		this.entrada = new ListaValor(new ValorInteiro(1), new ListaValor(
				new ValorBooleano(true)));
	}

	static OO1Parser parser;

	@Before
	public void setup() {
		ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());
		if (parser == null)
			parser = new OO1Parser(bis);
		else
			parser.ReInit(bis);

	}

	@Test
	public void testInput() throws Exception {

		try {
			AmbienteCompilacaoOO1 ambComp = new ContextoCompilacaoOO1(entrada);
			Programa programa = parser.processaEntrada();
//			boolean tipoOK = programa.checaTipo(ambComp);
//
//			assertThat("Erro de Tipo no programa: \n" + input, tipoOK,
//					is(this.aceitoTipo));

//			if (tipoOK) {
				AmbienteExecucaoOO1 ambExec = new ContextoExecucaoOO1(entrada);
				programa.executar(ambExec).toString();
				long referenciasColetadasFinal = ambExec.getGarbageColector()
						.referenciasColetadas();

				assertThat(
						"Número incorreto de referências coletadas para a avaliação de:\n"
								+ input, referenciasColetadasFinal,
						is(referenciasColetadas));
//			}

//			if (this.aceitoExcecao) {
//				fail("Deveria lançar Excecao");
//			}

		} catch (Exception e) {
//			if (!this.aceitoExcecao) {
				System.out.println(input);
				e.printStackTrace();
//			}
		}

	}

	@Parameters
	public static List<Object[]> data() {
		ArrayList<Object[]> data = new ArrayList<Object[]>();
		data.addAll(UtilCaixaPreta.getEntradaGCTest());
		return data;
	}
}
