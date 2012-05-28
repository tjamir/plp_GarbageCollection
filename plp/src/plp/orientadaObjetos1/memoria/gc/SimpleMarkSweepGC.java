package plp.orientadaObjetos1.memoria.gc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;

import plp.expressions2.expression.Id;
import plp.orientadaObjetos1.expressao.valor.Valor;
import plp.orientadaObjetos1.expressao.valor.ValorRef;
import plp.orientadaObjetos1.memoria.ContextoObjeto;
import plp.orientadaObjetos1.memoria.Objeto;

public class SimpleMarkSweepGC implements GarbageColector {

	
	/**
	 * Referência para teste unitário
	 */
	public long contadorReferenciasColetadas = 0;
	
	public long runGC(Stack<HashMap<Id, Valor>> pilha,
			HashMap<ValorRef, Objeto> mapObjetos) {
		// mark
    	this.marcar(pilha, mapObjetos);
    	// sweep
    	long naoAlcancaveis = this.coletar(mapObjetos);
		return naoAlcancaveis;
	}
	
	private synchronized void marcar(Stack<HashMap<Id, Valor>> pilha,
			HashMap<ValorRef, Objeto> mapObjetos) {
		LinkedList<ValorRef> todosValoresMapeados = new LinkedList<ValorRef>();

		
		for (HashMap<Id, Valor> posicoesPilha : pilha) {

			for (Entry<Id, Valor> mapeamento : posicoesPilha.entrySet()) {

				Valor valor = mapeamento.getValue();
				if (valor instanceof ValorRef) {
					todosValoresMapeados.add((ValorRef) valor);
				}
			}

		}
		while (!todosValoresMapeados.isEmpty()) {
			ValorRef referencia = todosValoresMapeados.pop();
			Objeto objeto = mapObjetos.get(referencia);
			if (objeto != null) {
				if (!objeto.isMarked()) {
					objeto.setMarked(true);
					ContextoObjeto estadoObjeto = objeto.getEstado();
					if (estadoObjeto != null) {
						Collection<Valor> valoresMapeados = estadoObjeto
								.getValoresMapeados();
						if (valoresMapeados != null) {
							for (Valor v : valoresMapeados) {
								if (v instanceof ValorRef) {
									todosValoresMapeados.add((ValorRef) v);
								}
							}
						}
					}
				}
			}
		}
		
	}

	private synchronized long coletar(HashMap<ValorRef, Objeto> mapObjetos) {
		LinkedList<ValorRef> referencias = new LinkedList<ValorRef>();
		long naoAlcancaveisCount = 0;
		for(Entry<ValorRef,Objeto> entries : mapObjetos.entrySet()){
			ValorRef referencia = entries.getKey();
			Objeto objeto = entries.getValue();
			
			if(objeto.isMarked()){
				// limpando para o próximo GC
				objeto.setMarked(false); 
			}
			else{
				// não está marcado, não é alcançável
				referencias.add(referencia);
				naoAlcancaveisCount += 1;
			}
		}
		for(ValorRef referencia : referencias){
			mapObjetos.remove(referencia);
			contadorReferenciasColetadas++;
		}

		System.gc();
		
		return naoAlcancaveisCount;
	}

	public synchronized long referenciasColetadas(){
		return contadorReferenciasColetadas;
	}
	
}
