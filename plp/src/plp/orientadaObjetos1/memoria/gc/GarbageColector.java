package plp.orientadaObjetos1.memoria.gc;

import java.util.HashMap;
import java.util.Stack;

import plp.expressions2.expression.Id;
import plp.orientadaObjetos1.expressao.valor.Valor;
import plp.orientadaObjetos1.expressao.valor.ValorRef;
import plp.orientadaObjetos1.memoria.Objeto;

public interface GarbageColector {
	
	
	public void marcar(Stack<HashMap<Id, Valor>> pilha, HashMap<ValorRef, Objeto> mapObjetos);
	
	public void coletar(HashMap<ValorRef, Objeto> mapObjetos);

}
