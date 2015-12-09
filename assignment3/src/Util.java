import java.util.ArrayList;

/**
 * Created by loki on 2015. 12. 9..
 */
public class Util {
    public static ArrayList<ArrayList<PLWumpusWorldSymbol>> cloneClauses(ArrayList<ArrayList<PLWumpusWorldSymbol>> clauses) {
        ArrayList< ArrayList<PLWumpusWorldSymbol> > cloneClauses = new ArrayList<>();
        for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
            ArrayList<PLWumpusWorldSymbol> cloneClause = new ArrayList<>();
            for (PLWumpusWorldSymbol symbol : clause) {
                cloneClause.add(new PLWumpusWorldSymbol(symbol));
            }
            cloneClauses.add(cloneClause);
        }

        return cloneClauses;
    }

    public static boolean existSymbolInClause(ArrayList<PLWumpusWorldSymbol> clause, PLWumpusWorldSymbol symbol) {
        for (PLWumpusWorldSymbol s : clause) {
            if (s.equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    public static boolean existSymbolInSortedClause(ArrayList<PLWumpusWorldSymbol> clause, PLWumpusWorldSymbol symbol) {
        // clause is increasementary sorted
        for (PLWumpusWorldSymbol s : clause) {
            if (s.equals(symbol)) {
                return true;
            }

            if (s.x > symbol.x) {
                break;
            } else if (s.x == symbol.x) {
                if (s.y > symbol.y) {

                }
            }
        }
        return false;
    }

    public static boolean existSameClause(ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses, ArrayList<PLWumpusWorldSymbol> targetClause) {
        for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
            if (Util.compareClause(clause, targetClause)) {
                return true;
            }
        }

        return false;
    }

    public static boolean existSameSortedClause(ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses, ArrayList<PLWumpusWorldSymbol> targetClause) {
        for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
            if (Util.compareSortedClause(clause, targetClause)) {
                return true;
            }
        }

        return false;
    }

    public static boolean compareClause(ArrayList<PLWumpusWorldSymbol> clause1, ArrayList<PLWumpusWorldSymbol> clause2) {
        if (clause1.size() != clause2.size()) {
            return false;
        }

        int i, j;
        for (i = 0; i < clause1.size(); i ++) {
            PLWumpusWorldSymbol symbol1 = clause1.get(i);
            boolean exist = false;
            for (j = 0; j < clause2.size(); j ++) {
                PLWumpusWorldSymbol symbol2 = clause2.get(i);
                if (symbol1.equals(symbol2)) {
                    exist = true;
                }
            }

            if (!exist) {
                return false;
            }
        }

        return true;
    }

    public static boolean compareSortedClause(ArrayList<PLWumpusWorldSymbol> clause1, ArrayList<PLWumpusWorldSymbol> clause2) {
        if (clause1.size() != clause2.size()) {
            return false;
        }

        for (int i = 0; i < clause1.size(); i++) {
            if (!clause1.get(i).equals(clause2.get(i))) {
                return false;
            }
        }

        return true;
    }


    public static void addAllExceptIndex(ArrayList<PLWumpusWorldSymbol> dest, ArrayList<PLWumpusWorldSymbol> src, int except) {
        for (int i = 0; i < src.size(); i ++) {
            if (i == except) {
                continue;
            }
            dest.add(src.get(i));
        }
    }

    public static void addAllExceptIndices(ArrayList<PLWumpusWorldSymbol> dest, ArrayList<PLWumpusWorldSymbol> src, int[] indices) {
        for (int i = 0; i < src.size(); i ++) {
            boolean except = false;
            for (int index : indices) {
                if (index == i) {
                    except = true;
                    break;
                }
            }

            if (except) {
                continue;
            }

            dest.add(src.get(i));
        }
    }

}
