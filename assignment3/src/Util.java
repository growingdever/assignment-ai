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
