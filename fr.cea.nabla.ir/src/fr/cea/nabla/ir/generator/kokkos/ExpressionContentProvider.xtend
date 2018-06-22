package fr.cea.nabla.ir.generator.kokkos

import com.google.inject.Inject
import fr.cea.nabla.ir.generator.Utils
import fr.cea.nabla.ir.ir.ArrayVariable
import fr.cea.nabla.ir.ir.BinaryExpression
import fr.cea.nabla.ir.ir.BoolConstant
import fr.cea.nabla.ir.ir.FunctionCall
import fr.cea.nabla.ir.ir.IntConstant
import fr.cea.nabla.ir.ir.IteratorRange
import fr.cea.nabla.ir.ir.IteratorRef
import fr.cea.nabla.ir.ir.MaxConstant
import fr.cea.nabla.ir.ir.MinConstant
import fr.cea.nabla.ir.ir.Parenthesis
import fr.cea.nabla.ir.ir.Real2Constant
import fr.cea.nabla.ir.ir.Real2x2Constant
import fr.cea.nabla.ir.ir.Real3Constant
import fr.cea.nabla.ir.ir.Real3x3Constant
import fr.cea.nabla.ir.ir.RealConstant
import fr.cea.nabla.ir.ir.ScalarVariable
import fr.cea.nabla.ir.ir.UnaryExpression
import fr.cea.nabla.ir.ir.VarRef
import fr.cea.nabla.ir.ir.Variable
import java.util.ArrayList

import static extension fr.cea.nabla.ir.VariableExtensions.isScalarConst

class ExpressionContentProvider
{
	@Inject extension Utils
	@Inject extension Ir2KokkosUtils
	
	def dispatch CharSequence getContent(BinaryExpression it) '''�left.content� �operator� �right.content�'''
	def dispatch CharSequence getContent(UnaryExpression it) '''�operator��expression.content�'''
	def dispatch CharSequence getContent(Parenthesis it) '''(�expression.content�)'''
	def dispatch CharSequence getContent(IntConstant it) '''�value�'''
	def dispatch CharSequence getContent(RealConstant it) '''�value�'''
	def dispatch CharSequence getContent(Real2Constant it) '''Real2(�x�, �y�)'''
	def dispatch CharSequence getContent(Real3Constant it) '''Real3(�x�, �y�, �z�)'''
	def dispatch CharSequence getContent(Real2x2Constant it) '''Real2x2(�x.content�, �y.content�)'''
	def dispatch CharSequence getContent(Real3x3Constant it) '''Real3x3(�x.content�, �y.content�, �z.content�)'''
	def dispatch CharSequence getContent(BoolConstant it) '''�value�'''
	
	def dispatch CharSequence getContent(MinConstant it) 
	{
		switch getType().basicType
		{
			case INT  : '''Integer.MIN_VALUE'''
			case REAL : '''Double.MIN_VALUE'''
			case REAL2, case REAL2X2, case REAL3, case REAL3X3: '''new �getType().basicType�(Double.MIN_VALUE)'''
			default: throw new Exception('Invalid expression Min for type: ' + getType().basicType)
		}
	}

	def dispatch CharSequence getContent(MaxConstant it) 
	{
		switch getType().basicType
		{
			case INT  : '''Integer.MAX_VALUE'''
			case REAL : '''Double.MAX_VALUE'''
			case REAL2, case REAL2X2, case REAL3, case REAL3X3: '''new �getType().basicType�(Double.MAX_VALUE)'''
			default: throw new Exception('Invalid expression Max for type: ' + getType().basicType)
		}
	}

	def dispatch CharSequence getContent(FunctionCall it) 
	'''�function.provider�Functions::�function.name�(�FOR a:args SEPARATOR ', '��a.content��ENDFOR�)'''
	
	def dispatch CharSequence getContent(VarRef it) 
	'''�variable.codeName��iteratorsContent��FOR f:fields BEFORE '.' SEPARATOR '.'��f��ENDFOR�'''

	private def getCodeName(Variable it)
	{
		if (scalarConst) 'options->' + name
		else name
	}

	private def getIteratorsContent(VarRef it) 
	{ 
		if (iterators.empty || variable instanceof ScalarVariable) return ''
		val array = variable as ArrayVariable
		if (array.dimensions.size < iterators.size) return ''
		var content = new ArrayList<CharSequence>
		for (i : 0..<iterators.size)
		{
			val iter = iterators.get(i);
			switch iter
			{
				IteratorRange: content += iter.accessor
				IteratorRef: content += iter.getVarRefName(array, i)
			}
		}
		return '(' + content.join(',') + ')'
	}

	private def getVarRefName(IteratorRef i, ArrayVariable v, int iteratorIndex)
	{
		prefix(i, i.iterator.name + v.dimensions.get(iteratorIndex).name.toFirstUpper)
	}	
}