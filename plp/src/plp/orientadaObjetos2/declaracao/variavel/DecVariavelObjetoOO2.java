package plp.orientadaObjetos2.declaracao.variavel;

import plp.expressions2.memory.VariavelJaDeclaradaException;
import plp.expressions2.memory.VariavelNaoDeclaradaException;
import plp.orientadaObjetos1.comando.ChamadaProcedimento;
import plp.orientadaObjetos1.comando.Procedimento;
import plp.orientadaObjetos1.declaracao.variavel.DecVariavelObjeto;
import plp.orientadaObjetos1.declaracao.variavel.SimplesDecVariavel;
import plp.orientadaObjetos1.excecao.declaracao.ClasseJaDeclaradaException;
import plp.orientadaObjetos1.excecao.declaracao.ClasseNaoDeclaradaException;
import plp.orientadaObjetos1.excecao.declaracao.ObjetoJaDeclaradoException;
import plp.orientadaObjetos1.excecao.declaracao.ObjetoNaoDeclaradoException;
import plp.orientadaObjetos1.excecao.declaracao.ProcedimentoNaoDeclaradoException;
import plp.orientadaObjetos1.expressao.ListaExpressao;
import plp.orientadaObjetos1.expressao.leftExpression.Id;
import plp.orientadaObjetos1.expressao.valor.ValorNull;
import plp.orientadaObjetos1.memoria.AmbienteCompilacaoOO1;
import plp.orientadaObjetos1.memoria.AmbienteExecucaoOO1;
import plp.orientadaObjetos1.util.Tipo;
import plp.orientadaObjetos2.comando.NewOO2;
import plp.orientadaObjetos2.declaracao.ConstrutorNaoDeclaradoException;
import plp.orientadaObjetos2.memoria.AmbienteExecucaoOO2;
import plp.orientadaObjetos2.memoria.DefClasseOO2;

public class DecVariavelObjetoOO2 extends DecVariavelObjeto {

	private ListaExpressao parametrosReais;

	public DecVariavelObjetoOO2(Tipo tipo, Id objeto, Id classe,
			ListaExpressao parametrosReais) {
		super(tipo, objeto, classe);
		this.parametrosReais = parametrosReais;
	}

	public AmbienteExecucaoOO1 elabora(AmbienteExecucaoOO1 ambiente)
			throws VariavelJaDeclaradaException, VariavelNaoDeclaradaException,
			ObjetoNaoDeclaradoException, ObjetoJaDeclaradoException,
			ClasseNaoDeclaradaException {

		AmbienteExecucaoOO2 aux = (AmbienteExecucaoOO2) new SimplesDecVariavel(
				getTipo(), getObjeto(), new ValorNull()).elabora(ambiente);

		try {
			aux = new NewOO2(getObjeto(), getClasse(), parametrosReais)
					.executar(aux);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aux;
	}

	public boolean checaTipo(AmbienteCompilacaoOO1 ambiente) throws VariavelJaDeclaradaException, VariavelNaoDeclaradaException, ClasseJaDeclaradaException, ClasseNaoDeclaradaException {

		boolean booleanSuper = super.checaTipo(ambiente);
		Tipo tipoClasse = getObjeto().getTipo(ambiente);
		DefClasseOO2 defClasse = (DefClasseOO2) ambiente.getDefClasse(tipoClasse.getTipo());
		
		Procedimento metodo = defClasse.getConstrutor().getProcedimento();
		
		boolean resposta = false;
		
		if (metodo != null) {
			try {
				ambiente.incrementa();
				resposta = new ChamadaProcedimento(metodo, parametrosReais).checaTipo(ambiente);
				ambiente.restaura();
			} catch (ProcedimentoNaoDeclaradoException e) {
				throw new RuntimeException("Construtor não declarado.");
			}
		}
		
		return booleanSuper && resposta;
	}
}
