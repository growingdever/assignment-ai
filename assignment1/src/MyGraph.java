import java.util.*;

/**
 * Created by loki on 2015. 10. 4..
 */
public class MyGraph {

    public static int DIR_LEFT = 0;
    public static int DIR_TOP = 1;
    public static int DIR_RIGHT = 2;
    public static int DIR_BOTTOM = 3;


    ArrayList<MyNode> nodes;
    ArrayList<MyEdge> edges;
    MyNode currNode;
    public int currDir;

    ArrayList<MyNode> frontiers;
    MyNode currFrontier;

    ArrayDeque<Integer> currActions;

    Random random;


    public MyGraph(Random random) {
        this.random = random;

        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        currNode = new MyNode();
        nodes.add(currNode);

        currDir = DIR_LEFT;

        frontiers = new ArrayList<>();

        currActions = new ArrayDeque<>();
    }

    public void updateGraph() {
        currNode.isVisited = true;

        if (!currNode.isBreeze && !currNode.isStench) {
            if (currNode.left == null) {
                MyNode newNode = new MyNode(currNode.x - 1, currNode.y);
                addNode(newNode);
            }
            if (currNode.top == null) {
                MyNode newNode = new MyNode(currNode.x, currNode.y + 1);
                addNode(newNode);
            }
            if (currNode.right == null) {
                MyNode newNode = new MyNode(currNode.x + 1, currNode.y);
                addNode(newNode);
            }
            if (currNode.bottom == null) {
                MyNode newNode = new MyNode(currNode.x, currNode.y - 1);
                addNode(newNode);
            }
        }

        for (MyNode node : nodes) {
            if (node.isWumpus || node.isPit) {
                continue;
            }

            checkIrony(node);

            if (!node.isVisited && !node.isPit && !node.isWumpus) {
                if (frontiers.indexOf(node) == -1) {
                    frontiers.add(node);
                }
            }
        }
    }

