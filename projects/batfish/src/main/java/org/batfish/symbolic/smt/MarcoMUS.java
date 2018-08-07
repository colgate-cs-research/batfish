package org.batfish.symbolic.smt;

import com.microsoft.z3.*;

import java.util.*;

public class MarcoMUS {

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

        BoolExpr[] constraints = new BoolExpr[]{c1, c2, c3, c4};
        //List of constraints in the test constraint system.

    }


    private static class SubsetSolver{
        private Context ctx;
        private BoolExpr[] constraints;
        private int n;
        private Solver solver;

        private Map<Integer,BoolExpr> varCache; //TODO : Decrypt this.
        private Map<Integer, Integer> idCache; //TODO : Decrypt this.

        SubsetSolver(BoolExpr[] constraints, Context ctx){
            this.constraints = constraints;
            this.n = constraints.length;
            varCache = new HashMap<>();
            idCache = new HashMap<>();
            solver = ctx.mkSolver();

            for (int i=0;i<n;i++){
                solver.add(ctx.mkImplies(getIndicatorVariable(i),constraints[i]));
            }
        }

        //c_var(self, i) in python.
        private BoolExpr getIndicatorVariable(int i){
            if (!varCache.containsKey(i)){
                BoolExpr v = ctx.mkBoolConst(constraints[i].toString());
                idCache.put(v.getId(), Math.abs(i));
                if (i>=0){
                    varCache.put(i, v);
                }else{
                    varCache.put(i, ctx.mkNot(v));
                }
            }
            return varCache.get(i);
        }

        private boolean checkSubset(Set<Integer> seed){
            List<Expr> assumptions = toIndicatorLiterals(seed);
            return (Status.SATISFIABLE ==
                    solver.check(assumptions.toArray(new Expr[assumptions.size()])));
        }

        //to_c_lits(self, seed) in python
        private List<Expr> toIndicatorLiterals(Set<Integer> seed){
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

        //Maximizing a satisfying subset
        private Set<Integer> grow(List<Integer> seed){
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
}
