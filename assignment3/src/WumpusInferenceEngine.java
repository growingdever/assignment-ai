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

            CNF cnf = kb.getCNF();

            output.print("1.");
            output.print(cnf.get(0).toString());

            for (int i = 1; i < cnf.size(); i ++) {
                output.print(i + 1);
                output.print(".");
                output.print(cnf.get(i).toString());
                output.println();
            }

            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    public void runInference(String queryFilePath, String queryOutputPath) {
        try {
            File queryFile = new File(queryFilePath);
            FileReader fileReader = new FileReader(queryFile);
            BufferedReader reader = new BufferedReader(fileReader);
            ArrayList<String> inputLines = new ArrayList<>();
            while (true) {
                String line = reader.readLine();
                if (line == null || line.length() == 0) {
                    break;
                }

                inputLines.add(line);
            }

            PrintWriter output = new PrintWriter(queryOutputPath);
            int lineNumber = 1;
            for (String line : inputLines) {
                CNF clauses = Parser.parseQueryLine(line);

                boolean result = runResolutionInference(clauses);
                System.out.println(result);

                output.print(lineNumber + ".");
                if (result) {
                    output.println("yes");
                } else {
                    output.println("no");
                }

                lineNumber++;
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean runResolutionInference(CNF alpha) {
        CNF cnf = new CNF(kb.getCNF());

        // KB ^ ~a
        CNF negatedAlpha = CNF.negate(alpha);
        cnf.add(negatedAlpha);

        int i, j, k;
        while (true) {
            CNF new_cnf = new CNF();

            for (i = 0; i < cnf.size(); i ++) {
                Clause clause1 = cnf.get(i);
                for (j = i + 1; j < cnf.size(); j ++) {
                    Clause clause2 = cnf.get(j);

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

                        CNF resolvents = Clause.resolve(clause1, clause2);
                        for (k = 0; k < resolvents.size(); k ++) {
                            Clause resolved = resolvents.get(k);
                            if (resolved.size() == 0) {
                                return true;
                            }

                            new_cnf.add(resolved);
                        }
                    }
                }
            }

            // there is no new resolved clause
            if (new_cnf.size() == 0) {
                return false;
            }


            int prevSize = cnf.size();

            // union origin clauses and new clauses
            {
                ArrayList<Integer> newClauseIndices = new ArrayList<>();
                for (i = 0; i < new_cnf.size(); i ++) {
                    Clause new_clause = new_cnf.get(i);
                    if (!cnf.exist(new_clause)) {
                        newClauseIndices.add(i);
                    }
                }

                for (Integer idx : newClauseIndices) {
                    cnf.add(new_cnf.get(idx));
                }
            }

            int currSize = cnf.size();

            // there is no new resolved clause
            if (prevSize == currSize) {
                return false;
            }
        }
    }

}
