package plp.orientadaObjetos1.memoria.gc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import plp.expressions2.expression.Id;
import plp.orientadaObjetos1.expressao.valor.Valor;
import plp.orientadaObjetos1.expressao.valor.ValorRef;
import plp.orientadaObjetos1.memoria.ContextoObjeto;
import plp.orientadaObjetos1.memoria.Objeto;

public class IncrementalGC implements GarbageColector {

	private LinkedList<ValorRef> brancas;
	
	private LinkedList<ValorRef> cinzas;
	
	private LinkedList<ValorRef> pretas;
	
	/**
	 * Referência para teste unitário
	 */
	private long contadorReferenciasColetadas = 0;

	private static ExecutorService executor;

	public IncrementalGC() {
		this.brancas = new LinkedList<ValorRef>();
		this.cinzas =  new LinkedList<ValorRef>();
		this.pretas = new LinkedList<ValorRef>();
	}

	public static ExecutorService getExecutorService() {
		if (executor == null) {
			executor = Executors.newFixedThreadPool(1);
		}
		return executor;

	}

	public synchronized long runGC(Stack<HashMap<Id, Valor>> pilha,
			HashMap<ValorRef, Objeto> mapObjetos) {

		// marca elementos

		while (!this.cinzas.isEmpty()) {
			marcaObjeto(mapObjetos);
		}

		// coleta os brancos
		long coletados = coletar(mapObjetos);

		restartWhites(mapObjetos);
		restartGreys(pilha);
		this.pretas.clear();
		System.gc();
		return coletados;
	}

	private long coletar(HashMap<ValorRef, Objeto> mapObjetos) {
		for (ValorRef ref : this.brancas) {
			mapObjetos.remove(ref);
		}
		long coletados = this.brancas.size();
		contadorReferenciasColetadas += coletados;
		return coletados;
	}

	/**
	 * Reinicia todas referências como em branco
	 * 
	 * @param mapObjetos
	 */
	private void restartWhites(HashMap<ValorRef, Objeto> mapObjetos) {
		this.brancas.clear();
		Set<ValorRef> keySet = mapObjetos.keySet();
		this.brancas.addAll(keySet);
	}

	/**
	 * Reinicia a lista de cinzas com os valores da raiz (pilha);
	 * 
	 * @param pilha
	 */
	private void restartGreys(Stack<HashMap<Id, Valor>> pilha) {
		LinkedList<ValorRef> todosValoresPilha = new LinkedList<ValorRef>();
		for (HashMap<Id, Valor> posicoesPilha : pilha) {

			for (Entry<Id, Valor> mapeamento : posicoesPilha.entrySet()) {

				Valor valor = mapeamento.getValue();
				if (valor instanceof ValorRef) {
					todosValoresPilha.add((ValorRef) valor);
				}
			}

		}
		this.cinzas.clear();
		this.cinzas.addAll(todosValoresPilha);
		this.brancas.removeAll(todosValoresPilha);
	}

	public synchronized long referenciasColetadas() {
		return contadorReferenciasColetadas;
	}

	public synchronized void writeBarrier(HashMap<ValorRef, Objeto> mapObjetos,
			Objeto objeto) {

	}

	/**
	 * A cada escrita, escreve o objeto como cinza, e caminha na marcação dos
	 * elementos.
	 * 
	 * @param mapObjetos
	 * @param ref
	 */
	public synchronized void writeBarrier(HashMap<ValorRef, Objeto> mapObjetos,
			ValorRef ref) {


		// caminha um objeto na marcação
		marcaObjeto(mapObjetos);

		// marca novo objeto como cinza
		if (!this.cinzas.contains(ref)) {
			this.brancas.remove(ref);
			this.cinzas.add(ref);
		}
	}

	private void marcaObjeto(HashMap<ValorRef, Objeto> mapObjetos) {
		if (!this.cinzas.isEmpty()) {
			ValorRef topGrey = this.cinzas.pop();
			Objeto objeto = mapObjetos.get(topGrey);
			ContextoObjeto estadoObjeto = objeto.getEstado();
			if (estadoObjeto != null) {
				Collection<Valor> valoresMapeados = estadoObjeto
						.getValoresMapeados();
				if (valoresMapeados != null) {
					for (Valor v : valoresMapeados) {
						if (v instanceof ValorRef) {
							this.brancas.remove(v);
							this.cinzas.add((ValorRef) v);
						}
					}
				}
			}

			this.pretas.push(topGrey);
		}
	}

}
