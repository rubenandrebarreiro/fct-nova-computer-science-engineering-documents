package lp;

import org.apache.commons.math3.optimization.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optimization.linear.UnboundedSolutionException;

import com.winvector.linalg.DenseVec;
import com.winvector.linalg.Matrix;
import com.winvector.linalg.colt.NativeMatrix;
import com.winvector.lp.LPEQProb;
import com.winvector.lp.LPException;
import com.winvector.lp.LPSoln;
import com.winvector.lp.LPException.LPInfeasibleException;
import com.winvector.lp.LPException.LPMalformedException;
import com.winvector.lp.LPException.LPUnboundedException;
import com.winvector.lp.apachem3.M3Solver;

class LinearProgrammingProblem
{
	int numVariables;
	int numConstraints;
	boolean feasible;
	boolean bounded;
	Matrix<NativeMatrix> m; 
	double[] b; 
	double[] c;
	LPEQProb prob;
	LPSoln solution;
}

public class LinearProgramming {
	
	public static void linearProgrammingExample() throws LPException {	
		LinearProgrammingProblem problem = setProblem();
		showProblem(problem);
		solveProblem(problem);
		if (problem.feasible) {
			System.out.println("The problem is feasible!");
			if (problem.bounded) {
				System.out.println("The problem is bounded!");
				showSolution(problem);
			}
			else System.out.println("The problem is unbounded!");
		}
		else System.out.println("The problem is infeasible!");
	}
	
	public static LinearProgrammingProblem setProblem() {
		LinearProgrammingProblem problem = new LinearProgrammingProblem();	
		
		problem.numVariables = 2;   // number of variables
		problem.numConstraints = 4; // number of constraints
		final Matrix<NativeMatrix> m = NativeMatrix.factory.newMatrix(problem.numConstraints,problem.numVariables+problem.numConstraints,false);
		final double[] b = new double[problem.numConstraints];
		final double[] c = new double[problem.numVariables+problem.numConstraints];
		m.set(0,0,7.0);  m.set(0,1,11.0); m.set(0,2,1.0); b[0] = 77.0;  //  7*x1 + 11*x2 + s1 = 77
		m.set(1,0,10.0); m.set(1,1,8.0);  m.set(1,3,1.0); b[1] = 80.0;  // 10*x1 +  8*x2 + s2 = 80
		m.set(2,0,1.0);  m.set(2,1,0.0);  m.set(2,4,1.0); b[2] = 9.0;   //    x1         + s3 =  9
		m.set(3,0,0.0);  m.set(3,1,1.0);  m.set(3,5,1.0); b[3] = 6.0;   //            x2 + s4 =  6
		c[0] = -150.0; c[1] = -175.0;                                   // minimize -150*x1 - 175*x2	
		
		
		problem.m = m;
		problem.b = b;
		problem.c = c;
		problem.prob = null;
		try {
			problem.prob = new LPEQProb(m.columnMatrix(),b,new DenseVec(c));
		}
		catch (LPMalformedException e) {
			System.out.println("Error in problem specification!");
			e.printStackTrace();
		}
		return problem;
	}
	
	public static void showProblem(LinearProgrammingProblem problem) {
		System.out.println("********************PROBLEM********************");
		System.out.print("  minimize: "); 
		for(int i=0;i<problem.numVariables;i++)	if (problem.c[i] != 0) 
			if (problem.c[i] > 0) System.out.print("+" + problem.c[i] + "*x["+i+"] "); 
			else System.out.print(problem.c[i] + "*x["+i+"] "); 
		for(int i=problem.numVariables;i<problem.c.length;i++)	if (problem.c[i] != 0) 
			if (problem.c[i] > 0) System.out.print("+" + problem.c[i] + "*s["+(i-problem.numVariables)+"] "); 
			else System.out.print(problem.c[i] + "*s["+(i-problem.numVariables)+"] "); 
		System.out.println("");
		System.out.print("subject to: "); 
		double aij;
		for(int lin=0;lin<problem.numConstraints;lin++) {
			if (lin != 0) System.out.print("            ");
			for(int col=0;col<problem.numVariables+problem.numConstraints;col++){
				aij = problem.m.get(lin, col);
				if (aij != 0) {
					if (aij>0) System.out.print("+");
					if (aij==-1)  System.out.print("-");
					else if (aij!=1) System.out.print(aij+"*"); 
					if (col<problem.numVariables) System.out.print("x["+col+"] ");
					else System.out.print("s["+(col-problem.numVariables)+"] ");
				}
			}	
			System.out.println("= "+problem.b[lin]);
		}
		System.out.println("***********************************************");
	}
	
	public static void solveProblem(LinearProgrammingProblem problem)  throws LPException {
		problem.solution = null;
		//LPSolver solver = new RevisedSimplexSolver();
		final M3Solver solver = new M3Solver();
		problem.feasible = true;
		problem.bounded = true;
		try {
			problem.solution = solver.solve(problem.prob, null, 1.0e-6, 1000, NativeMatrix.factory);
		} catch (NoFeasibleSolutionException e) {
			problem.feasible = false;
		} catch (UnboundedSolutionException e) {
			problem.bounded = false;
		} catch (LPUnboundedException e) {
			problem.bounded = false;
		} catch (LPInfeasibleException e) {
			problem.feasible = false;
		}
	}
	
	public static void showSolution(LinearProgrammingProblem problem) {
		final double[] x = getSolution(problem);
		System.out.println("********************SOLUTION*******************");
		for(int i=0;i<problem.numVariables;i++)System.out.println("x["+i+"] = "+ x[i]); 
		System.out.println("*********************SLACKS********************");
		final double[] s = getSlacks(problem);
		for(int i=0;i<problem.numConstraints;i++)System.out.println("s["+i+"] = "+ s[i]); 	
		System.out.println("***********************************************");		
	}
	
	public static double[] getSolution(LinearProgrammingProblem problem) {
		final double[] x = new double[problem.numVariables];
		for(int i=0;i<problem.solution.primalSolution.nIndices();i++) {
			if (problem.solution.primalSolution.index(i)<problem.numVariables) {
				x[problem.solution.primalSolution.index(i)]=problem.solution.primalSolution.value(i);
			}
		}
		return x;
	}
	
	public static double[] getSlacks(LinearProgrammingProblem problem) {
		final double[] s = new double[problem.numConstraints];
		for(int i=0;i<problem.solution.primalSolution.nIndices();i++) {
			if (problem.solution.primalSolution.index(i)>=problem.numVariables) {
				s[problem.solution.primalSolution.index(i)-problem.numVariables]=problem.solution.primalSolution.value(i);
			}
		}
		return s;
	}

}


