package org.batfish.symbolic.smt;

import com.microsoft.z3.*;

import java.util.*;

/**
 * Implementation of algorithm for enumerating multiple MUSes/MSSes of an unsatisfiable
 * constraint system as described in
 * @see <a href="https://sun.iwu.edu/~mliffito/publications/cpaior13_liffiton_MARCO.pdf">this paper</a> and demonstrated
 * in <a href ="https://github.com/Z3Prover/z3/blob/master/examples/python/mus/marco.py"> this example</a>
 */

public class MarcoMUS {


    private static final int MAX_MUS_COUNT = 5;
    private static final int MAX_MSS_COUNT = 500;
	private static final int MAX_EXPLORATION_TIME = 10 * 1000;

	/**
     * Demo.
     */
    public static void testMarco(){
        System.out.println("Testing Marco");
        Context ctx = new Context();

        BoolExpr a = ctx.mkBoolConst("a");
        BoolExpr b = ctx.mkBoolConst("b");

        //Making constraint system from example 1 of the first marco paper.
        BoolExpr c1 = a;
        BoolExpr c2 = ctx.mkOr(ctx.mkNot(a), b);
        BoolExpr c3 = ctx.mkNot(b);
        BoolExpr c4 = ctx.mkNot(a);

        //List of constraints in the test constraint system.
        BoolExpr[] constraints = new BoolExpr[]{c1, c2, c3, c4};

        enumerate(constraints,ctx, MAX_MUS_COUNT, MAX_MSS_COUNT,true);

    }

    /**
     * Core algorithm to enumerate MUSes/MSSes. Note: MUSes/MSSes are yielded
     * as they are generated in the original algorithm (and sample implementation in python)
     * @param constraints Set of constraints in the infeasible constraints system
     * @param ctx Context object required for creating MapSolver and SubsetSolver objects.
     * @param shouldReturnMUSes true, if MUSes are to be returned.
     * @return List of MUSes of the unsatisfiable constraint system.
     */
    public static List<Set<Integer>> enumerate(BoolExpr[] constraints, Context ctx,
                                               int maxMUSCount,
                                               int maxMSSCount,
                                               boolean shouldReturnMUSes){
        SubsetSolver subsetSolver = new SubsetSolver(constraints, ctx);
        MapSolver mapSolver = new MapSolver(constraints.length, ctx);

        List<List<Expr>> MSSes = new ArrayList<>();
        List<List<Expr>> MUSes = new ArrayList<>();

        List<Set<Integer>> indexOfMUSes = new ArrayList<>();
        List<Set<Integer>> indexOfMSSes = new ArrayList<>();

		long start_time = System.currentTimeMillis();
        while (true){
            List<Integer> seed  = mapSolver.nextSeed();
            if (seed==null) {
                break;
            }
            if (MSSes.size()>=maxMSSCount || MUSes.size()>=maxMUSCount) {
                break;
            }
            if (System.currentTimeMillis()-start_time > MAX_EXPLORATION_TIME) {
                break;
            }

            Set<Integer> seedSet= new HashSet<>();
            seedSet.addAll(seed);
            if (subsetSolver.checkSubset(seedSet)){
                Set<Integer> mss = subsetSolver.grow(seed);
                indexOfMSSes.add(mss);
                List<Expr> mssLits = subsetSolver.toIndicatorLiterals(mss);
                MSSes.add(mssLits);
                mapSolver.blockDown(mss);
            }else{
                Set<Integer> mus = subsetSolver.shrink(seed);
                indexOfMUSes.add(mus);
                List<Expr> musLits = subsetSolver.toIndicatorLiterals(mus);
                MUSes.add(musLits);
                mapSolver.blockUp(mus);
            }
        }


        return shouldReturnMUSes? indexOfMUSes: indexOfMSSes;
    }

    /**
     * Helper Class for MARCO for growing satisfying subsets to MSSes and shrinking
     * unsatisfiable subsets to MUSes.
     */
    public static class SubsetSolver{
        private Context ctx;
        private BoolExpr[] constraints;
        private int n;
        private Solver solver;

        private Map<Integer,BoolExpr> varCache; //TODO : Decrypt this.
        private Map<Integer, Integer> idCache; //TODO : Decrypt this.

        /**
         * Constructor
         * @param constraints Set of all constraints.
         * @param ctx Z3 Context object
         */
        public SubsetSolver(BoolExpr[] constraints, Context ctx){
            this.constraints = constraints;
            this.n = constraints.length;
            varCache = new HashMap<>();
            idCache = new HashMap<>();
            solver = ctx.mkSolver();
            this.ctx = ctx;
            for (int i=0;i<n;i++){
                //Add unique indicator variable for each constraint.
                solver.add(ctx.mkImplies(getIndicatorVariable(i),constraints[i]));
            }
        }

        private BoolExpr getIndicatorVariable(int i){
            if (!varCache.containsKey(i)){
                BoolExpr v = ctx.mkBoolConst(constraints[Math.abs(i)].toString());
                idCache.put(v.getId(), Math.abs(i));
                if (i>=0){
                    varCache.put(i, v);
                }else{
                    varCache.put(i, ctx.mkNot(v));
                }
            }
            return varCache.get(i);
        }

        /**
         * Check if a subset of assertions is satisfiable.
         * @param seed Set of indexes of assertions that are to be checked for satisfiability
         * @return True if subset satisfiable.
         */
        public boolean checkSubset(Set<Integer> seed){
            List<Expr> assumptions = toIndicatorLiterals(seed);
            return (Status.SATISFIABLE ==
                    solver.check(assumptions.toArray(new Expr[assumptions.size()])));
        }

