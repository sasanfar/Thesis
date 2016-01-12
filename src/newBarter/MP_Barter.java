package newBarter;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

public class MP_Barter {

	IloCplex master;
	List<IloRange> constraints = new ArrayList<IloRange>();

	public void solve() throws IloException {
		// variables
		IloNumVar g[] = new IloNumVar[CG_Barter.outputSet.size()];
		IloNumExpr numExpr = master.numExpr();
		// objective

		// constraints

		// solve

	}

	public void solveMIP() {
		// variables

		// objective

		// constraints

		// solve

	}

}
