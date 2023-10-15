package s5p.graph;

import s5p.graph.Edge;
import s5p.properties.GlobalConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class OriginGraph implements Graph {

    private final ArrayList<Edge> edgeList;
    private final int vCount;
    private final int eCount;
    private BufferedReader bufferedReader;
    private String graphpath = GlobalConfig.getInputGraphPath();
    public OriginGraph(String graphpath) {
        this.edgeList = new ArrayList<>();
        this.vCount = GlobalConfig.getVCount();
        this.eCount = GlobalConfig.getECount();
        this.graphpath = graphpath;
    }

    @Override
    public Edge readStep(){
        try {
            String line;
            if ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) return readStep();
                if (line.isEmpty()) return null;
                String[] edgeValues = line.split("\t");
                int srcVid = Integer.parseInt(edgeValues[0]);
                int destVid = Integer.parseInt(edgeValues[1]);
                if(srcVid == destVid) return readStep();
                return new Edge(srcVid, destVid, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void readGraphFromFile() {
        try {
            File file = new File(graphpath);
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEdge(int srcVId, int destVId) {
        Edge edge = new Edge(srcVId, destVId, 1);
        edgeList.add(edge);
    }

    @Override
    public ArrayList<Edge> getEdgeList() {
        return edgeList;
    }

    @Override
    public int getVCount() {
        return vCount;
    }

    @Override
    public int getECount() {
        return eCount;
    }

    @Override
    public void clear() {
        edgeList.clear();
    }
}
