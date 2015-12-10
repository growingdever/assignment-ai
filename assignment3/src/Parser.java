/**
 * Created by loki on 2015. 12. 10..
 */
public class Parser {

    public static CNF parseQueryLine(String line) {
        // skip line number suffix
        line = line.substring(2);

        return parseFormula(line);
    }

    public static CNF parseFormula(String line) {
        if (line.contains("<=>")) {
            int pos = line.indexOf("<=>");
            String lhs = line.substring(0, pos);
            String rhs = line.substring(pos + 3);
            CNF cnf1 = parseFormula(lhs);
            CNF cnf2 = parseFormula(rhs);
            System.out.println(cnf1 + " <=> " + cnf2);
        } else if (line.contains("=>")) {
            int pos = line.indexOf("=>");
            String lhs = line.substring(0, pos);
            String rhs = line.substring(pos + 2);
            CNF cnf1 = parseFormula(lhs);
            CNF cnf2 = parseFormula(rhs);
            CNF result = CNF.convertFromImply(cnf1, cnf2);
            System.out.println(cnf1 + " => " + cnf2);
            System.out.println(result);
        } else {
            if (line.contains("(") && line.contains(")")) {
                int posBrOpen = line.indexOf('(');
                int posBrClose = line.lastIndexOf(')');
                return parseFormula(line.substring(posBrOpen + 1, posBrClose));
            }

            int countAND = 0;
            int countOR = 0;
            for (int i = 0; i < line.length(); i ++) {
                if (line.charAt(i) == '^') {
                    countAND++;
                } else if (line.charAt(i) == 'V') {
                    countOR++;
                }
            }

            String[] tokens = line.split("\\^|V");
            if (countAND != 0 && countOR != 0 && tokens.length > 1) {
                System.err.println("CNF 혹은 DNF만 처리 가능");
                System.exit(0);
            }

            if (countAND > 0) {
                CNF cnf = new CNF();
                for (String token : tokens) {
                    Clause clause = new Clause();
                    PLWumpusWorldSymbol symbol = Parser.parseLiteral(token);
                    clause.add(symbol);
                    cnf.add(clause);
                }

                return cnf;
            } else {
                CNF cnf = new CNF();
                Clause clause = new Clause();
                cnf.add(clause);

                for (String token : tokens) {
                    PLWumpusWorldSymbol symbol = Parser.parseLiteral(token);
                    clause.add(symbol);
                }

                return cnf;
            }
        }

        return null;
    }

    public static PLWumpusWorldSymbol parseLiteral(String strLiteral) {
        if (strLiteral.charAt(0) == '~') {
            char c = strLiteral.charAt(1);
            String[] strPos = strLiteral.substring(2).split(",");
            int y = Integer.parseInt(strPos[0]);
            int x = Integer.parseInt(strPos[1]);
            PLWumpusWorldSymbol symbol = new PLWumpusWorldSymbol(c == 'B' ?
                    PLWumpusWorldSymbol.SymbolType.BREEZE : PLWumpusWorldSymbol.SymbolType.PIT,
                    x, y);
            symbol.setNegative();

            return symbol;
        } else {
            char c = strLiteral.charAt(0);
            String[] strPos = strLiteral.substring(1).split(",");
            int y = Integer.parseInt(strPos[0]);
            int x = Integer.parseInt(strPos[1]);

            return new PLWumpusWorldSymbol(c == 'B' ?
                    PLWumpusWorldSymbol.SymbolType.BREEZE : PLWumpusWorldSymbol.SymbolType.PIT,
                    x, y);
        }
    }

}
