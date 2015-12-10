/**
 * Created by loki on 2015. 12. 10..
 */
public class Parser {

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
