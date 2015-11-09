import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by loki on 15. 11. 9..
 */
public class AStar {

    public static void pathFinding(Environment wumpusEnvironment, ArrayList<Integer> stateSeq, ArrayList<Integer[]> path) {
        int worldSize = wumpusEnvironment.getWorldSize();
        char[][][] world = wumpusEnvironment.getWumpusWorld();

        Node startNode = new Node(0, 0, Node.Direction.LEFT, 0);
        Node endNode = new Node(0, 0, Node.Direction.LEFT, 0);

        findStartLocation(world, worldSize, startNode);
        findGoalLocation(world, worldSize, endNode);

        ArrayList<Node> frontierList = new ArrayList<>();
        ArrayList<Node> exploredList = new ArrayList<>();

        frontierList.add(startNode);

        Node last = start(world, worldSize, endNode, frontierList, exploredList);
        ArrayList<Node> nodesOnPath = getPath(last);
        fillStateSequence(nodesOnPath, stateSeq);
        fillPathSequence(nodesOnPath, path);
    }

    static void findStartLocation(char[][][] world, int worldSize, Node node) {
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                char agent = world[i][j][3];
                if (agent == 'A' || agent == 'V' || agent == '<' || agent == '>') {
                    node.x = j;
                    node.y = i;
                    node.g = 0;
                    node.dir = Node.Direction.determine(agent);
                }
            }
        }
    }

    static void findGoalLocation(char[][][] world, int worldSize, Node node) {
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                if (world[i][j][2] == 'G') {
                    node.x = j;
                    node.y = i;
                }
            }
        }
    }

    static Node start(char[][][] world, int worldSize, Node endNode, ArrayList<Node> frontierList, ArrayList<Node> exploredList) {
        int currOptimal = Integer.MAX_VALUE;
        Node optimalNode = null;

        while (frontierList.size() > 0) {
            Node frontier = getOptimalFrontier(frontierList, endNode, currOptimal);
            if (frontier == null) {
                break;
            }
            frontierList.remove(frontier);
            exploredList.add(frontier);

            if (endNode.isSameLocation(frontier)) {
                if (optimalNode == null) {
                    optimalNode = frontier;
                }

                if (currOptimal > optimalNode.g) {
                    currOptimal = optimalNode.g;
                    optimalNode = frontier;
                }
            }

            Node[] nextNodes = new Node[3];
            setNextNodes(frontier, nextNodes);
            for (Node node : nextNodes) {
                if (existNode(exploredList, node)) {
                    continue;
                }

                if (outOfWorld(node, world, worldSize)) {
                    continue;
                }

                if (world[node.y][node.x][1] == 'W') {
                    node.isWumpus = true;
                }

                if (validNode(node, world, worldSize)) {
                    frontierList.add(node);
                    node.prev = frontier;
                } else {
                    if (frontier.isHaveArrow) {
                        node.g += 2;
                        node.isHaveArrow = false;
                        frontierList.add(node);
                        node.prev = frontier;
                    }
                }
            }
        }

        return optimalNode;
    }

    static Node getOptimalFrontier(ArrayList<Node> frontierList, Node endNode, int currOptimal) {
        Node optimal = null;
        int min = Integer.MAX_VALUE;

        for (Node node : frontierList) {
            int h = heuristic(endNode, node);

            int est = node.g + h;
            if (est < currOptimal && est < min) {
                min = est;
                optimal = node;
            }
        }

        return optimal;
    }

    static void setNextNodes(Node curr, Node[] nextNodes) {
        if (curr.dir == Node.Direction.LEFT) {
            // forward
            nextNodes[0] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 2);
        } else if (curr.dir == Node.Direction.TOP) {
            // forward
            nextNodes[0] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 2);
        } else if (curr.dir == Node.Direction.RIGHT) {
            // forward
            nextNodes[0] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x, curr.y + 1, Node.Direction.TOP, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 2);
        } else if (curr.dir == Node.Direction.BOTTOM) {
            // forward
            nextNodes[0] = new Node(curr.x, curr.y - 1, Node.Direction.BOTTOM, curr.g + 1);

            // left
            nextNodes[1] = new Node(curr.x + 1, curr.y, Node.Direction.RIGHT, curr.g + 2);

            // right
            nextNodes[2] = new Node(curr.x - 1, curr.y, Node.Direction.LEFT, curr.g + 2);
        }
    }

    static boolean existNode(ArrayList<Node> exploredList, Node node) {
        for (Node node2 : exploredList) {
            if (node.x == node2.x && node.y == node2.y) {
                return true;
            }
        }

        return false;
    }

    static boolean outOfWorld(Node node, char[][][] world, int worldSize) {
        return node.x < 0 || node.x >= worldSize
                || node.y < 0 || node.y >= worldSize;
    }

    static boolean validNode(Node node, char[][][] world, int worldSize) {
        char[] percepts = world[node.y][node.x];
        return !(percepts[0] == 'P') && !(percepts[1] == 'W');
    }

    static int heuristic(Node endNode, Node node) {
        return Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
    }

    static ArrayList<Node> getPath(Node last) {
        ArrayList<Node> nodesOnPath = new ArrayList<>();
        while (last != null) {
            nodesOnPath.add(last);
            last = last.prev;
        }

        Collections.reverse(nodesOnPath);
        for (Node node : nodesOnPath) {
            System.out.println(node);
        }

        return nodesOnPath;
    }

    static void fillStateSequence(ArrayList<Node> nodesOnPath, ArrayList<Integer> stateSequence) {
        stateSequence.add(Action.START_TRIAL);

        for (int i = 0; i < nodesOnPath.size() - 1; i ++) {
            Node curr = nodesOnPath.get(i);
            Node next = nodesOnPath.get(i + 1);

            if (curr.dir == Node.Direction.LEFT) {
                if (curr.x - 1 == next.x) {
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y - 1 == next.y) {
                    stateSequence.add(Action.TURN_LEFT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y + 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.TOP) {
                if (curr.y + 1 == next.y) {
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x - 1 == next.x) {
                    stateSequence.add(Action.TURN_LEFT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x + 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.RIGHT) {
                if (curr.x + 1 == next.x) {
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y - 1 == next.y) {
                    stateSequence.add(Action.TURN_RIGHT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.y + 1 == next.y) {
                    stateSequence.add(Action.TURN_LEFT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                }
            } else if (curr.dir == Node.Direction.BOTTOM) {
                if (curr.y - 1 == next.y) {
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x - 1 == next.x) {
                    stateSequence.add(Action.TURN_RIGHT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                } else if (curr.x + 1 == next.x) {
                    stateSequence.add(Action.TURN_LEFT);
                    if (next.isWumpus) {
                        stateSequence.add(Action.SHOOT);
                    }
                    stateSequence.add(Action.GO_FORWARD);
                }
            }
        }

        stateSequence.add(Action.GRAB);
        stateSequence.add(Action.END_TRIAL);
    }

    static void fillPathSequence(ArrayList<Node> nodesOnPath, ArrayList<Integer[]> path) {
        for (Node node : nodesOnPath) {
            Integer[] location = new Integer[2];
            location[0] = node.y;
            location[1] = node.x;
            path.add(location);
        }
    }

}
