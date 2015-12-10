import java.util.ArrayList;

/**
 * Created by loki on 2015. 12. 10..
 */
public class CNF {

    ArrayList<Clause> clauses;

    public CNF() {
        clauses = new ArrayList<>();
    }

    public CNF(CNF cnf) {
        clauses = new ArrayList<>();

        for (int i = 0; i < cnf.size(); i ++) {
            Clause clause = cnf.get(i);
            Clause cloneClause = new Clause(clause);
            this.add(cloneClause);
        }
    }

    public void add(Clause clause) {
        if (clauses.size() == 0) {
            clauses.add(clause);
            return;
        }

        int i;
        for (i = 0; i < clauses.size(); i ++) {
            if (clauses.get(i).compareTo(clause) > 0) {
                break;
            }
        }
        clauses.add(i, clause);
    }

    public void add(CNF cnf) {
        for (int i = 0; i < cnf.size(); i ++) {
            this.add(cnf.get(i));
        }
    }

    public Clause get(int i) {
        return clauses.get(i);
    }

    public int size() {
        return clauses.size();
    }

    public boolean exist(Clause targetClause) {
        for (Clause clause : clauses) {
            if (clause.equals(targetClause)) {
                return true;
            }
        }

        return false;
    }

    public static CNF negate(CNF cnf) {
        CNF result = new CNF();

        if (cnf.size() == 1) {
            for (int i = 0; i < cnf.size(); i ++) {
                Clause clause = cnf.get(i);
                for (int j = 0; j < clause.size(); j ++) {
                    PLWumpusWorldSymbol s = clause.get(j);

                    Clause new_clause = new Clause();
                    PLWumpusWorldSymbol clone = new PLWumpusWorldSymbol(s.type, s.x, s.y, !s.isNegation);
                    new_clause.add(clone);
                    result.add(new_clause);
                }
            }
        } else {
            Clause new_clause = new Clause();
            for (int i = 0; i < cnf.size(); i ++) {
                Clause clause = cnf.get(i);
                for (int j = 0; j < clause.size(); j ++) {
                    PLWumpusWorldSymbol s = clause.get(j);
                    PLWumpusWorldSymbol clone = new PLWumpusWorldSymbol(s.type, s.x, s.y, !s.isNegation);
                    new_clause.add(clone);
                }
            }
            result.add(new_clause);
        }

        return result;
    }

}