        //to_c_lits(self, seed) in python
        public List<Expr> toIndicatorLiterals(Set<Integer> seed){
            List<Expr> retList = new ArrayList<>();
            for (int i: seed){
                retList.add(getIndicatorVariable(i));
            }
            return retList;
        }

        private Set<Integer> complement(Set<Integer> set){
            Set<Integer> retSet = new HashSet<>();
            for (int i = 0;i<n;i++){
                if (!set.contains(i)){
                    retSet.add(i);
                }
            }
            return retSet;
        }

        private List<Integer> seedFromUnsatCore(){
            BoolExpr[] unsatCore = solver.getUnsatCore();
            List<Integer> retList = new ArrayList<>();
            //getting indicator variables in the unsat core.
            for (BoolExpr expr: unsatCore){
                retList.add(idCache.get(expr.getId()));
            }
            return retList;
        }


        /**
         * Minimize an unsatisfying subset of constraints into a MUS.
         * @param seed List of indexes of the constraints in the unsatisfiable subset.
         * @return MUS obtained by minimizing input unsatisfying subset.
         */
        private Set<Integer> shrink(List<Integer> seed){
            Set<Integer> current = new HashSet<>(seed);
            for (int i:seed){
                if (!current.contains(i)){
                    continue;
                }
                current.remove(i);
                if (!checkSubset(current)){
                    current = new HashSet<>(seedFromUnsatCore());
                }else{
                    current.add(i);
                }
            }
            return current;
        }

        /**
         * Maximize a satisfying subset of constraints into a MSS.
         * @param seed List of indexes of the constraints in the satisfiable subset.
         * @return MSS obtained by maximizing input satisfying subset.
         */
        public Set<Integer> grow(List<Integer> seed){
            Set<Integer> current = new HashSet<>(seed);
            Set<Integer> currentComplement = complement(current);
            for (int i: currentComplement){
                current.add(i);
                if (!checkSubset(current)){
                    current.remove(i);
                }
            }
            return current;
        }

    }

    public static void main(String[] args){
        System.out.println("Running MarcoMUS");
        testMarco();
    }

    /**
     * Helper class to explore the power set (from the set of constraints)
     * to find new seeds that are not supersets of discovered unsatisfiable subsets
     * or subsets of discovered satisfiable subsets.
     */
    public static class MapSolver{
        private Solver solver;
        private int n;
        private Set<Integer> fullSet;
        private Context context;

        /**
         * Constructor for MapSolver
         * @param n Number of constraints in the infeasible constraint system.
         * @param ctx Z3 context object.
         */
        public MapSolver(int n, Context ctx){
            this.context = ctx;
            this.solver = context.mkSolver();
            this.n = n;
            fullSet = new HashSet<>();
            for (int i = 0; i<n;i++){
                fullSet.add(i);
            }
        }

        /**
         * Generate a new subset of the constraints for MSS/MUS generation.
         * @return List of indexes corresponding to constraints in the new seed.
         */
        public List<Integer> nextSeed(){
            if (solver.check()==Status.UNSATISFIABLE){
                return null; //TODO: Ensure this works or return an empty list.
            }
            Set<Integer> seed = new HashSet<>();
            seed.addAll(fullSet);

            Model model = solver.getModel();
            //the first `x` are consts, followed by FuncDecls
            /**
             * if _is_int(idx):
                if idx >= len(self):
                     raise IndexError
                     num_consts = Z3_model_get_num_consts(self.ctx.ref(), self.model)
                     if (idx < num_consts):
                        return FuncDeclRef(Z3_model_get_const_decl(self.ctx.ref(), self.model, idx), self.ctx)
                     else:
                        return FuncDeclRef(Z3_model_get_func_decl(self.ctx.ref(), self.model, idx - num_consts), self.ctx)
             */
            for (FuncDecl c :  model.getConstDecls()){
                if (model.getConstInterp(c).isFalse()){
                    seed.remove(Integer.parseInt(c.getName().toString()));
//                    seed.remove(c.getId()); //TODO : Prevent hazard here
                }
            }
            for (FuncDecl f : model.getFuncDecls()){
                if (model.getConstInterp(f).isFalse()) {
                    seed.remove(Integer.parseInt(f.getName().toString()));
                    seed.remove(f.getId());
                }
            }

            return new ArrayList<>(seed);
        }

        private Set<Integer> complement(Set<Integer> set){
            Set<Integer> retSet = new HashSet<Integer>();
            retSet.addAll(fullSet);
            retSet.removeAll(set);
            return retSet;
        }

        /**
         * Ensure subsets of existing MSSes are not explored again.
         * @param fromPoint An MSS
         */
        public void blockDown(Set<Integer> fromPoint){
            Set<Integer> comp = complement(fromPoint);
            List<BoolExpr> blockConstraints = new ArrayList<>();
            for (int i: comp){
                blockConstraints.add(context.mkBoolConst(Integer.toString(i)));
            }
            solver.add(context.mkOr(blockConstraints.toArray(new BoolExpr[blockConstraints.size()])));
        }

        /**
         * Ensure supersets of existing MUSes are not explored again.
         * @param fromPoint An MUS.
         */
        public void blockUp(Set<Integer> fromPoint){
            List<BoolExpr> blockConstraints = new ArrayList<>();
            for (int i : fromPoint){
                blockConstraints.add(context.mkNot(context.mkBoolConst(Integer.toString(i))));
            }
            solver.add(context.mkOr(blockConstraints.toArray(new BoolExpr[blockConstraints.size()])));
        }
    }



}
