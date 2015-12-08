import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by loki on 2015. 12. 8..
 */
public class KnowledgeBase {

    private ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses;

    public KnowledgeBase() {
        clauses = new ArrayList<>();
    }

    public void addClause(ArrayList<PLWumpusWorldSymbol> clause) {
        Collections.sort(clause);
        clauses.add(clause);
    }

    public ArrayList< ArrayList<PLWumpusWorldSymbol> > getClauses() {
        return clauses;
    }

    public ArrayList<ArrayList<PLWumpusWorldSymbol>> getClausesClone() {
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
}
