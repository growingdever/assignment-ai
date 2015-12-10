import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by loki on 2015. 12. 10..
 */
public class Clause implements Comparable<Clause> {

    ArrayList<PLWumpusWorldSymbol> literals;

    public Clause() {
        literals = new ArrayList<>();
    }

    public Clause(Clause clause) {
        literals = new ArrayList<>();
        for (int i = 0; i < clause.size(); i ++) {
            literals.add(new PLWumpusWorldSymbol(clause.get(i)));
        }
    }

    public void sort() {
        Collections.sort(literals);
    }

    public void add(PLWumpusWorldSymbol literal) {
        if (literals.size() == 0) {
            literals.add(literal);
            return;
        }

        int i;
        for (i = 0; i < literals.size(); i ++) {
            if (literals.get(i).compareTo(literal) > 0) {
                break;
            }
        }
        literals.add(i, literal);
    }

    public void addAll(Clause c) {
        // TODO : refactoring
        for (int i = 0; i < c.size(); i ++) {
            this.add(c.get(i));
        }
    }

    public void addAllExceptIndex(Clause c, int except) {
        // TODO : refactoring
        for (int i = 0; i < c.size(); i ++) {
            if (i == except) {
                continue;
            }

            this.add(c.get(i));
        }
    }

    public void clear() {
        this.literals.clear();
    }

    public int size() {
        return literals.size();
    }

    public PLWumpusWorldSymbol get(int i) {
        return literals.get(i);
    }

    public boolean exist(PLWumpusWorldSymbol symbol) {
        for (PLWumpusWorldSymbol s : literals) {
            if (s.equals(symbol)) {
                return true;
            }

            if (s.compareTo(symbol) > 0) {
                break;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }

        if (!(object instanceof Clause)) {
            return false;
        }

        Clause clause = (Clause) object;
        if (this.size() != clause.size()) {
            return false;
        }

        int size = this.literals.size();
        for (int i = 0; i < size; i ++) {
            if (!this.literals.get(i).equals(clause.literals.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int compareTo(Clause o) {
        int result = this.size() - o.size();
        if (result == 0) {
            int size = this.size();
            int i = 0;
            while(result == 0 && i < size) {
                result = this.literals.get(i).compareTo(o.literals.get(i));
                i ++;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        if (this.literals.size() == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        PLWumpusWorldSymbol literal = this.literals.get(0);
        stringBuilder.append(literal.toString());

        for (int i = 1; i < literals.size(); i++) {
            stringBuilder.append(" OR ");

            literal = this.literals.get(i);
            stringBuilder.append(literal.toString());
        }

        return stringBuilder.toString();
    }

    public boolean isResolvable(Clause clause) {
        for (PLWumpusWorldSymbol symbol1 : this.literals) {
            for (int j = 0; j < clause.size(); j++) {
                PLWumpusWorldSymbol symbol2 = clause.get(j);
                if (symbol1.isOpposite(symbol2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static CNF resolve(Clause c1, Clause c2) {
        CNF result = new CNF();

        ArrayList<Integer> oppositeIndices = new ArrayList<>();
        for (int i = 0; i < c1.size(); i ++) {
            PLWumpusWorldSymbol symbol1 = c1.get(i);
            for (int j = 0; j < c2.size(); j ++) {
                PLWumpusWorldSymbol symbol2 = c2.get(j);
                if (symbol1.isOpposite(symbol2)) {
                    oppositeIndices.add(j);
                }
            }
        }

        if (oppositeIndices.size() == 0) {
            Clause new_clause = new Clause();
            new_clause.addAll(c1);
            new_clause.addAll(c2);

            result.add(new_clause);
        } else {
            for (Integer oppositeIndex : oppositeIndices) {
                Clause new_clause = new Clause();

                PLWumpusWorldSymbol oppositeSymbol = c2.get(oppositeIndex);
                for (int i = 0; i < c1.size(); i ++) {
                    PLWumpusWorldSymbol symbol = c1.get(i);

                    if (symbol.isSameTarget(oppositeSymbol) && symbol.isNegation != oppositeSymbol.isNegation) {
                        continue;
                    }

                    if (new_clause.exist(symbol)) {
                        continue;
                    }

                    new_clause.add(symbol);
                }

                new_clause.addAllExceptIndex(c2, oppositeIndex);

                if (new_clause.size() > c1.size() + c2.size() - 2 && new_clause.size() > 1) {
                    System.err.println("resolution error");
                    System.exit(1000);
                }

                // clear if this clause is tautology
                for (int i = 0; i < new_clause.size() - 1; i ++) {
                    if (new_clause.get(i).isSameTarget(new_clause.get(i + 1))) {
                        PLWumpusWorldSymbol symbol1 = new_clause.get(i);
                        PLWumpusWorldSymbol symbol2 = new_clause.get(i + 1);

                        new_clause.clear();
                        new_clause.add(symbol1);
                        new_clause.add(symbol2);
                        break;
                    }
                }

                if (!result.exist(new_clause)) {
                    result.add(new_clause);
                }
            }
        }

        return result;
    }

}
