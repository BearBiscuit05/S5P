package s5p.graph;

public class Edge {
    private final int srcVId;
    private final int destVId;
    private int weight;

    public Edge(int srcVId, int destVId, int weight) {
        this.srcVId = srcVId;
        this.destVId = destVId;
        this.weight = weight;
    }

    public int getSrcVId() {
        return srcVId;
    }

    public int getDestVId() {
        return destVId;
    }

    public int getWeight() {
        return weight;
    }

    public void addWeight(){
        weight++;
    }
}
