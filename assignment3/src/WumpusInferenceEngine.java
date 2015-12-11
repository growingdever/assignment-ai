import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by loki on 2015. 12. 8..
 */
public class WumpusInferenceEngine {

    public enum AlgorithmType {
        RESOLUTION,
        RANDOM_WALKING,
    };

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

    public void runInference(String queryFilePath, String queryOutputPath, AlgorithmType algorithm) {
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
                CNF queryCNF = Parser.parseQueryLine(line);

                boolean result = false;
                if (algorithm == AlgorithmType.RESOLUTION) {
                    result = runResolutionInference(queryCNF);
                } else if (algorithm == AlgorithmType.RANDOM_WALKING){
                    result = runWalkSAT(queryCNF, 0.01, 100000);
                }

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

    boolean runWalkSAT(CNF queryCNF, double p, int maxFlips) {
        CNF cnf = new CNF(kb.getCNF());
        cnf.add(queryCNF);

        int countP = 0;
        int countQ = 0;

        boolean[][] modelPit = new boolean[worldSize][worldSize];
        boolean[][] modelBreeze = new boolean[worldSize][worldSize];

        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        for (int y = 0; y < worldSize; y ++) {
            for (int x = 0; x < worldSize; x ++) {
                modelBreeze[y][x] = random.nextBoolean();
                modelPit[y][x] = random.nextBoolean();
            }
        }

        ArrayList<Clause> falseClauseList = new ArrayList<>();

        for (int flip = 0; flip < maxFlips; flip ++) {
            falseClauseList.clear();

            boolean sat = true;
            for (int i = 0; i < cnf.size(); i ++) {
                Clause clause = cnf.get(i);
                boolean isSatisfying = isSatisfied(clause, modelBreeze, modelPit);
                if (!isSatisfying) {
                    falseClauseList.add(clause);
                } else {
                }

                sat = sat && isSatisfying;
            }

            if (sat) {
                return true;
            }

            Clause falseClause = falseClauseList.get(random.nextInt(falseClauseList.size()));

            double q = random.nextDouble();
            if (q < p) {
                PLWumpusWorldSymbol randomSymbol = falseClause.get(random.nextInt(falseClause.size()));
                if (randomSymbol.type == PLWumpusWorldSymbol.SymbolType.PIT) {
                    modelPit[randomSymbol.y][randomSymbol.x] = !modelPit[randomSymbol.y][randomSymbol.x];
                } else if (randomSymbol.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
                    modelBreeze[randomSymbol.y][randomSymbol.x] = !modelBreeze[randomSymbol.y][randomSymbol.x];
                }

                countP++;
            } else {
                int maxNumOfSatisfiedClause = -1;
                PLWumpusWorldSymbol maximizeSymbol = falseClause.get(0);
                for (int i = 0; i < falseClause.size(); i ++) {
                    PLWumpusWorldSymbol symbol = falseClause.get(i);
                    boolean origin = false;

                    if (symbol.type == PLWumpusWorldSymbol.SymbolType.PIT) {
                        origin = modelPit[symbol.y][symbol.x];
                        modelPit[symbol.y][symbol.x] = !origin;
                    } else if (symbol.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
                        origin = modelBreeze[symbol.y][symbol.x];
                        modelBreeze[symbol.y][symbol.x] = !origin;
                    }

                    int numOfSatisfiedClause = 0;
                    for (int j = 0; j < cnf.size(); j ++) {
                        Clause clause = cnf.get(j);
                        if (isSatisfied(clause, modelBreeze, modelPit)) {
                            numOfSatisfiedClause++;
                        }
                    }

                    if (symbol.type == PLWumpusWorldSymbol.SymbolType.PIT) {
                        modelPit[symbol.y][symbol.x] = origin;
                    } else if (symbol.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
                        modelBreeze[symbol.y][symbol.x] = origin;
                    }

                    if (maxNumOfSatisfiedClause < numOfSatisfiedClause) {
                        maximizeSymbol = symbol;
                        maxNumOfSatisfiedClause = numOfSatisfiedClause;
                    }
                }

                if (maximizeSymbol.type == PLWumpusWorldSymbol.SymbolType.PIT) {
                    modelPit[maximizeSymbol.y][maximizeSymbol.x] = !modelPit[maximizeSymbol.y][maximizeSymbol.x];
                } else if (maximizeSymbol.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
                    modelBreeze[maximizeSymbol.y][maximizeSymbol.x] = !modelBreeze[maximizeSymbol.y][maximizeSymbol.x];
                }

                countQ++;
            }
        }

        return false;
    }

    boolean isSatisfied(Clause clause, boolean[][] modelBreeze, boolean[][] modelPit) {
        if (clause.size() == 0) {
            return true;
        }

        PLWumpusWorldSymbol first = clause.get(0);

        boolean sat = false;
        if (first.type == PLWumpusWorldSymbol.SymbolType.PIT) {
            if (first.isNegation) {
                sat = !modelPit[first.y][first.x];
            } else {
                sat = modelPit[first.y][first.x];
            }
        } else if (first.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
            if (first.isNegation) {
                sat = !modelBreeze[first.y][first.x];
            } else {
                sat = modelBreeze[first.y][first.x];
            }
        }

        for (int j = 1; j < clause.size(); j ++) {
            PLWumpusWorldSymbol symbol = clause.get(j);
            if (symbol.type == PLWumpusWorldSymbol.SymbolType.BREEZE) {
                boolean target = modelBreeze[symbol.y][symbol.x];
                if (symbol.isNegation) {
                    target = !target;
                }

                sat = sat || target;
            } else if (symbol.type == PLWumpusWorldSymbol.SymbolType.PIT) {
                boolean target = modelPit[symbol.y][symbol.x];
                if (symbol.isNegation) {
                    target = !target;
                }

                sat = sat || target;
            }
        }

        return sat;
    }

}
