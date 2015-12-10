import java.util.ArrayList;

/**
 * Created by loki on 2015. 12. 9..
 */
public class Util {
    public static ArrayList<Clause> cloneClauses(ArrayList<Clause> clauses) {
        ArrayList<Clause> cloneClauses = new ArrayList<>();
        for (Clause clause : clauses) {
            Clause cloneClause = new Clause(clause);
            cloneClauses.add(cloneClause);
        }

        return cloneClauses;
    }

    public static boolean existSameSortedClause(ArrayList<Clause> clauses, Clause targetClause) {
        for (Clause clause : clauses) {
            if (clause.equals(targetClause)) {
                return true;
            }
        }

        return false;
    }

}
