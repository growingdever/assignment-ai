/**
 * Created by loki on 2015. 12. 8..
 */
public class KnowledgeBase {

    private CNF cnf;

    public KnowledgeBase() {
        cnf = new CNF();
    }

    public void addClause(Clause clause) {
        clause.sort();
        cnf.add(clause);
    }

    public CNF getCNF() {
        return cnf;
    }

}
