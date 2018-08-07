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

    private static class MapSolver{
        private Solver solver;
        private int n;
        private Set<Integer> fullSet;
        private Context context;

        MapSolver(int n, Context ctx){
            this.context = ctx;
            this.solver = context.mkSolver();
            this.n = n;
            fullSet = new HashSet<>();
            for (int i = 0; i<n;i++){
                fullSet.add(i);
            }
        }

        private List<Integer> nextSeed(){
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
//                    seed.remove(Integer.parseInt(c.getName().toString()));
                    seed.remove(c.getId()); //TODO : Prevent hazard here
                }
            }
            for (FuncDecl f : model.getFuncDecls()){
                if (model.getConstInterp(f).isFalse()) {
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

        private void blockDown(Set<Integer> fromPoint){
            Set<Integer> comp = complement(fromPoint);
            List<BoolExpr> blockConstraints = new ArrayList<>();
            for (int i: comp){
                blockConstraints.add(context.mkBoolConst(Integer.toString(i)));
            }
            solver.add(context.mkOr(blockConstraints.toArray(new BoolExpr[blockConstraints.size()])));
        }

        private void blockUp(Set<Integer> fromPoint){
            List<BoolExpr> blockConstraints = new ArrayList<>();
            for (int i : fromPoint){
                blockConstraints.add(context.mkNot(context.mkBoolConst(Integer.toString(i))));
            }
            solver.add(context.mkOr(blockConstraints.toArray(new BoolExpr[blockConstraints.size()])));
        }
    }



}
