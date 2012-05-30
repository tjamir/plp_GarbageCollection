package plp.orientadaObjetos1.memoria.gc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import plp.expressions2.expression.Id;
import plp.orientadaObjetos1.expressao.valor.Valor;
import plp.orientadaObjetos1.expressao.valor.ValorRef;
import plp.orientadaObjetos1.memoria.ContextoObjeto;
import plp.orientadaObjetos1.memoria.Objeto;

public class TriColorIncrementalGC implements GarbageColector {

	public enum Color {
		WHITE, GREY, BLACK
	};

	private Map<Objeto, Color> marcas;
	/**
	 * Referência para teste unitário
	 */
	public long contadorReferenciasColetadas = 0;
	
	private static ExecutorService executor;
	
	public static ExecutorService getExecutorService(){
		if(executor==null){
			executor=Executors.newFixedThreadPool(1);
		}
		return executor;
		
	}

	public long runGC(final Stack<HashMap<Id, Valor>> pilha,
			final HashMap<ValorRef, Objeto> mapObjetos) {
		// mark
		this.marcar(pilha, mapObjetos);
		// sweep
		long naoAlcancaveis = this.coletar(mapObjetos);
		
		class RunGC implements Callable<Long> {
			
			
			public Long call() {
				// mark
				marcar(pilha, mapObjetos);
				// sweep
				return coletar(mapObjetos);
				
			}
		};
		
		Future<Long> resultado=getExecutorService().submit(new RunGC());
		
		try {
			naoAlcancaveis=resultado.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		return naoAlcancaveis;
	}

	private void init(HashMap<ValorRef, Objeto> mapObjetos) {
		marcas = new HashMap<Objeto, TriColorIncrementalGC.Color>();
		for (Entry<ValorRef, Objeto> entries : mapObjetos.entrySet()) {

			marcas.put(entries.getValue(), Color.WHITE);
		}
	}

	private synchronized void marcar(Stack<HashMap<Id, Valor>> pilha,
			HashMap<ValorRef, Objeto> mapObjetos) {
		init(mapObjetos);
		LinkedList<ValorRef> todosValoresMapeados = new LinkedList<ValorRef>();

		for (HashMap<Id, Valor> posicoesPilha : pilha) {

			for (Entry<Id, Valor> mapeamento : posicoesPilha.entrySet()) {

				Valor valor = mapeamento.getValue();
				if (valor instanceof ValorRef) {
					ValorRef valorRef = (ValorRef) valor;
					todosValoresMapeados.add((ValorRef) valorRef);
					marcas.put(mapObjetos.get(valorRef), Color.GREY);
					// Objeto pecorrido no grafo, mas seus filhos ainda não
				}
			}

		}
		while (!todosValoresMapeados.isEmpty()) {
			ValorRef referencia = todosValoresMapeados.pop();
			Objeto objeto = mapObjetos.get(referencia);
			if (objeto != null) {
				if (marcas.get(objeto) != Color.BLACK) {
					todosValoresMapeados.addAll(pecorrerObjeto(mapObjetos,
							objeto));
					// Todos os filhos foram identificados
					marcas.put(objeto, Color.BLACK);
				}
			}
		}

	}

	public void writeBarrier(HashMap<ValorRef, Objeto> mapObjetos, Objeto objeto) {

		if (marcas!=null&&marcas.containsKey(objeto) && marcas.get(objeto) == Color.WHITE) {
			LinkedList<ValorRef> lista = new LinkedList<ValorRef>();

			do {
				lista.addAll(pecorrerObjeto(mapObjetos, objeto));
			} while (!lista.isEmpty());
		}

	}

	private LinkedList<ValorRef> pecorrerObjeto(
			HashMap<ValorRef, Objeto> mapObjetos, Objeto objeto) {
		LinkedList<ValorRef> lista = new LinkedList<ValorRef>();
		// Isso pode acontecer em caso de write barrier
		if (marcas.get(objeto) == Color.WHITE) {
			marcas.put(objeto, Color.WHITE);
		}
		ContextoObjeto estadoObjeto = objeto.getEstado();
		if (estadoObjeto != null) {
			Collection<Valor> valoresMapeados = estadoObjeto
					.getValoresMapeados();
			if (valoresMapeados != null) {
				for (Valor v : valoresMapeados) {
					if (v instanceof ValorRef) {

						lista.add((ValorRef) v);
						Objeto o = mapObjetos.get(v);
						if (marcas.get(o) == Color.WHITE) {
							marcas.put(mapObjetos.get(v), Color.GREY);
						}
					}
				}
			}
		}
		return lista;
	}

	private synchronized long coletar(HashMap<ValorRef, Objeto> mapObjetos) {
		LinkedList<ValorRef> referencias = new LinkedList<ValorRef>();
		long naoAlcancaveisCount = 0;
		for (Entry<ValorRef, Objeto> entries : mapObjetos.entrySet()) {
			ValorRef referencia = entries.getKey();
			Objeto objeto = entries.getValue();

			if (marcas.containsKey(objeto) && marcas.get(objeto) == Color.WHITE) {
				// está marcado como branco
				referencias.add(referencia);
				naoAlcancaveisCount += 1;
			}
		}
		for (ValorRef referencia : referencias) {
			mapObjetos.remove(referencia);
			contadorReferenciasColetadas++;
		}

		System.gc();

		return naoAlcancaveisCount;
	}

	public synchronized long referenciasColetadas() {
		return contadorReferenciasColetadas;
	}

}
