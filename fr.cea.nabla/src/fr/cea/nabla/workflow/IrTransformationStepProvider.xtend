package fr.cea.nabla.workflow

import fr.cea.nabla.ir.transformers.FillJobHLTs
import fr.cea.nabla.ir.transformers.OptimizeConnectivities
import fr.cea.nabla.ir.transformers.ReplaceReductions
import fr.cea.nabla.ir.transformers.ReplaceUtf8Chars
import fr.cea.nabla.ir.transformers.TagPersistentVariables
import fr.cea.nabla.nablagen.FillHLTsComponent
import fr.cea.nabla.nablagen.Iteration
import fr.cea.nabla.nablagen.OptimizeConnectivitiesComponent
import fr.cea.nabla.nablagen.ReplaceInternalReductionsComponent
import fr.cea.nabla.nablagen.ReplaceUtfComponent
import fr.cea.nabla.nablagen.TagPersistentVariablesComponent
import fr.cea.nabla.nablagen.TimeStep
import java.util.ArrayList
import java.util.HashMap

class IrTransformationStepProvider 
{
	// Dispatch on Ir2IrComponent
	static def dispatch get(TagPersistentVariablesComponent it)
	{
		val outVars = new HashMap<String, String>
		vars.forEach[outVars.put(varRef.name, varName)]
		val tpv = new TagPersistentVariables(outVars)
		val p = period
		switch p
		{
			Iteration : tpv.iterationPeriod = p.value
			TimeStep : tpv.timeStep = p.value
		}
		return tpv
	} 
		
	static def dispatch get(ReplaceUtfComponent it)
	{
		new ReplaceUtf8Chars
	} 
	
	static def dispatch get(ReplaceInternalReductionsComponent it)
	{
		new ReplaceReductions
	} 
	
	static def dispatch get(OptimizeConnectivitiesComponent it)
	{
		val c = new ArrayList<String>
		c.addAll(connectivities.map[name])
		new OptimizeConnectivities(c)
	}
	
	static def dispatch get(FillHLTsComponent it)
	{
		new FillJobHLTs
	} 
}