import java.io.*;
import java.util.ArrayList;

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
    }

    void printKnowledgeBase(String path) {
        try {
            PrintWriter output = new PrintWriter(path);

            ArrayList<Clause> clauses = kb.getClauses();

            output.print(clauses.get(0).toString());

            for (int i = 1; i < clauses.size(); i ++) {
                output.print(" V ");
                output.print(clauses.get(i).toString());
            }

            output.println();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    String buildClausesString(ArrayList<Clause> clauses) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < clauses.size(); i ++) {
            if (i > 0) {
                stringBuilder.append("\n AND ");
            }

            Clause clause = clauses.get(i);
            stringBuilder.append(clause.toString());
        }

        return stringBuilder.toString();
    }

    void addClauseByBreeze(int target_x, int target_y) {
        Clause clause = new Clause();
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

        Clause clause2 = new Clause();
        PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.BREEZE, target_x, target_y);
        clause2.add(symbol2);
        kb.addClause(clause2);

        Clause clause3 = new Clause();
        PLWumpusWorldSymbol symbol3 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, target_x, target_y);
        symbol3.setNegative();
        clause3.add(symbol3);
        kb.addClause(clause3);
    }

    void addClauseByBlank(int target_x, int target_y) {
        // insert there isn't breeze propositional logic
        Clause clause1 = new Clause();
        PLWumpusWorldSymbol symbol1 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.BREEZE, target_x, target_y);
        symbol1.setNegative();
        clause1.add(symbol1);
        kb.addClause(clause1);

        if (isAllNeighborBreeze(target_x, target_y)) {
            Clause clause2 = new Clause();
            PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, target_x, target_y);
            clause2.add(symbol2);
            kb.addClause(clause2);
        }

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

        // there isn't B at neighbor room, there isn't pit
        Clause clause2 = new Clause();
        PLWumpusWorldSymbol symbol2 = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, target_x, target_y);
        symbol2.setNegative();
        clause2.add(symbol2);
        kb.addClause(clause2);

        for (int i = 0; i < OFFSET_X.length; i++) {
            int x = target_x + OFFSET_X[i];
            int y = target_y + OFFSET_Y[i];

            if (x >= worldSize || x < 0
                    || y >= worldSize || y < 0) {
                continue;
            }

            Clause clause3 = new Clause();
            PLWumpusWorldSymbol symbol = new PLWumpusWorldSymbol(PLWumpusWorldSymbol.SymbolType.PIT, x, y);
            symbol.setNegative();
            clause3.add(symbol);
            kb.addClause(clause3);
        }
    }

    boolean isAllNeighborBreeze(int targetX, int targetY) {
        int count = 0;
        for (int i = 0; i < OFFSET_X.length; i++) {
            int x = targetX + OFFSET_X[i];
            int y = targetY + OFFSET_Y[i];

            if (x >= worldSize || x < 0
                    || y >= worldSize || y < 0) {
                count ++;
                continue;
            }

            if (map[y][x] == 'B') {
                count ++;
            }
        }

        return count == 4;
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

                ArrayList<Clause> clauses = parseQueryLine(line);
                boolean result = runResolutionInference(clauses);
                System.out.println(buildClausesString(clauses));
                System.out.println(result);
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<Clause> negateClauses(ArrayList<Clause> clauses) {
        ArrayList<Clause> result = new ArrayList<>();

        if (clauses.size() == 1) {
            for (Clause clause : clauses) {
                for (int i = 0; i < clause.size(); i ++) {
                    PLWumpusWorldSymbol s = clause.get(i);

                    Clause new_clause = new Clause();
                    PLWumpusWorldSymbol clone = new PLWumpusWorldSymbol(s.type, s.x, s.y, !s.isNegation);
                    new_clause.add(clone);
                    result.add(new_clause);
                }
            }
        } else {
            Clause new_clause = new Clause();
            for (Clause clause : clauses) {
                for (int i = 0; i < clause.size(); i ++) {
                    PLWumpusWorldSymbol s = clause.get(i);

                    PLWumpusWorldSymbol clone = new PLWumpusWorldSymbol(s);
                    clone.setNegative();
                    new_clause.add(clone);
                }
            }
            result.add(new_clause);
        }

        return result;
    }

    boolean runResolutionInference(ArrayList<Clause> alpha) {
        ArrayList<Clause> clauses = Util.cloneClauses(kb.getClauses());

        // KB ^ ~a
        ArrayList<Clause> negatedAlpha = negateClauses(alpha);
        clauses.addAll(negatedAlpha);

        int i, j, k;
        while (true) {
            ArrayList<Clause> new_clauses = new ArrayList<>();

            for (i = 0; i < clauses.size(); i ++) {
                Clause clause1 = clauses.get(i);
                for (j = i + 1; j < clauses.size(); j ++) {
                    Clause clause2 = clauses.get(j);

                    if (clause1.size() == 1 && clause2.size() == 1) {
                        PLWumpusWorldSymbol symbol1 = clause1.get(0);
                        PLWumpusWorldSymbol symbol2 = clause2.get(0);

                        // contradiction - resolvents contains the empty clause?
                        if (symbol1.isSameTarget(symbol2)
                                && symbol1.isNegation != symbol2.isNegation) {
                            return true;
                        }
                    } else {
                        if (!clause1.isResolvable(clause2)) {
                            continue;
                        }

                        ArrayList<Clause> resolvents = Clause.resolve(clause1, clause2);
                        for (k = 0; k < resolvents.size(); k ++) {
                            Clause resolved = resolvents.get(k);
                            if (resolved.size() == 0) {
                                return true;
                            }

                            new_clauses.add(resolved);
                        }
                    }
                }
            }

            // there is no new resolved clause
            if (new_clauses.size() == 0) {
                return false;
            }


            int prevSize = clauses.size();

            // union origin clauses and new clauses
            {
                ArrayList<Integer> newClauseIndices = new ArrayList<>();
                for (i = 0; i < new_clauses.size(); i ++) {
                    Clause new_clause = new_clauses.get(i);
                    if (!Util.existSameSortedClause(clauses, new_clause)) {
                        newClauseIndices.add(i);
                    }
                }

                for (Integer idx : newClauseIndices) {
                    clauses.add(new_clauses.get(idx));
                }
            }

            int currSize = clauses.size();

            // there is no new resolved clause
            if (prevSize == currSize) {
                return false;
            }
        }
    }

    ArrayList<Clause> parseQueryLine(String line) {
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
            ArrayList<Clause> clauses = new ArrayList<>();
            for (String token : tokens) {
                Clause clause = new Clause();
                PLWumpusWorldSymbol symbol = Parser.parseLiteral(token);
                clause.add(symbol);
                clauses.add(clause);
            }

            return clauses;
        } else {
            ArrayList<Clause> clauses = new ArrayList<>();
            Clause clause = new Clause();
            clauses.add(clause);

            for (String token : tokens) {
                PLWumpusWorldSymbol symbol = Parser.parseLiteral(token);
                clause.add(symbol);
            }

            return clauses;
        }
    }

}
