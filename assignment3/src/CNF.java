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

    public static CNF convertFromImply(CNF cnf1, CNF cnf2) {
        if (cnf1.onlyOneClause() && cnf2.onlyOneClause()) {
            // assume cnf1 is DNF and cnf2 is DNF
            CNF newCNF = new CNF();

            Clause clause1 = cnf1.get(0);
            Clause clause2 = cnf2.get(0);

            for (int i = 0; i < clause1.size(); i ++) {
                Clause newClause = new Clause();
                newClause.add(
                        new PLWumpusWorldSymbol(clause1.get(i), !clause1.get(i).isNegation)
                );
                for (int j = 0; j < clause2.size(); j ++) {
                    newClause.add(new PLWumpusWorldSymbol(clause2.get(j)));
                }
                newCNF.add(newClause);
            }

            return newCNF;
        } else if (cnf1.onlyOneClause() && !cnf2.onlyOneClause()) {
            // assume cnf1 is DNF and cnf2 is CNF
            CNF newCNF = new CNF();
            Clause clause1 = cnf1.get(0);

            for (int i = 0; i < clause1.size(); i ++) {
                for (int j = 0; j < cnf2.size(); j ++) {
                    Clause clause2 = cnf2.get(j);
                    for (int k = 0; k < clause2.size(); k ++) {
                        Clause newClause = new Clause();

                        PLWumpusWorldSymbol symbol1 = new PLWumpusWorldSymbol(clause1.get(i), !clause1.get(i).isNegation);
                        PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(clause2.get(k));

                        newClause.add(symbol1);
                        newClause.add(symbol2);

                        newCNF.add(newClause);
                    }
                }
            }

            return newCNF;
        } else if (!cnf1.onlyOneClause() && cnf2.onlyOneClause()) {
            CNF newCNF = new CNF();
            Clause newClause = new Clause();

            for (int i = 0; i < cnf1.size(); i ++) {
                Clause clause1 = cnf1.get(i);
                for (int j = 0; j < clause1.size(); j ++) {
                    newClause.add(new PLWumpusWorldSymbol(clause1.get(j), !clause1.get(j).isNegation));
                }
            }

            Clause clause2 = cnf2.get(0);
            for (int j = 0; j < clause2.size(); j ++) {
                newClause.add(new PLWumpusWorldSymbol(clause2.get(j)));
            }

            newCNF.add(newClause);
            return newCNF;
        } else {
            CNF newCNF = new CNF();

            for (int i = 0; i < cnf2.size(); i ++) {
                Clause clause2 = cnf2.get(i);

                Clause newClause = new Clause();
                newClause.add(clause2.get(0));

                for (int j = 0; j < cnf1.size(); j ++) {
                    Clause clause1 = cnf1.get(j);
                    newClause.add(new PLWumpusWorldSymbol(clause1.get(0), !clause1.get(0).isNegation));
                }

                newCNF.add(newClause);
            }

            return newCNF;
        }
    }

    public boolean onlyOneClause() {
        return this.size() == 1;
    }

}
