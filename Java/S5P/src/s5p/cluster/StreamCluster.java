package s5p.cluster;


import s5p.graph.*;
import s5p.properties.GlobalConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.Collections;
import java.io.*;
public class StreamCluster {
    public int[] getDegree() {
        return degree;
    }

    public HashMap<Integer, Integer> getVolume_S() {
        return volume_S;
    }

    public Graph getGraph() {
        return graphB;
    }



    public List<Integer> getClusterList_S() {
        return clusterList_S;
    }

    public List<Integer> getClusterList_B() {
        return clusterList_B;
    }

    public double getMaxVolume() {
        return maxVolume;
    }

    public String getGraphType() {
        return graphType;
    }
    private int[] cluster;
    private int[] cluster_S;
    private int[] cluster_B;
    private int[] degree;
    private int[] degree_B;
    private int[] degree_S;

    private HashMap<Integer, Integer> volume;


    private HashMap<Integer, Integer> volume_S;
    private HashMap<Integer, Integer> volume_B;
    private HashMap<Integer, HashMap<Integer, Integer>> innerAndCutEdge;
    private HashMap<Integer, HashMap<Integer, Integer>> innerAndCutEdge_B;
    private HashMap<Integer, HashMap<Integer, Integer>> innerAndCutEdge_S;
    private HashMap<Integer, HashMap<Integer, Integer>> innerAndCutEdge_hybrid;
    private Graph graph;
    private Graph graphB;
    private Graph graphS;
    private  List<Integer> clusterList;

    public void setCluster(int[] cluster) {
        this.cluster = cluster;
    }

    public void setDegree(int[] degree) {
        this.degree = degree;
    }

    public void setVolume_S(HashMap<Integer, Integer> volume_S) {
        this.volume_S = volume_S;
    }

    public void setInnerAndCutEdge(HashMap<Integer, HashMap<Integer, Integer>> innerAndCutEdge) {
        this.innerAndCutEdge = innerAndCutEdge;
    }

    public void setClusterList(List<Integer> clusterList) {
        this.clusterList = clusterList;
    }

    public void setClusterList_S(List<Integer> clusterList_S) {
        this.clusterList_S = clusterList_S;
    }

    public void setClusterList_B(List<Integer> clusterList_B) {
        this.clusterList_B = clusterList_B;
    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }

    private List<Integer> clusterList_S;
    private List<Integer> clusterList_B;
    private double maxVolume;
    private int maxVolume_B;
    private int maxVolume_S;
    private String graphType;

    public StreamCluster() {

    }

    public StreamCluster(Graph graphB, Graph graphS) {
        this.cluster_B = new int[GlobalConfig.vCount];
        this.cluster_S = new int[GlobalConfig.vCount];
        this.graphB = graphB;
        this.graphS = graphS;
        this.volume_B = new HashMap<>();
        this.volume_S = new HashMap<>();

        this.maxVolume = GlobalConfig.getMaxClusterVolume();
        this.innerAndCutEdge = new HashMap<>();
        this.clusterList_S = new ArrayList<>();
        this.clusterList_B = new ArrayList<>();
        this.innerAndCutEdge_S = new HashMap<>();
        this.innerAndCutEdge_B = new HashMap<>();
        this.innerAndCutEdge_hybrid = new HashMap<>();
        this.degree = new int[GlobalConfig.vCount];
        this.degree_B = new int[GlobalConfig.vCount];
        this.degree_S = new int[GlobalConfig.vCount];
        calculateDegree();
    }

    private void calculateDegree() {
        graphB.readGraphFromFile();
        Edge edge;
        while((edge = graphB.readStep()) != null) {
            int src = edge.getSrcVId();
            int dest = edge.getDestVId();
            degree[src] ++;
            degree[dest] ++;
        }
        System.out.println("End CalculateDegree");
    }

