import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by loki on 2015. 12. 8..
 */
public class KnowledgeBase {

    private ArrayList<Clause> clauses;

    public KnowledgeBase() {
        clauses = new ArrayList<>();
    }

    public void addClause(Clause clause) {
        clause.sort();
        clauses.add(clause);
    }

    public ArrayList<Clause> getClauses() {
        return clauses;
    }

}