    void checkIrony(MyNode node) {
        if (perceptionCross(node.left, node.top)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.left, node.right)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.left, node.bottom)) {
            node.isSafe = true;
        }

        if (perceptionCross(node.top, node.left)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.top, node.right)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.top, node.bottom)) {
            node.isSafe = true;
        }

        if (perceptionCross(node.right, node.left)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.right, node.top)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.right, node.bottom)) {
            node.isSafe = true;
        }

        if (perceptionCross(node.bottom, node.left)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.bottom, node.top)) {
            node.isSafe = true;
        }
        if (perceptionCross(node.bottom, node.right)) {
            node.isSafe = true;
        }
    }

    boolean perceptionCross(MyNode lhs, MyNode rhs) {
        if (lhs == null || rhs == null) {
            return false;
        }

        return (lhs.isBreeze && rhs.isStench) || (lhs.isStench && rhs.isBreeze);
    }

    public void updateCurrentFrontier() {
        frontiers.remove(currFrontier);

        if (frontiers.size() == 0) {

        } else {
            while (true) {
                currFrontier = frontiers.get(random.nextInt(frontiers.size()));
                if (currFrontier.isPit || currFrontier.isWumpus) {
                } else if (currFrontier.x == currNode.x && currFrontier.y == currNode.y) {
                } else {
                    break;
                }
            }
        }

        // find path of current node to frontier
        Queue<MyNode> queue = new ArrayDeque<>();
        queue.offer(currNode);

        HashSet<MyNode> visited = new HashSet<>();
        visited.add(currNode);

        HashMap<MyNode, MyNode> pred = new HashMap<>();
        pred.put(currNode, null);

        while (!queue.isEmpty()) {
            MyNode node = queue.poll();
            if (node == currFrontier) {
                break;
            }

            if (node.left != null && !visited.contains(node.left)) {
                queue.offer(node.left);
                visited.add(node.left);
                pred.put(node.left, node);
            }
            if (node.right != null && !visited.contains(node.right)) {
                queue.offer(node.right);
                visited.add(node.right);
                pred.put(node.right, node);
            }
            if (node.top != null && !visited.contains(node.top)) {
                queue.offer(node.top);
                visited.add(node.top);
                pred.put(node.top, node);
            }
            if (node.bottom != null && !visited.contains(node.bottom)) {
                queue.offer(node.bottom);
                visited.add(node.bottom);
                pred.put(node.bottom, node);
            }
        }

        MyNode predNode = pred.get(currFrontier);

        ArrayList<MyNode> onPathNodes = new ArrayList<>();
        onPathNodes.add(currFrontier);

        while(true) {
            onPathNodes.add(predNode);
            predNode = pred.get(predNode);
            if (predNode == null) {
                break;
            }
        }

        Collections.reverse(onPathNodes);

        if (currActions.size() != 0) {
            // error!
            return;
        }

        int prevDir = currDir;
        for (int i = 0; i < onPathNodes.size() - 1; i++) {
            MyNode a = onPathNodes.get(i);
            MyNode b = onPathNodes.get(i + 1);
            prevDir = addActions(a, b, prevDir);
        }
    }

    int addActions(MyNode curr, MyNode next, int prevDir) {
        if (curr.left == next) {
            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_LEFT;
        } else if (curr.right == next) {
            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_RIGHT;
        } else if (curr.top == next) {
            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_TOP;
        } else if (curr.bottom == next) {
            if (prevDir == DIR_LEFT) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_TOP) {
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.TURN_LEFT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_RIGHT) {
                currActions.offer(Action.TURN_RIGHT);
                currActions.offer(Action.GO_FORWARD);
            } else if (prevDir == DIR_BOTTOM) {
                currActions.offer(Action.GO_FORWARD);
            }

            return DIR_BOTTOM;
        }

        // error!
        assert false;
        return DIR_LEFT;
    }

    public Queue getCurrentActions() {
        return currActions;
    }

    public MyNode getCurrNode() {
        return currNode;
    }

    public MyNode getForwardNode() {
        MyNode node = createNextNode();
        return findNode(node.x, node.y);
    }

    public MyNode findNode(int x, int y) {
        for (MyNode node : nodes) {
            if (node.x == x && node.y == y) {
                return node;
            }
        }

        return null;
    }

    public MyNode createNextNode() {
        MyNode node = new MyNode();
        if (currDir == DIR_LEFT) {
            node.x = currNode.x - 1;
            node.y = currNode.y;
        } else if (currDir == DIR_TOP) {
            node.x = currNode.x;
            node.y = currNode.y + 1;
        } else if (currDir == DIR_RIGHT) {
            node.x = currNode.x + 1;
            node.y = currNode.y;
        } else if (currDir == DIR_BOTTOM) {
            node.x = currNode.x;
            node.y = currNode.y - 1;
        }

        return node;
    }

    public void addNode(MyNode node) {
        nodes.add(node);

        MyNode left = new MyNode(node.x - 1, node.y);
        MyNode top = new MyNode(node.x, node.y + 1);
        MyNode right = new MyNode(node.x + 1, node.y);
        MyNode bottom = new MyNode(node.x, node.y - 1);

        if (findNode(left.x, left.y) != null) {
            node.left = findNode(left.x, left.y);
            findNode(left.x, left.y).right = node;

            edges.add(new MyEdge(findNode(left.x, left.y), node));
            edges.add(new MyEdge(node, findNode(left.x, left.y)));
        }
        if (findNode(top.x, top.y) != null) {
            node.top = findNode(top.x, top.y);
            findNode(top.x, top.y).bottom = node;

            edges.add(new MyEdge(findNode(top.x, top.y), node));
            edges.add(new MyEdge(node, findNode(top.x, top.y)));
        }
        if (findNode(right.x, right.y) != null) {
            node.right = findNode(right.x, right.y);
            findNode(right.x, right.y).left = node;

            edges.add(new MyEdge(findNode(right.x, right.y), node));
            edges.add(new MyEdge(node, findNode(right.x, right.y)));
        }
        if (findNode(bottom.x, bottom.y) != null) {
            node.bottom = findNode(bottom.x, bottom.y);
            findNode(bottom.x, bottom.y).top = node;

            edges.add(new MyEdge(findNode(bottom.x, bottom.y), node));
            edges.add(new MyEdge(node, findNode(bottom.x, bottom.y)));
        }
    }

    public void turnRight() {
        if (currDir == DIR_LEFT) {
            currDir = DIR_TOP;
        } else if (currDir == DIR_TOP) {
            currDir = DIR_RIGHT;
        } else if (currDir == DIR_RIGHT) {
            currDir = DIR_BOTTOM;
        } else if (currDir == DIR_BOTTOM) {
            currDir = DIR_LEFT;
        }
    }

    public void turnLeft() {
        if (currDir == DIR_LEFT) {
            currDir = DIR_BOTTOM;
        } else if (currDir == DIR_TOP) {
            currDir = DIR_LEFT;
        } else if (currDir == DIR_RIGHT) {
            currDir = DIR_TOP;
        } else if (currDir == DIR_BOTTOM) {
            currDir = DIR_RIGHT;
        }
    }

    public void goForward() {
        MyNode nextNode = createNextNode();
        for (MyNode node : nodes) {
            if (node.x == nextNode.x && node.y == nextNode.y) {
                currNode = node;
            }
        }
    }

    public void wasBumped() {
        currNode.isBump = true;
        if (currDir == DIR_LEFT) {
            currNode = findNode(currNode.x - 1, currNode.y);
        } else if (currDir == DIR_TOP) {
            currNode = findNode(currNode.x, currNode.y - 1);
        } else if (currDir == DIR_RIGHT) {
            currNode = findNode(currNode.x + 1, currNode.y);
        } else if (currDir == DIR_BOTTOM) {
            currNode = findNode(currNode.x, currNode.y + 1);
        }
    }

    public class MyNode {
        public int x, y;
        public MyNode left, top, right, bottom;

        public boolean isVisited;

        public boolean isSafe;

        public boolean isPit;
        public boolean isBreeze;

        public boolean isWumpus;
        public boolean isStench;

        public boolean isBump;

        public MyNode() {

        }

        public MyNode(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public class MyEdge {
        public MyNode u, v;

        public MyEdge(MyNode u, MyNode v) {
            this.u = u;
            this.v = v;
        }
    }

}