    public void startSteamClusterB() {
        double averageDegree = GlobalConfig.getAverageDegree();
        int clusterID_B = 1;
        Edge edge;
        graphB.readGraphFromFile();
        while((edge = graphB.readStep()) != null) {
            int src = edge.getSrcVId();
            int dest = edge.getDestVId();

            //When both the src and dst degrees of an edge are greater than the threshold,
            //the node is placed in the ClusterB (Head) cluster
            if(degree[src] >= GlobalConfig.getTao() *  GlobalConfig.getAverageDegree() &&
             degree[dest] >= GlobalConfig.getTao() * GlobalConfig.getAverageDegree()) {

                //if 𝑉2𝐶𝐻 [𝑢] 𝑜𝑟 𝑉2𝐶𝐻 [𝑣] 𝑖𝑠 𝑁𝑈𝐿𝐿 then, Assign a new ID
                if (cluster_B[src] == 0) {
                    cluster_B[src] = clusterID_B++;
                }
                if (cluster_B[dest] == 0) {
                    cluster_B[dest] = clusterID_B++;
                }

                
                //update d(u) and d(v)
                this.degree_B[src]++;
                this.degree_B[dest]++;

                //Update 𝑣𝑜𝑙 by 𝑑 (𝑢) and 𝑑 (𝑣)
                if (!volume_B.containsKey(cluster_B[src])) {
                    volume_B.put(cluster_B[src], 0);
                }
                if (!volume_B.containsKey(cluster_B[dest])) {
                    volume_B.put(cluster_B[dest], 0);
                }

                //when a vertex is added to a cluster, increasing the volume of the cluster by 1.
                volume_B.put(cluster_B[src], volume_B.get(cluster_B[src]) + 1);
                volume_B.put(cluster_B[dest], volume_B.get(cluster_B[dest]) + 1);

                if(volume_B.get(cluster_B[src]) >= maxVolume)
                {
                    volume_B.put(cluster_B[src], volume_B.get(cluster_B[src]) - this.degree_B[src]);
                    cluster_B[src] = clusterID_B++;
                    volume_B.put(cluster_B[src], this.degree_B[src]);
                }

                if(volume_B.get(cluster_B[dest]) >= maxVolume)  
                {
                    volume_B.put(cluster_B[dest], volume_B.get(cluster_B[dest]) - this.degree_B[dest]);
                    cluster_B[dest] = clusterID_B++;
                    volume_B.put(cluster_B[dest], this.degree_B[dest]);
                }


                if (volume_B.get(cluster_B[src]) >= maxVolume || volume_B.get(cluster_B[dest]) >= maxVolume) continue;
                //if vol(V2CH[u]) and vol(V2CH[v]) < k then execute following (k is maxVolume)

                //i <-- argmin(vol(V2CH[z]) - d(z)) (z ∈ {u,v})
                //i.e. minVid <-- min(vol(cluster[u], vol(cluster[v])))
                int minVid = (volume_B.get(cluster_B[src]) < volume_B.get(cluster_B[dest]) ? src : dest);

                //j <-- z∈ {u, v}:z≠ i
                //i.e. maxVid <-- (src or dest that not minVid)
                int maxVid = (src == minVid ? dest: src);

                
                if ((volume_B.get(cluster_B[maxVid]) + this.degree_B[minVid]) <= maxVolume) {
                    //if vol(V2CH[j])+ d(i)< k then

                    //vol(V2CH[j]) <- vol(V2CH[j])+d(i)
                    volume_B.put(cluster_B[maxVid], volume_B.get(cluster_B[maxVid]) + this.degree_B[minVid]);
                    //vol(V2CH[i]) <- vol(V2CH[i])-d(i)
                    volume_B.put(cluster_B[minVid], volume_B.get(cluster_B[minVid]) - this.degree_B[minVid]);


                    if (volume_B.get(cluster_B[minVid]) == 0) 
                        volume_B.remove(cluster_B[minVid]);

                    //V2CH[i] <- V2CH[j]
                    cluster_B[minVid] = cluster_B[maxVid];
                }

            }  
        }      
        clusterList_B = new ArrayList<>(volume_B.keySet());
    }


       
    public void startSteamClusterS() {    
        double averageDegree = GlobalConfig.getAverageDegree();
        int clusterID_S = 1;
        graphS.readGraphFromFile();
        Edge edge;
        while((edge = graphS.readStep()) != null) {
            int src = edge.getSrcVId();
            int dest = edge.getDestVId();

            //When both the src and dst degrees of an edge are smaller than the threshold,
            //the node is placed in the ClusterS (Tail) cluster
            if(!(degree[src] >= GlobalConfig.getTao() *  GlobalConfig.getAverageDegree() &&
             degree[dest] >= GlobalConfig.getTao() * GlobalConfig.getAverageDegree()))  {

                //if 𝑉2𝐶T [𝑢] 𝑜𝑟 𝑉2𝐶T [𝑣] 𝑖𝑠 𝑁𝑈𝐿𝐿 then, Assign a new ID
                if (cluster_S[src] == 0) {
                    cluster_S[src] = clusterID_S++;
                }
                if (cluster_S[dest] == 0) {
                    cluster_S[dest] = clusterID_S++;
                }

                //update d(u) and d(v)
                this.degree_S[src]++;
                this.degree_S[dest]++;
                
                //Update 𝑣𝑜𝑙 by 𝑑 (𝑢) and 𝑑 (𝑣)
                if (!volume_S.containsKey(cluster_S[src])) {
                    volume_S.put(cluster_S[src], 0);
                }
                if (!volume_S.containsKey(cluster_S[dest])) {
                    volume_S.put(cluster_S[dest], 0);
                }

                //when a vertex is added to a cluster, increasing the volume of the cluster by 1.
                volume_S.put(cluster_S[src], volume_S.get(cluster_S[src]) + 1);
                volume_S.put(cluster_S[dest], volume_S.get(cluster_S[dest]) + 1);

                if(volume_S.get(cluster_S[src]) >= maxVolume)
                {
                    volume_S.put(cluster_S[src], volume_S.get(cluster_S[src]) - this.degree_S[src]);
                    cluster_S[src] = clusterID_S++;
                    volume_S.put(cluster_S[src], this.degree_S[src]);
                }

                if(volume_S.get(cluster_S[dest]) >= maxVolume)
                {
                    volume_S.put(cluster_S[dest], volume_S.get(cluster_S[dest]) - this.degree_S[dest]);
                    cluster_S[dest] = clusterID_S++;
                    volume_S.put(cluster_S[dest], this.degree_S[dest]);
                }
                if (volume_S.get(cluster_S[src]) >= maxVolume || volume_S.get(cluster_S[dest]) >= maxVolume) continue;
                //if vol(V2CT[u]) and vol(V2CT[v]) < k then execute following (k is maxVolume)

                //i <-- argmin(vol(V2CH[z]) - d(z)) (z ∈ {u,v})
                //i.e. minVid <-- min(vol(cluster[u], vol(cluster[v])))
                int minVid = (volume_S.get(cluster_S[src]) < volume_S.get(cluster_S[dest]) ? src : dest);
                
                //j <-- z∈ {u, v}:z≠ i
                //i.e. maxVid <-- (src or dest that not minVid)
                int maxVid = (src == minVid ? dest : src);

                if ((volume_S.get(cluster_S[maxVid]) + this.degree_S[minVid]) <= maxVolume) {
                    //if vol(V2CT[j])+ d(i)< k then
                    
                    //vol(V2CT[j]) <- vol(V2CT[j])+d(i)
                    volume_S.put(cluster_S[maxVid], volume_S.get(cluster_S[maxVid]) + this.degree_S[minVid]);
                    
                    //vol(V2CT[i]) <- vol(V2CT[i])-d(i)
                    volume_S.put(cluster_S[minVid], volume_S.get(cluster_S[minVid]) - this.degree_S[minVid]);
                    if (volume_S.get(cluster_S[minVid]) == 0) volume_S.remove(cluster_S[minVid]);
                    
                    //V2CT[i] <- V2CT[j]
                    cluster_S[minVid] = cluster_S[maxVid];
                }
            }

        }

        clusterList_S = new ArrayList<>(volume_S.keySet());
    }

