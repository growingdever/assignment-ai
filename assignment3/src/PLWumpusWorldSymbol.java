/**
 * Created by loki on 2015. 12. 8..
 */
public class PLWumpusWorldSymbol implements Comparable<PLWumpusWorldSymbol> {
    @Override
    public int compareTo(PLWumpusWorldSymbol o) {
        if (this.type != o.type) {
            return this.type.compareTo(o.type);
        }

        int result = this.y - o.y;

        if (result == 0) {
            result = this.x - o.x;
        }

        if (result == 0) {
            if (this.isNegation && !o.isNegation) {
                result = 1;
            } else if (!this.isNegation && o.isNegation) {
                result = -1;
            }
        }

        return result;
    }

    public enum SymbolType {
        GOLD,
        PIT,
        BREEZE,
        WUMPUS,
        STENCH,
    };

    SymbolType type;
    int x;
    int y;
    boolean isNegation;

    public PLWumpusWorldSymbol(SymbolType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public PLWumpusWorldSymbol(SymbolType type, int x, int y, boolean negation) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.isNegation = negation;
    }

    public PLWumpusWorldSymbol(PLWumpusWorldSymbol s) {
        this.type = s.type;
        this.x = s.x;
        this.y = s.y;
        this.isNegation = s.isNegation;
    }

    public void setNegative() {
        isNegation = !isNegation;
    }

    public boolean getValue() {
        return !isNegation;
    }

    public boolean isSamePosition(PLWumpusWorldSymbol s) {
        return this.x == s.x && this.y == s.y;
    }

    public boolean isSameTarget(PLWumpusWorldSymbol s) {
        return this.isSamePosition(s) && this.type == s.type;
    }

    public boolean isOpposite(PLWumpusWorldSymbol s) {
        return this.isSameTarget(s) && this.isNegation == !s.isNegation;
    }

    @Override
    public String toString() {
        char c = 0;
        if (type == SymbolType.PIT) {
            c = 'P';
        } else if (type == SymbolType.BREEZE) {
            c = 'B';
        }

        if (isNegation) {
            return String.format("~%c(%d,%d)", c, y, x);
        } else {
            return String.format("%c(%d,%d)", c, y, x);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }

        if (!(object instanceof PLWumpusWorldSymbol)) {
            return false;
        }

        PLWumpusWorldSymbol symbol = (PLWumpusWorldSymbol) object;
        return this.isSameTarget(symbol) && this.isNegation == symbol.isNegation;
    }
}
