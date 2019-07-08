package heatequation;

import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.IntStream;

import fr.cea.nabla.javalib.Utils;
import fr.cea.nabla.javalib.types.*;
import fr.cea.nabla.javalib.mesh.*;

@SuppressWarnings("all")
public final class HeatEquation
{
	public final static class Options
	{
		public final double X_EDGE_LENGTH = 0.1;
		public final double Y_EDGE_LENGTH = X_EDGE_LENGTH;
		public final int X_EDGE_ELEMS = 20;
		public final int Y_EDGE_ELEMS = 20;
		public final int Z_EDGE_ELEMS = 1;
		public final double option_stoptime = 0.1;
		public final int option_max_iterations = 500;
		public final double PI = 3.1415926;
		public final double k = 1.0;
	}
	
	private final Options options;

	// Mesh
	private final NumericMesh2D mesh;
	private final int nbNodes, nbCells, nbFaces, nbNodesOfCell, nbNodesOfFace, nbNeighbourCells;
	private final VtkFileWriter2D writer;

	// Global Variables
	private double t, deltat, t_nplus1;

	// Array Variables
	private double[] X[];
	private double[] center[];
	private double u[];
	private double V[];
	private double f[];
	private double outgoingFlux[];
	private double surface[];
	private double u_nplus1[];
	
	public HeatEquation(Options aOptions, NumericMesh2D aNumericMesh2D)
	{
		options = aOptions;
		mesh = aNumericMesh2D;
		writer = new VtkFileWriter2D("HeatEquation");

		nbNodes = mesh.getNbNodes();
		nbCells = mesh.getNbCells();
		nbFaces = mesh.getNbFaces();
		nbNodesOfCell = NumericMesh2D.MaxNbNodesOfCell;
		nbNodesOfFace = NumericMesh2D.MaxNbNodesOfFace;
		nbNeighbourCells = NumericMesh2D.MaxNbNeighbourCells;

		t = 0.0;
		deltat = 0.001;
		t_nplus1 = 0.0;

		// Arrays allocation
		X = new double[nbNodes][2];
		center = new double[nbCells][2];
		u = new double[nbCells];
		V = new double[nbCells];
		f = new double[nbCells];
		outgoingFlux = new double[nbCells];
		surface = new double[nbFaces];
		u_nplus1 = new double[nbCells];

		// Copy node coordinates
		ArrayList<double[]> gNodes = mesh.getGeometricMesh().getNodes();
		IntStream.range(0, nbNodes).parallel().forEach(rNodes -> X[rNodes] = gNodes.get(rNodes));
	}
	
	/**
	 * Job IniF @-2.0
	 * In variables: 
	 * Out variables: f
	 */
	private void iniF() 
	{
		IntStream.range(0, nbCells).parallel().forEach(jCells -> 
		{
			f[jCells] = 0.0;
		});
	}		
	
	/**
	 * Job IniCenter @-2.0
	 * In variables: X
	 * Out variables: center
	 */
	private void iniCenter() 
	{
		IntStream.range(0, nbCells).parallel().forEach(jCells -> 
		{
			int jId = jCells;
			double[] reduceSum1389170290 = {0.0,0.0};
			{
				int[] nodesOfCellJ = mesh.getNodesOfCell(jId);
				for (int rNodesOfCellJ=0; rNodesOfCellJ<nodesOfCellJ.length; rNodesOfCellJ++)
				{
					int rId = nodesOfCellJ[rNodesOfCellJ];
					int rNodes = rId;
					reduceSum1389170290 = OperatorExtensions.operator_plus(reduceSum1389170290, (X[rNodes]));
				}
			}
			center[jCells] = OperatorExtensions.operator_multiply(0.25, reduceSum1389170290);
		});
	}		
	
	/**
	 * Job ComputeV @-2.0
	 * In variables: X
	 * Out variables: V
	 */
	private void computeV() 
	{
		IntStream.range(0, nbCells).parallel().forEach(jCells -> 
		{
			int jId = jCells;
			double reduceSum905788938 = 0.0;
			{
				int[] nodesOfCellJ = mesh.getNodesOfCell(jId);
				for (int rNodesOfCellJ=0; rNodesOfCellJ<nodesOfCellJ.length; rNodesOfCellJ++)
				{
					int rId = nodesOfCellJ[rNodesOfCellJ];
					int rPlus1Id = nodesOfCellJ[(rNodesOfCellJ+1+nbNodesOfCell)%nbNodesOfCell];
					int rNodes = rId;
					int rPlus1Nodes = rPlus1Id;
					reduceSum905788938 = reduceSum905788938 + (MathFunctions.det(X[rNodes], X[rPlus1Nodes]));
				}
			}
			V[jCells] = 0.5 * reduceSum905788938;
		});
	}		
	