    private void setUpIndex() {

        List<HashMap.Entry<Integer, Integer>> sortList_B = new ArrayList<HashMap.Entry<Integer, Integer>>(volume_B.entrySet());
        for (java.util.Map.Entry<Integer, Integer> integerIntegerEntry : sortList_B) {
            if (integerIntegerEntry.getValue() == 0) {
                continue;
            }
            this.clusterList_B.add(integerIntegerEntry.getKey());
        }
        volume_B.clear();

        List<HashMap.Entry<Integer, Integer>> sortList_S = new ArrayList<HashMap.Entry<Integer, Integer>>(volume_S.entrySet());
        for (java.util.Map.Entry<Integer, Integer> integerIntegerEntry : sortList_S) {
            if (integerIntegerEntry.getValue() == 0) {
                continue;
            }
            this.clusterList_S.add(integerIntegerEntry.getKey());
        }
        volume_S.clear();
        System.gc();
    }



    public void computeHybridInfo() {
        graphB.readGraphFromFile();
        Edge edge;
        while((edge = graphB.readStep()) != null){
            int src = edge.getSrcVId();
            int dest = edge.getDestVId();
            int oldValue = 0;
            if(degree[src] >= GlobalConfig.tao * GlobalConfig.getAverageDegree() && degree[dest] >= GlobalConfig.tao * GlobalConfig.getAverageDegree()) {
                if (!innerAndCutEdge_B.containsKey(cluster_B[src]))
                    innerAndCutEdge_B.put(cluster_B[src], new HashMap<>());
                if (!innerAndCutEdge_B.get(cluster_B[src]).containsKey(cluster_B[dest]))
                    innerAndCutEdge_B.get(cluster_B[src]).put(cluster_B[dest], 0);
                else continue;
                oldValue = innerAndCutEdge_B.get(cluster_B[src]).get(cluster_B[dest]);
                innerAndCutEdge_B.get(cluster_B[src]).put(cluster_B[dest], oldValue + edge.getWeight());
            }     
            else  {
                if (!innerAndCutEdge_S.containsKey(cluster_S[src]))
                    innerAndCutEdge_S.put(cluster_S[src], new HashMap<>());
                if (!innerAndCutEdge_S.get(cluster_S[src]).containsKey(cluster_S[dest])) {
                    innerAndCutEdge_S.get(cluster_S[src]).put(cluster_S[dest], 0);
                    oldValue = innerAndCutEdge_S.get(cluster_S[src]).get(cluster_S[dest]);
                    innerAndCutEdge_S.get(cluster_S[src]).put(cluster_S[dest], oldValue + edge.getWeight());
                }
                if(cluster_B[src] != 0) {
                    if (!innerAndCutEdge_hybrid.containsKey(cluster_S[src]))
                        innerAndCutEdge_hybrid.put(cluster_S[src], new HashMap<>());
                    if (!innerAndCutEdge_hybrid.get(cluster_S[src]).containsKey(cluster_B[src])) {
                        innerAndCutEdge_hybrid.get(cluster_S[src]).put(cluster_B[src], 0);
                        oldValue = innerAndCutEdge_hybrid.get(cluster_S[src]).get(cluster_B[src]);
                        innerAndCutEdge_hybrid.get(cluster_S[src]).put(cluster_B[src], oldValue + 1);
                    }
                }
                if(cluster_B[dest] != 0) {
                    if (!innerAndCutEdge_hybrid.containsKey(cluster_S[dest]))
                        innerAndCutEdge_hybrid.put(cluster_S[dest], new HashMap<>());
                    if (!innerAndCutEdge_hybrid.get(cluster_S[dest]).containsKey(cluster_B[dest])) {
                        innerAndCutEdge_hybrid.get(cluster_S[dest]).put(cluster_B[dest], 0);
                        oldValue = innerAndCutEdge_hybrid.get(cluster_S[dest]).get(cluster_B[dest]);
                        innerAndCutEdge_hybrid.get(cluster_S[dest]).put(cluster_B[dest], oldValue + 1);
                    }
                }
            }
        }
        volume_B.clear();
        volume_S.clear();
        System.gc();
    }

