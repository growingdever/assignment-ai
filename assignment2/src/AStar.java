import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by loki on 15. 11. 9..
 */
public class AStar {

    public static void pathFinding(Environment wumpusEnvironment) {
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
        ArrayList<Node> path = new ArrayList<>();
        while (last != null) {
            path.add(last);
            last = last.prev;
        }

        Collections.reverse(path);
        for (Node node : path) {
            System.out.println(node);
        }
    }

    public static void findStartLocation(char[][][] world, int worldSize, Node node) {
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

    public static void findGoalLocation(char[][][] world, int worldSize, Node node) {
        for (int i = 0; i < worldSize; i ++) {
            for (int j = 0; j < worldSize; j ++) {
                if (world[i][j][2] == 'G') {
                    node.x = j;
                    node.y = i;
                }
            }
        }
    }

    private static Node start(char[][][] world, int worldSize, Node endNode, ArrayList<Node> frontierList, ArrayList<Node> exploredList) {
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
                if (!existNode(exploredList, node) && validNode(node, world, worldSize)) {
                    frontierList.add(node);
                    node.setPrev(frontier);
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

    static boolean validNode(Node node, char[][][] world, int worldSize) {
        if (node.x < 0 || node.x >= worldSize
                || node.y < 0 || node.y >= worldSize) {
            return false;
        }

        char[] percepts = world[node.y][node.x];
        return !(percepts[0] == 'P') && !(percepts[1] == 'W');
    }

    boolean goalTest(char[][][] world, Node node) {
        char[] percepts = world[node.y][node.x];
        return percepts[2] == 'G';
    }

    static int heuristic(Node endNode, Node node) {
        return Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
    }

}
