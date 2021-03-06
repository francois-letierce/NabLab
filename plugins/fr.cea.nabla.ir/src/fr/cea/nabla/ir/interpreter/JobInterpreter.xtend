/*******************************************************************************
 * Copyright (c) 2020 CEA
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors: see AUTHORS file
 *******************************************************************************/
package fr.cea.nabla.ir.interpreter

import fr.cea.nabla.ir.ir.ConnectivityVariable
import fr.cea.nabla.ir.ir.InstructionJob
import fr.cea.nabla.ir.ir.IrModule
import fr.cea.nabla.ir.ir.Job
import fr.cea.nabla.ir.ir.TimeLoop
import fr.cea.nabla.ir.ir.TimeLoopCopyJob
import fr.cea.nabla.ir.ir.TimeLoopJob
import fr.cea.nabla.javalib.mesh.PvdFileWriter2D
import fr.cea.nabla.javalib.mesh.VtkFileContent
import java.util.Arrays
import java.util.Locale

import static fr.cea.nabla.ir.interpreter.ExpressionInterpreter.*
import static fr.cea.nabla.ir.interpreter.InstructionInterpreter.*

import static extension fr.cea.nabla.ir.IrModuleExtensions.*
import static extension fr.cea.nabla.ir.JobExtensions.isTopLevel
import static extension fr.cea.nabla.ir.interpreter.NablaValueSetter.*

class JobInterpreter
{
	val PvdFileWriter2D writer

	new (PvdFileWriter2D writer)
	{
		this.writer = writer
	}

	// Switch to more efficient dispatch (also clearer for profiling)
	def void interprete(Job it, Context context)
	{
		if (it instanceof TimeLoopJob) {
			interpreteTimeLoopJob(context)
		} else if (it instanceof InstructionJob) {
			interpreteInstructionJob(context)
		} else if (it instanceof TimeLoopCopyJob) {
			interpreteTimeLoopCopyJob(context)
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: " +
				Arrays.<Object>asList(it, context).toString())
		}
	}

	def void interpreteInstructionJob(InstructionJob it, Context context)
	{
		context.logFiner("Interprete InstructionJob " + name + " @ " + at)
		val innerContext = new Context(context)
		interprete(instruction, innerContext)
	}

	def void interpreteTimeLoopJob(TimeLoopJob it, Context context)
	{
		context.logFiner("Interprete TimeLoopJob " + name + " @ " + at)
		val irModule = eContainer as IrModule
		val iterationVariable = timeLoop.iterationCounter
		val ppInfo = irModule.postProcessingInfo
		var iteration = 0
		context.logVariables("Before timeLoop " + timeLoop.name)
		context.addVariableValue(iterationVariable, new NV0Int(iteration))
		var continueLoop = true
		do
		{
			iteration ++
			context.setVariableValue(iterationVariable, new NV0Int(iteration))

			val log = String.format(Locale::ENGLISH, "%1$s [%2$d] t: %3$.5f - deltat: %4$.5f",
					timeLoop.indentation,
					iteration,
					context.getReal(irModule.timeVariable.name),
					context.getReal(irModule.deltatVariable.name))
			context.logFine(log)
			if (topLevel && ppInfo !== null)
			{
				val periodValue = context.getNumber(ppInfo.periodValue.name)
				val periodReference = context.getNumber(ppInfo.periodReference.name)
				val lastDump = context.getNumber(ppInfo.lastDumpVariable.name)
				if (periodReference >= lastDump + periodValue)
					dumpVariables(irModule, iteration, context, periodReference);
			}
			for (j : innerJobs.filter[x | x.at > 0].sortBy[at])
				interprete(j, context)
			context.logVariables("After iteration = " + iteration)

			continueLoop = (interprete(timeLoop.whileCondition, context) as NV0Bool).data

			if (continueLoop)
			{
				// Switch variables to prepare next iteration
				for (copy : copies)
				{
					val leftValue = context.getVariableValue(copy.destination)
					val rightValue = context.getVariableValue(copy.source)
					context.setVariableValue(copy.destination, rightValue)
					context.setVariableValue(copy.source, leftValue)
				}
			}
		}
		while (continueLoop)
		val log = String.format("%1$s Nb iteration %2$s = %3$d", timeLoop.indentation, timeLoop.name, iteration)
		context.logInfo(log)
		val msg = String.format("%1$s After timeLoop %2$s %3$d", timeLoop.indentation, timeLoop.name, iteration)
		context.logVariables(msg)
	}

	def void interpreteTimeLoopCopyJob(TimeLoopCopyJob it, Context context)
	{
		context.logFiner("Interprete TimeLoopCopyJob " + name + " @ " + at)

		for (copy : copies)
		{
			val sourceValue = context.getVariableValue(copy.source)
			val destinationValue = context.getVariableValue(copy.destination)
			destinationValue.setValue(#[], sourceValue)
		}
	}

	private def void dumpVariables(IrModule irModule, int iteration, Context context, double periodReference)
	{
		val ppInfo = irModule.postProcessingInfo
		if (!writer.disabled)
		{
			val time = context.getReal(irModule.timeVariable.name)
			val coordVariable = irModule.getVariableByName(irModule.nodeCoordVariable.name)
			val coord = (context.getVariableValue(coordVariable) as NV2Real).data
			val quads = context.meshWrapper.quads
			val vtkFileContent = new VtkFileContent(iteration, time, coord, quads);

			//TODO deal with linearAlgebra
			val connectivityVars = ppInfo.outputVariables.filter(ConnectivityVariable)
			for (v : connectivityVars.filter(v | v.type.connectivities.head.returnType.name == "cell"))
			{
				val value = context.getVariableValue(irModule.getVariableByName(v.name))
				switch value
				{
					NV1Real: vtkFileContent.addCellVariable(v.outputName, value.data)
					NV2Real: vtkFileContent.addCellVariable(v.outputName, value.data)
					default: throw new RuntimeException("Vtk writer not yet implemented for type: " + value.class.name)
				}
			}
			for (v : connectivityVars.filter(v | v.type.connectivities.head.returnType.name == "node"))
			{
				val value = context.getVariableValue(irModule.getVariableByName(v.name))
				switch value
				{
					NV1Real: vtkFileContent.addNodeVariable(v.outputName, value.data)
					NV2Real: vtkFileContent.addNodeVariable(v.outputName, value.data)
					default: throw new RuntimeException("Vtk writer not yet implemented for type: " + value.class.name)
				}
			}
			writer.writeFile(vtkFileContent)
			context.setVariableValue(ppInfo.lastDumpVariable, new NV0Real(periodReference))
		}
	}

	private static def String getIndentation(TimeLoop it)
	{
		if (container instanceof IrModule) ''
		else getIndentation(container as TimeLoop) + '\t\t'
	}
}