    public int getClusterId(int id, String graphType) {
        if(graphType.equals("S"))
            return cluster_S[id];
        return cluster_B[id];
    }

    public void PrintInfomation(){
        for(int i = 0; i < GlobalConfig.vCount; ++i) {
            System.out.println("vertex: " + i + ",clusterID: " + cluster[i]);
        }
        for(int i = 0; i < clusterList.size(); i++){
            System.out.println("clusterList: " + i + ": " + clusterList.get(i));
        }

        innerAndCutEdge.forEach((key1, value1) -> {

            System.out.print(key1.toString());
            System.out.print(" ");
            value1.forEach((key2, value2) ->{
                System.out.print(key2.toString());
                System.out.print(" ");
                System.out.print(value2.toString());
                System.out.print(" ");
            });
            System.out.print("\n");
        });

    }
    public List<Integer> getClusterList() {
        return clusterList;
    }


    public int[]  getCluster() {
        return cluster;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getInnerAndCutEdge() {
        return innerAndCutEdge;
    }

    public int getEdgeNum(int cluster1, int cluster2) {
        if(!innerAndCutEdge.containsKey(cluster1) || !innerAndCutEdge.get(cluster1).containsKey(cluster2)) return 0;

        return innerAndCutEdge.get(cluster1).get(cluster2);
    }

    public int getEdgeNum(int cluster1, int cluster2, String type) {
        if(type.equals("B")) {
            if(!innerAndCutEdge_B.containsKey(cluster1) || !innerAndCutEdge_B.get(cluster1).containsKey(cluster2)) return 0;
            return innerAndCutEdge_B.get(cluster1).get(cluster2);
        } else if(type.equals("S")) {
            if(!innerAndCutEdge_S.containsKey(cluster1) || !innerAndCutEdge_S.get(cluster1).containsKey(cluster2)) return 0;
            return innerAndCutEdge_S.get(cluster1).get(cluster2);
        } else if(type.equals("hybrid")) {
            if(!innerAndCutEdge_hybrid.containsKey(cluster1) || !innerAndCutEdge_hybrid.get(cluster1).containsKey(cluster2)) return 0;
            return innerAndCutEdge_hybrid.get(cluster1).get(cluster2);
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder volumeStr = new StringBuilder();
        StringBuilder clusterStr = new StringBuilder();
        volume.forEach((k, v) -> {
            volumeStr.append("cluster ").append(k).append(" volume: ").append(v).append("\n");
        });

        for (int i = 0; i < graph.getVCount(); i++) {
            clusterStr.append("vid : ").append(i).append(" cluster: ").append(cluster[i]).append("\n");
        }

        return volumeStr.toString() + clusterStr.toString();
    }


    public void outputClusterlistDistribution() throws IOException {
        HashMap<Integer, Integer> bigCluster = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> smallCluster = new HashMap<Integer, Integer>();
        for(int i = 0; i < clusterList_B.size(); ++i) {
            if(!bigCluster.containsKey(clusterList_B.get(i))) {
                bigCluster.put(clusterList_B.get(i), 0);
            }
            bigCluster.put(clusterList_B.get(i), bigCluster.get(clusterList_B.get(i)) + 1);

        }
        for(int i = 0; i < clusterList_S.size(); ++i) {
            if(!smallCluster.containsKey(clusterList_S.get(i))) {
                smallCluster.put(clusterList_S.get(i), 0);
            }
            smallCluster.put(clusterList_S.get(i), smallCluster.get(clusterList_S.get(i)) + 1);
        }
        BufferedWriter bw_B = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../../../../../graphdataset/com-orkut/com-orkut.ungraph.txt.bigdistrbution.csv")));
        BufferedWriter bw_S = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("../../../../../graphdataset/com-orkut/com-orkut.ungraph.txt.smalldistrbution.csv")));
        int lineNum_B = 0;
        int lineNum_S = 0;
        bw_B.write("size" + "," + "count" + "\n");
        bw_B.flush();
        bw_S.write("size" + "," + "count" + "\n");
        bw_S.flush();
        bigCluster.forEach((key, value) -> {
            try {
                bw_B.write(key + "," + value + "\n");
                bw_B.flush();
            } catch (Exception e) {

            }
        });

        smallCluster.forEach((key, value) -> {
            try {
                bw_S.write(key + "," + value + "\n");
                bw_S.flush();
            } catch (Exception e) {

            }

        });
        bw_B.close();
        bw_S.close();
        System.exit(-1);
    }

}
