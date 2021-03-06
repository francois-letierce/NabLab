/*******************************************************************************
 * Copyright (c) 2020 CEA
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors: see AUTHORS file
 *******************************************************************************/
package fr.cea.nabla.tests.interpreter

import com.google.inject.Inject
import fr.cea.nabla.ir.interpreter.NV0Real
import fr.cea.nabla.tests.CompilationChainHelper
import fr.cea.nabla.tests.NablaInjectorProvider
import fr.cea.nabla.tests.TestUtils
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(NablaInjectorProvider)
class ModuleInterpreterTest extends AbstractModuleInterpreterTest
{
	@Inject CompilationChainHelper compilationHelper
	@Inject extension TestUtils

	override assertInterpreteModule(String model)
	{
		val irModule = compilationHelper.getIrModuleForInterpretation(model, testGenModel)
		val context = compilationHelper.getInterpreterContext(irModule, jsonDefaultContent)
		assertVariableValueInContext(irModule, context, "t_n", new NV0Real(0.2))
	}
}