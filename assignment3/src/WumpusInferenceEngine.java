import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by loki on 2015. 12. 8..
 */
public class WumpusInferenceEngine {

    static int OFFSET_X[] = { -1, 0, 1, 0 };
    static int OFFSET_Y[] = { 0, 1, 0, -1 };

    char[][] map;
    int worldSize;
    KnowledgeBase kb;


    public WumpusInferenceEngine(char[][] map, int worldSize) {
        this.map = map;
        this.worldSize = worldSize;

        initializeKnowledgeBase();
    }

    void initializeKnowledgeBase() {
        kb = new KnowledgeBase();

        // left bottom is 0, 0
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                if (map[i][j] == 'B') {
                    addClauseByBreeze(j, i);
                } else {
                    addClauseByBlank(j, i);
                }
            }
        }

//        refactoringKnowledgeBase();
    }

    void printKnowledgeBase(String path) {
        try {
            PrintWriter output = new PrintWriter(path);

            ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = kb.getClauses();
            for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
                if (clause.size() == 0) {
                    continue;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(clause.get(0).toString());
                for (int i = 1; i < clause.size(); i ++) {
                    stringBuilder.append(" V ");
                    stringBuilder.append(clause.get(i).toString());
                }

                output.println(stringBuilder.toString());
            }

            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void printClauses(ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses) {
        System.out.println("===Clauses===");
        for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
            if (clause.size() == 0) {
                continue;
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(clause.get(0).toString());
            for (int i = 1; i < clause.size(); i ++) {
                stringBuilder.append(" V ");
                stringBuilder.append(clause.get(i).toString());
            }

            System.out.println(stringBuilder.toString());
        }
        System.out.println("============");
    }

    void addClauseByBreeze(int target_x, int target_y) {
        ArrayList<PLWumpusWorldSymbol> clause = new ArrayList<>();
        for (int i = 0; i < OFFSET_X.length; i++) {
            int x = target_x + OFFSET_X[i];
            int y = target_y + OFFSET_Y[i];

            if (x >= worldSize || x < 0 ||
                    y >= worldSize || y < 0) {
                continue;
            }

            PLWumpusWorldSymbol symbol = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, x, y);
            clause.add(symbol);
        }
        kb.addClause(clause);

        ArrayList<PLWumpusWorldSymbol> clause2 = new ArrayList<>();
        PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.BREEZE, target_x, target_y);
        clause2.add(symbol2);
        kb.addClause(clause2);

        ArrayList<PLWumpusWorldSymbol> clause3 = new ArrayList<>();
        PLWumpusWorldSymbol symbol3 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, target_x, target_y);
        symbol3.setNegative();
        clause3.add(symbol3);
        kb.addClause(clause3);
    }

    void addClauseByBlank(int target_x, int target_y) {
        // insert there isn't breeze propositional logic
        ArrayList<PLWumpusWorldSymbol> clause1 = new ArrayList<>();
        PLWumpusWorldSymbol symbol1 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.BREEZE, target_x, target_y);
        symbol1.setNegative();
        clause1.add(symbol1);
        kb.addClause(clause1);

        // skip if there is breeze at neighbor room
        for (int i = 0; i < OFFSET_X.length; i++) {
            int x = target_x + OFFSET_X[i];
            int y = target_y + OFFSET_Y[i];

            if (x >= worldSize || x < 0
                    || y >= worldSize || y < 0) {
                continue;
            }

            if (map[y][x] == 'B') {
                return;
            }
        }

        ArrayList<PLWumpusWorldSymbol> clause = new ArrayList<>();
        for (int i = 0; i < OFFSET_X.length; i++) {
            int x = target_x + OFFSET_X[i];
            int y = target_y + OFFSET_Y[i];

            if (x >= worldSize || x < 0
                    || y >= worldSize || y < 0) {
                continue;
            }

            PLWumpusWorldSymbol symbol = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, x, y);
            symbol.setNegative();
            clause.add(symbol);
        }
        kb.addClause(clause);

        ArrayList<PLWumpusWorldSymbol> clause2 = new ArrayList<>();
        PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.BREEZE, target_x, target_y);
        symbol2.setNegative();
        clause2.add(symbol2);
        kb.addClause(clause2);

        ArrayList<PLWumpusWorldSymbol> clause3 = new ArrayList<>();
        PLWumpusWorldSymbol symbol3 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, target_x, target_y);
        symbol3.setNegative();
        clause3.add(symbol3);
        kb.addClause(clause3);
    }

    void refactoringKnowledgeBase() {
        ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = kb.getClauses();
        for (int i = 0; i < clauses.size(); i ++) {
            ArrayList<PLWumpusWorldSymbol> clause1 = clauses.get(i);
            if (clause1.size() != 1) {
                continue;
            }

            for (int j = i + 1; j < clauses.size(); j ++) {
                ArrayList<PLWumpusWorldSymbol> clause2 = clauses.get(j);
                if (clause2.size() != 1) {
                    continue;
                }

                PLWumpusWorldSymbol symbol1 = clause1.get(0);
                PLWumpusWorldSymbol symbol2 = clause2.get(0);

                if (!symbol1.isSamePosition(symbol2)
                        || symbol1.isNegation == symbol2.isNegation
                        || symbol1.type == symbol2.type) {
                    continue;
                }

                clauses.remove(i);
                i --;
            }
        }
    }

    void removeSameTypeSymbol(PLWumpusWorldSymbol symbol) {
        ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = kb.getClauses();
        for (ArrayList<PLWumpusWorldSymbol> clause : clauses) {
            for (PLWumpusWorldSymbol s : clause) {
                if (symbol.isSameTarget(s)) {
                    clause.remove(s);
                    break;
                }
            }
        }
    }

    public void runInference(String queryFilePath) {
        try {
            File queryFile = new File(queryFilePath);
            FileReader fileReader = new FileReader(queryFile);
            BufferedReader reader = new BufferedReader(fileReader);
            while (true) {
                String line = reader.readLine();
                if (line == null || line.length() == 0) {
                    break;
                }

                ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = parseQueryLine(line);
                boolean result = runResolutionInference(clauses);
                System.out.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean runResolutionInference(ArrayList< ArrayList<PLWumpusWorldSymbol> > alpha) {
        ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = Util.cloneClauses(kb.getClauses());
        ArrayList< ArrayList<PLWumpusWorldSymbol> > originAlpha = Util.cloneClauses(alpha);

        // KB ^ ~a
        // below is invalid
        for (ArrayList<PLWumpusWorldSymbol> clause : alpha) {
            ArrayList<PLWumpusWorldSymbol> new_clause = new ArrayList<>();
            for (PLWumpusWorldSymbol s : clause) {
                s.setNegative();
                new_clause.add(s);
            }
            clauses.add(new_clause);
        }

        int i, j, k;
        while (true) {
            ArrayList< ArrayList<PLWumpusWorldSymbol> > new_clauses = new ArrayList<>();

            for (i = 0; i < clauses.size(); i ++) {
                ArrayList<PLWumpusWorldSymbol> clause1 = clauses.get(i);
                for (j = i + 1; j < clauses.size(); j ++) {
                    ArrayList<PLWumpusWorldSymbol> clause2 = clauses.get(j);

                    if (clause1.size() == 1 && clause2.size() == 1) {
                        PLWumpusWorldSymbol symbol1 = clause1.get(0);
                        PLWumpusWorldSymbol symbol2 = clause2.get(0);

                        // contradiction - resolvents contains the empty clause?
                        if (symbol1.isSameTarget(symbol2)
                                && symbol1.isNegation != symbol2.isNegation) {
                            return true;
                        }
                    } else {
                        if (!isResolvable(clause1, clause2)) {
                            continue;
                        }

                        ArrayList< ArrayList<PLWumpusWorldSymbol> > resolvents = runResolution(clause1, clause2);
                        for (ArrayList<PLWumpusWorldSymbol> resolved : resolvents) {
                            if (resolved.size() == 0) {
                                ArrayList< ArrayList<PLWumpusWorldSymbol> > resolvents2 = runResolution(clause1, clause2);
                                return true;
                            }

                            for (k = 0; k < new_clauses.size(); k ++) {
                                if (compareSortedClause(new_clauses.get(k), resolved)) {
                                    break;
                                }
                            }
                            if (k == new_clauses.size()) {
                                new_clauses.add(resolved);
                            }
                        }
                    }
                }
            }

            if (new_clauses.size() == 0) {
                return false;
            }


            // union origin clauses and new clauses
            {
                ArrayList<Integer> newClauseIndices = new ArrayList<>();
                for (i = 0; i < new_clauses.size(); i ++) {
                    ArrayList<PLWumpusWorldSymbol> new_clause = new_clauses.get(i);
                    for (j = 0; j < clauses.size(); j++) {
                        if (compareSortedClause(clauses.get(j), new_clause)) {
                            break;
                        }
                    }

                    if (j == clauses.size()) {
                        newClauseIndices.add(i);
                    }
                }

                for (Integer idx : newClauseIndices) {
                    clauses.add(new_clauses.get(idx));
                }
            }

            printClauses(clauses);
        }

//        return false;
    }

    boolean isResolvable(ArrayList<PLWumpusWorldSymbol> c1, ArrayList<PLWumpusWorldSymbol> c2) {
        for (PLWumpusWorldSymbol symbol1 : c1) {
            for (PLWumpusWorldSymbol symbol2 : c2) {
                if (symbol1.isOpposite(symbol2)) {
                    return true;
                }
            }
        }

        return false;
    }

    ArrayList< ArrayList<PLWumpusWorldSymbol> > runResolution(ArrayList<PLWumpusWorldSymbol> c1, ArrayList<PLWumpusWorldSymbol> c2) {
        ArrayList< ArrayList<PLWumpusWorldSymbol> > result = new ArrayList<>();

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
            ArrayList<PLWumpusWorldSymbol> new_clause = new ArrayList<>();
            new_clause.addAll(c1);
            new_clause.addAll(c2);

            result.add(new_clause);
        } else {
            for (Integer oppositeIndex : oppositeIndices) {
                ArrayList<PLWumpusWorldSymbol> new_clause = new ArrayList<>();

                PLWumpusWorldSymbol oppositeSymbol = c2.get(oppositeIndex);
                for (PLWumpusWorldSymbol symbol : c1) {
                    if (symbol.isSameTarget(oppositeSymbol) && symbol.isNegation != oppositeSymbol.isNegation) {
                        continue;
                    }

                    if (Util.existSymbolInClause(new_clause, symbol)) {
                        continue;
                    }

                    new_clause.add(symbol);
                }

                Util.addAllExceptIndex(new_clause, c2, oppositeIndex);

                if (new_clause.size() > c1.size() + c2.size() - 2) {
                    System.err.println("resolution error");
                    System.exit(1000);
                }

                Collections.sort(new_clause);

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

                boolean isExistSameClause = false;
                for (ArrayList<PLWumpusWorldSymbol> clause : result) {
                    if (compareSortedClause(clause, new_clause)) {
                        isExistSameClause = true;
                    }
                }

                if (!isExistSameClause) {
                    result.add(new_clause);
                }
            }
        }

        return result;
    }

    boolean compareClause(ArrayList<PLWumpusWorldSymbol> clause1, ArrayList<PLWumpusWorldSymbol> clause2) {
        if (clause1.size() != clause2.size()) {
            return false;
        }

        int i, j;
        for (i = 0; i < clause1.size(); i ++) {
            PLWumpusWorldSymbol symbol1 = clause1.get(i);
            boolean exist = false;
            for (j = 0; j < clause2.size(); j ++) {
                PLWumpusWorldSymbol symbol2 = clause2.get(i);
                if (symbol1.equals(symbol2)) {
                    exist = true;
                }
            }

            if (!exist) {
                return false;
            }
        }

        return true;
    }

    boolean compareSortedClause(ArrayList<PLWumpusWorldSymbol> clause1, ArrayList<PLWumpusWorldSymbol> clause2) {
        if (clause1.size() != clause2.size()) {
            return false;
        }

        for (int i = 0; i < clause1.size(); i++) {
            if (!clause1.get(i).equals(clause2.get(i))) {
                return false;
            }
        }

        return true;
    }

    ArrayList< ArrayList<PLWumpusWorldSymbol> > parseQueryLine(String line) {
        // skip line number suffix
        line = line.substring(2);

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
            ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = new ArrayList<>();
            for (String token : tokens) {
                ArrayList<PLWumpusWorldSymbol> clause = new ArrayList<>();
                PLWumpusWorldSymbol symbol = parseLiteral(token);
                clause.add(symbol);
                clauses.add(clause);
            }

            return clauses;
        } else {
            ArrayList< ArrayList<PLWumpusWorldSymbol> > clauses = new ArrayList<>();
            ArrayList<PLWumpusWorldSymbol> clause = new ArrayList<>();
            clauses.add(clause);

            for (String token : tokens) {
                PLWumpusWorldSymbol symbol = parseLiteral(token);
                clause.add(symbol);
            }

            return clauses;
        }
    }

    PLWumpusWorldSymbol parseLiteral(String strLiteral) {
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
