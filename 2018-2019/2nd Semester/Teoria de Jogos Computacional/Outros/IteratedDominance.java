package lp;

import java.util.ArrayList;
import java.util.List;

import com.winvector.linalg.DenseVec;
import com.winvector.linalg.Matrix;
import com.winvector.linalg.colt.NativeMatrix;
import com.winvector.lp.LPEQProb;
import com.winvector.lp.LPException;
import com.winvector.lp.LPException.LPMalformedException;

class NormalFormProblem
{
List<String> rowMoves;
List<String> colMoves;
boolean[] pRow, pCol;
double[][] u1, u2;
}

public class IteratedDominance {
	
	public static void iteratedDominanceExample() throws LPException {
		NormalFormProblem problem = exampleNormalFormProblem();	
		showNormalFormProblem(problem);
		boolean reduced = solveIterativeDomination(problem);
		while (reduced) {
			showNormalFormProblem(problem);	
			reduced = solveIterativeDomination(problem);
		}
		showNormalFormProblem(problem);				
	}
		
	public static NormalFormProblem exampleNormalFormProblem(){
		NormalFormProblem problem = new NormalFormProblem();
		problem.rowMoves = new ArrayList<String>();
		problem.colMoves = new ArrayList<String>();	
		
	    problem.rowMoves.add("1");
	    problem.rowMoves.add("2");
	    problem.rowMoves.add("3");
	    problem.colMoves.add("1");
	    problem.colMoves.add("2");
	    problem.colMoves.add("3");
		boolean[] pRow = new boolean[3];
		for (int i=0;i<pRow.length; i++) pRow[i] = true;
		boolean[] pCol = new boolean[3];
		for (int j=0;j<pCol.length; j++) pCol[j] = true;
		double[][] u2 = new double[3][3];
		u2[0][0] = 1.0; u2[0][1] = 1.0; u2[0][2] = 0.0;
		u2[1][0] = 1.0; u2[1][1] = 1.0; u2[1][2] = 0.0;
		u2[2][0] = 1.0; u2[2][1] = 1.0; u2[2][2] = 0.0;
		double[][] u1 = new double[3][3];
		u1[0][0] = 3.0; u1[0][1] = 0.0; u1[0][2] = 0.0;
		u1[1][0] = 1.0; u1[1][1] = 1.0; u1[1][2] = 5.0;
		u1[2][0] = 0.0; u1[2][1] = 4.0; u1[2][2] = 0.0;
		
		problem.pCol = pCol;
		problem.pRow = pRow;
		problem.u1 = u1;
		problem.u2 = u2;
		
		return problem;
		
	}
	
	public static void showNormalFormProblem(NormalFormProblem problem) {
		System.out.print("****");
		for (int j = 0; j<problem.pCol.length; j++)  if (problem.pCol[j]) 
			System.out.print("***********");
		System.out.println();
		System.out.print("  ");
		for (int j = 0; j<problem.pCol.length; j++)  if (problem.pCol[j]) {
				if (problem.colMoves.size()>0) {
					System.out.print("      ");
					System.out.print(problem.colMoves.get(j));
					System.out.print("    ");
				}
				else {
					System.out.print("\t");
					System.out.print("Col " +j);
				}
		}
		System.out.println();
		for (int i = 0; i<problem.pRow.length; i++) if (problem.pRow[i]) {
			if (problem.rowMoves.size()>0) System.out.print(problem.rowMoves.get(i)+ ": ");
			else System.out.print("Row " +i+ ": ");
			for (int j = 0; j<problem.pCol.length; j++)  if (problem.pCol[j]) {
				String fs = String.format("| %3.0f,%3.0f", problem.u1[i][j], problem.u2[i][j]);
				System.out.print(fs+"  ");
			}
			System.out.println("|");
		}
		System.out.print("****");
		for (int j = 0; j<problem.pCol.length; j++)  if (problem.pCol[j]) 
			System.out.print("***********");
		System.out.println();
	}
	
	public static double[][] transpose(double[][] u) {
		int lin = u.length;
		int col = u[0].length;
		double[][] t = new double[col][lin];
		for (int i = 0; i<lin; i++)
			for (int j = 0; j<col; j++) t[j][i] = u[i][j];
		return t;
	}
	
	public static boolean solveIterativeDomination(NormalFormProblem problem) throws LPException {
		for (int j = 0; j<problem.pCol.length; j++) if (problem.pCol[j]) {
			if (dominatedColumn(j, problem.u2, problem.pRow, problem.pCol)) {
				System.out.println("The column " +j+ " is dominated!");
				problem.pCol[j]=false;
				return true;
			}
			else System.out.println("The column " +j+ " is not dominated!");
		}	
		double[][] t1 = transpose(problem.u1);
		for (int i = 0; i<problem.pRow.length; i++) if (problem.pRow[i]) {			
			if (dominatedColumn(i, t1, problem.pCol, problem.pRow)) {
				System.out.println("The row " +i+ " is dominated!");
				problem.pRow[i]=false;
				return true;
			}
			else System.out.println("The row " +i+ " is not dominated!");
		}
		return false;
	}
	
	public static boolean dominatedColumn(int jDom, double[][] u, boolean[] pRow, boolean[] pCol) throws LPException {
		
		return false;
	}
}