	/**
	 * Job ComputeSurface @-2.0
	 * In variables: X
	 * Out variables: surface
	 */
	private void computeSurface() 
	{
		IntStream.range(0, nbFaces).parallel().forEach(fFaces -> 
		{
			int fId = fFaces;
			double reduceSum930431654 = 0.0;
			{
				int[] nodesOfFaceF = mesh.getNodesOfFace(fId);
				for (int rNodesOfFaceF=0; rNodesOfFaceF<nodesOfFaceF.length; rNodesOfFaceF++)
				{
					int rId = nodesOfFaceF[rNodesOfFaceF];
					int rPlus1Id = nodesOfFaceF[(rNodesOfFaceF+1+nbNodesOfFace)%nbNodesOfFace];
					int rNodes = rId;
					int rPlus1Nodes = rPlus1Id;
					reduceSum930431654 = reduceSum930431654 + (MathFunctions.norm(OperatorExtensions.operator_minus(X[rNodes], X[rPlus1Nodes])));
				}
			}
			surface[fFaces] = 0.5 * reduceSum930431654;
		});
	}		
	
	/**
	 * Job IniUn @-1.0
	 * In variables: PI, k, center
	 * Out variables: u
	 */
	private void iniUn() 
	{
		IntStream.range(0, nbCells).parallel().forEach(jCells -> 
		{
			u[jCells] = MathFunctions.cos(2 * options.PI * options.k * center[jCells][0]);
		});
	}		
	
	/**
	 * Job ComputeOutgoingFlux @1.0
	 * In variables: u, center, surface, deltat, V
	 * Out variables: outgoingFlux
	 */
	private void computeOutgoingFlux() 
	{
		IntStream.range(0, nbCells).parallel().forEach(j1Cells -> 
		{
			int j1Id = j1Cells;
			double reduceSum_1737136256 = 0.0;
			{
				int[] neighbourCellsJ1 = mesh.getNeighbourCells(j1Id);
				for (int j2NeighbourCellsJ1=0; j2NeighbourCellsJ1<neighbourCellsJ1.length; j2NeighbourCellsJ1++)
				{
					int j2Id = neighbourCellsJ1[j2NeighbourCellsJ1];
					int j2Cells = j2Id;
					int cfId = mesh.getCommonFace(j1Id, j2Id);
					int cfFaces = cfId;
					reduceSum_1737136256 = reduceSum_1737136256 + ((u[j2Cells] - u[j1Cells]) / MathFunctions.norm(OperatorExtensions.operator_minus(center[j2Cells], center[j1Cells])) * surface[cfFaces]);
				}
			}
			outgoingFlux[j1Cells] = deltat / V[j1Cells] * reduceSum_1737136256;
		});
	}		
	
	/**
	 * Job ComputeTn @1.0
	 * In variables: t, deltat
	 * Out variables: t_nplus1
	 */
	private void computeTn() 
	{
		t_nplus1 = t + deltat;
	}		
	
	/**
	 * Job Copy_t_nplus1_to_t @2.0
	 * In variables: t_nplus1
	 * Out variables: t
	 */
	private void copy_t_nplus1_to_t() 
	{
		double tmpSwitch = t;
		t = t_nplus1;
		t_nplus1 = tmpSwitch;
	}		
	
	/**
	 * Job ComputeUn @2.0
	 * In variables: f, deltat, u, outgoingFlux
	 * Out variables: u_nplus1
	 */
	private void computeUn() 
	{
		IntStream.range(0, nbCells).parallel().forEach(jCells -> 
		{
			u_nplus1[jCells] = f[jCells] * deltat + u[jCells] + outgoingFlux[jCells];
		});
	}		
	
	/**
	 * Job Copy_u_nplus1_to_u @3.0
	 * In variables: u_nplus1
	 * Out variables: u
	 */
	private void copy_u_nplus1_to_u() 
	{
		double[] tmpSwitch = u;
		u = u_nplus1;
		u_nplus1 = tmpSwitch;
	}		

	public void simulate()
	{
		System.out.println("Début de l'exécution du module HeatEquation");
		iniF(); // @-2.0
		iniCenter(); // @-2.0
		computeV(); // @-2.0
		computeSurface(); // @-2.0
		iniUn(); // @-1.0

		HashMap<String, double[]> cellVariables = new HashMap<String, double[]>();
		HashMap<String, double[]> nodeVariables = new HashMap<String, double[]>();
		cellVariables.put("Temperature", u);
		int iteration = 0;
		while (t < options.option_stoptime && iteration < options.option_max_iterations)
		{
			iteration++;
			System.out.println("[" + iteration + "] t = " + t);
			computeOutgoingFlux(); // @1.0
			computeTn(); // @1.0
			copy_t_nplus1_to_t(); // @2.0
			computeUn(); // @2.0
			copy_u_nplus1_to_u(); // @3.0
			writer.writeFile(iteration, X, mesh.getGeometricMesh().getQuads(), cellVariables, nodeVariables);
		}
		System.out.println("Fin de l'exécution du module HeatEquation");
	}

	public static void main(String[] args)
	{
		HeatEquation.Options o = new HeatEquation.Options();
		Mesh<double[]> gm = CartesianMesh2DGenerator.generate(o.X_EDGE_ELEMS, o.Y_EDGE_ELEMS, o.X_EDGE_LENGTH, o.Y_EDGE_LENGTH);
		NumericMesh2D nm = new NumericMesh2D(gm);
		HeatEquation i = new HeatEquation(o, nm);
		i.simulate();
	}
};