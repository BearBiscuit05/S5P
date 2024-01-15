package s5p.game;

import s5p.cluster.StreamCluster;
import s5p.properties.GlobalConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ClusterPackGame  {

    private HashMap<Integer, Integer> clusterPartition;
    private HashMap<Integer, Integer> clusterPartition_B;
    private HashMap<Integer, Integer> clusterPartition_S;
    private HashMap<Integer, Integer> cutCostValue;
    private HashMap<Integer, Integer> cutCostValue_B;
    private HashMap<Integer, Integer> cutCostValue_S;
    private HashMap<Integer, Integer> cutCostValue_hybrid_B;
    private HashMap<Integer, Integer> cutCostValue_hybrid_S;
    private HashMap<Integer, HashSet<Integer>> clusterNeighbours;
    private HashMap<Integer, HashSet<Integer>> clusterNeighbours_B;
    private HashMap<Integer, HashSet<Integer>> clusterNeighbours_S;
    private HashMap<Integer, HashSet<Integer>> clusterNeighbours_hybrid_B;
    private HashMap<Integer, HashSet<Integer>> clusterNeighbours_hybrid_S;
    private final double[] partitionLoad;
    private List<Integer> clusterList;
    private StreamCluster streamCluster;
    private StreamCluster streamCluster_B = null;
    private StreamCluster streamCluster_S = null;
    private List<Integer>  clusterList_S = null;
    private List<Integer>  clusterList_B = null;
    private double beta = 0.0;
    private double beta_B = 0.0;
    private double beta_S = 0.0;
    private int roundCnt;
    private String graphType;
    private int gap = 0;

    public ClusterPackGame(StreamCluster streamCluster, List<Integer> clusterList, String graphType) {
        this.graphType = graphType;
        this.clusterPartition = new HashMap<>();
        this.streamCluster = streamCluster;
        this.clusterList = clusterList;
        cutCostValue = new HashMap<>();
        partitionLoad = new double[GlobalConfig.getPartitionNum()];
        clusterNeighbours = new HashMap<>();
    }

    public ClusterPackGame(StreamCluster streamCluster, List<Integer> clusterList_B, List<Integer> clusterList_S, String graphType) {
        this.streamCluster = streamCluster;
        this.clusterPartition_B = new HashMap<>();
        this.clusterPartition_S = new HashMap<>();
        this.clusterList = streamCluster.getClusterList();
        cutCostValue_B = new HashMap<>();
        cutCostValue_S = new HashMap<>();
        cutCostValue_hybrid_B = new HashMap<>();
        cutCostValue_hybrid_S = new HashMap<>();

        partitionLoad = new double[GlobalConfig.getPartitionNum()];
        this.clusterNeighbours_B = new HashMap<>();
        this.clusterNeighbours_S = new HashMap<>();
        this.clusterNeighbours_hybrid_S = new HashMap<>();
        this.clusterNeighbours_hybrid_B = new HashMap<>();
        this.clusterList_B = clusterList_B;
        this.clusterList_S = clusterList_S;
        this.graphType = graphType;

    }

    public String getGraphType() {
        return graphType;
    }

    public ClusterPackGame(StreamCluster streamCluster, List<Integer> clusterList) {
        this.clusterPartition = new HashMap<>();
        this.streamCluster = streamCluster;
        this.clusterList = clusterList;
        cutCostValue = new HashMap<>();
        partitionLoad = new double[GlobalConfig.getPartitionNum()];
        clusterNeighbours = new HashMap<>();
    }

    
    public ClusterPackGame(StreamCluster streamCluster_B, List<Integer> clusterList_B, StreamCluster streamCluster_S, List<Integer> clusterList_S) {
        this.clusterPartition = new HashMap<>();
        this.streamCluster_B = streamCluster_B;
        this.streamCluster_S = streamCluster_S;
        this.clusterList_B = clusterList_B;
        this.clusterList_S = clusterList_S;
        cutCostValue = new HashMap<>();
        partitionLoad = new double[GlobalConfig.getPartitionNum()];
        clusterNeighbours = new HashMap<>();
        this.gap = clusterList_B.size();
    }

    
    public void initGame() {
        int partition = 0;
        for (Integer clusterId : clusterList) {
            double minLoad = GlobalConfig.getECount();
            for (int i = 0; i < GlobalConfig.getPartitionNum(); i++) {
                if (partitionLoad[i] < minLoad) {
                    minLoad = partitionLoad[i];
                    partition = i;
                }
            }
            clusterPartition.put(clusterId, partition);
            partitionLoad[partition] += streamCluster.getEdgeNum(clusterId, clusterId, graphType);
        }

        double sizePart = 0.0, cutPart = 0.0;
        for (Integer cluster1 : clusterList) {
            sizePart += streamCluster.getEdgeNum(cluster1, cluster1, graphType);

            for (Integer cluster2 : clusterList) {
                int innerCut = 0;
                int outerCut = 0;
                if (!cluster1.equals(cluster2)) {
                    innerCut = streamCluster.getEdgeNum(cluster2, cluster1, graphType);
                    outerCut = streamCluster.getEdgeNum(cluster1, cluster2, graphType);
                    if (innerCut != 0 || outerCut != 0) {
                        if (!clusterNeighbours.containsKey(cluster1))
                            clusterNeighbours.put(cluster1, new HashSet<>());
                        if (!clusterNeighbours.containsKey(cluster2))
                            clusterNeighbours.put(cluster2, new HashSet<>());

                        clusterNeighbours.get(cluster1).add(cluster2);
                        clusterNeighbours.get(cluster2).add(cluster1);
                    }
                    cutPart += outerCut;
                }

                if (!cutCostValue.containsKey(cluster1)) cutCostValue.put(cluster1, 0);
                cutCostValue.put(cluster1, cutCostValue.get(cluster1) + innerCut + outerCut);
            }
        }

        this.beta = GlobalConfig.getPartitionNum() * GlobalConfig.getPartitionNum() * cutPart / (sizePart * sizePart);
    }

    public void initGameDouble() {

        int partition = 0;
        for (Integer clusterId : clusterList_B) {
            double minLoad = GlobalConfig.getECount();
            for (int i = 0; i < GlobalConfig.getPartitionNum(); i++) {
                if (partitionLoad[i] < minLoad) {
                    minLoad = partitionLoad[i];
                    partition = i;
                }
            }
            clusterPartition_B.put(clusterId, partition);
            partitionLoad[partition] += streamCluster.getEdgeNum(clusterId, clusterId, "B");
        }

        for (Integer clusterId : clusterList_S) {
            double minLoad = GlobalConfig.getECount();
            for (int i = 0; i < GlobalConfig.getPartitionNum(); i++) {
                if (partitionLoad[i] < minLoad) {
                    minLoad = partitionLoad[i];
                    partition = i;
                }
            }
            clusterPartition_S.put(clusterId, partition);
            partitionLoad[partition] += streamCluster.getEdgeNum(clusterId, clusterId, "S");
        }

        double sizePart_B = 0.0, cutPart_B = 0.0;
        double sizePart_S = 0.0, cutPart_S = 0.0;
        for (Integer cluster1 : clusterList_B) {
            sizePart_B += streamCluster.getEdgeNum(cluster1, cluster1, "B");
           for (Integer cluster2 : clusterList_B) {
               int innerCut = 0;
               int outerCut = 0;
               if (!cluster1.equals(cluster2)) {
                   innerCut = streamCluster.getEdgeNum(cluster2, cluster1, "B");
                   outerCut = streamCluster.getEdgeNum(cluster1, cluster2, "B");

                   if (innerCut != 0 || outerCut != 0) {
                       if (!clusterNeighbours_B.containsKey(cluster1))
                           clusterNeighbours_B.put(cluster1, new HashSet<>());
                       if (!clusterNeighbours_B.containsKey(cluster2))
                           clusterNeighbours_B.put(cluster2, new HashSet<>());

                       clusterNeighbours_B.get(cluster1).add(cluster2);
                       clusterNeighbours_B.get(cluster2).add(cluster1);
                   }
                   cutPart_B += outerCut;
               }

               if (!cutCostValue_B.containsKey(cluster1)) cutCostValue_B.put(cluster1, 0);
               cutCostValue_B.put(cluster1, cutCostValue_B.get(cluster1) + innerCut + outerCut);
           }

           for (Integer cluster2 : clusterList_S) {
               int innerCut = 0;
               int outerCut = 0;
               if (!cluster1.equals(cluster2)) {
                   innerCut = streamCluster.getEdgeNum(cluster2, cluster1, "hybrid");
                   outerCut = streamCluster.getEdgeNum(cluster1, cluster2,"hybrid");
                   if (innerCut != 0 || outerCut != 0) {
                       if (!clusterNeighbours_hybrid_B.containsKey(cluster1))
                           clusterNeighbours_hybrid_B.put(cluster1, new HashSet<>());
                       clusterNeighbours_hybrid_B.get(cluster1).add(cluster2);
                   }
               }

               if (!cutCostValue_hybrid_B.containsKey(cluster1)) cutCostValue_hybrid_B.put(cluster1, 0);
               cutCostValue_hybrid_B.put(cluster1, cutCostValue_hybrid_B.get(cluster1) + innerCut + outerCut);
           }
        }

        this.beta_B = GlobalConfig.eCount_B * GlobalConfig.eCount_B * cutPart_B / (sizePart_B * sizePart_B);

        for (Integer cluster1 : clusterList_S) {
            sizePart_S += streamCluster.getEdgeNum(cluster1, cluster1, "S");
            for (Integer cluster2 : clusterList_B) {
                int innerCut = 0;
                int outerCut = 0;
                if (!cluster1.equals(cluster2)) {
                    innerCut = streamCluster.getEdgeNum(cluster2, cluster1, "hybrid");
                    outerCut = streamCluster.getEdgeNum(cluster1, cluster2,"hybrid");
                    if (innerCut != 0 || outerCut != 0) {
                        if (!clusterNeighbours_hybrid_S.containsKey(cluster1))
                            clusterNeighbours_hybrid_S.put(cluster1, new HashSet<>());
                        clusterNeighbours_hybrid_S.get(cluster1).add(cluster2);
                    }
                }
               if (!cutCostValue_hybrid_S.containsKey(cluster1)) cutCostValue_hybrid_S.put(cluster1, 0);
               cutCostValue_hybrid_S.put(cluster1, cutCostValue_hybrid_S.get(cluster1) + innerCut + outerCut);
           }
        }
        this.beta_S = GlobalConfig.eCount_S * GlobalConfig.eCount_S * cutPart_S / (sizePart_S * sizePart_S);
    }
    private double computeCost(int clusterId, int partition) {
        double loadPart = 0.0;
        int edgeCutPart = cutCostValue.get(clusterId);
        int old_partition = clusterPartition.get(clusterId);
        loadPart = partitionLoad[old_partition];
        if (partition != old_partition)
            loadPart = partitionLoad[partition] + streamCluster.getEdgeNum(clusterId, clusterId, graphType);

        if (clusterNeighbours.containsKey(clusterId)) {
            for (Integer neighbour : clusterNeighbours.get(clusterId)) {
                if (clusterPartition.get(neighbour) == partition)
                    edgeCutPart = edgeCutPart - streamCluster.getEdgeNum(clusterId, neighbour, graphType)
                            - streamCluster.getEdgeNum(neighbour, clusterId, graphType);
            }
        }

        double alpha = GlobalConfig.getAlpha(), k = GlobalConfig.getPartitionNum();
        double m = streamCluster.getEdgeNum(clusterId, clusterId, graphType);
        return alpha * beta / k * loadPart * m + (1 - alpha) / 2 * edgeCutPart;
    }

    private double computeCost(int clusterId, int partition, String type) {
        if(type.equals("B")) {
            double loadPart = 0.0;
            int edgeCutPart = 0;
            if(cutCostValue_B.containsKey(clusterId))   edgeCutPart = cutCostValue_B.get(clusterId);
            int old_partition = clusterPartition_B.get(clusterId);
            loadPart = partitionLoad[old_partition];
            if (partition != old_partition)
                loadPart = partitionLoad[partition] + streamCluster.getEdgeNum(clusterId, clusterId, "B");
            double alpha = GlobalConfig.getAlpha(), k = GlobalConfig.getPartitionNum();
            double m = streamCluster.getEdgeNum(clusterId, clusterId, "B");
            int edgeCutPart_hybrid_B = 0;
            if(cutCostValue_hybrid_B.containsKey(clusterId)) cutCostValue_hybrid_B.get(clusterId);
            if (clusterNeighbours_hybrid_B.containsKey(clusterId)) {
                for (Integer neighbour : clusterNeighbours_hybrid_B.get(clusterId)) {
                    if (clusterPartition_S.get(neighbour) == partition)
                        edgeCutPart_hybrid_B = edgeCutPart_hybrid_B - streamCluster.getEdgeNum(clusterId, neighbour, "hybrid")
                                - streamCluster.getEdgeNum(neighbour, clusterId, "hybrid");
                }
            }

            double Cost = (0.3 * beta_B / k * loadPart * m + 0.3 / 2 * edgeCutPart + 0.4 * edgeCutPart_hybrid_B / 2.0) ;
            return Cost;
        } else if(type.equals("S")) {
            double loadPart = 0.0;
            int edgeCutPart = 0;
            if(cutCostValue_S.containsKey(clusterId))   cutCostValue_S.get(clusterId);
            int old_partition = clusterPartition_S.get(clusterId);
            loadPart = partitionLoad[old_partition];
            if (partition != old_partition)
                loadPart = partitionLoad[partition] + streamCluster.getEdgeNum(clusterId, clusterId, "S");
            if (clusterNeighbours_S.containsKey(clusterId)) {
                for (Integer neighbour : clusterNeighbours_S.get(clusterId)) {
                    if (clusterPartition_S.get(neighbour) == partition)
                        edgeCutPart = edgeCutPart - streamCluster.getEdgeNum(clusterId, neighbour, "S")
                                - streamCluster.getEdgeNum(neighbour, clusterId, "S");
                }
            }

            double alpha = GlobalConfig.getAlpha(), k = GlobalConfig.getPartitionNum();
            double m = streamCluster.getEdgeNum(clusterId, clusterId, "S");


            int edgeCutPart_hybrid_S = 0;
            if(cutCostValue_hybrid_S.containsKey(clusterId)) cutCostValue_hybrid_S.get(clusterId);
            if (clusterNeighbours_hybrid_S.containsKey(clusterId)) {
                for (Integer neighbour : clusterNeighbours_hybrid_S.get(clusterId)) {
                    if (clusterPartition_B.get(neighbour) == partition)
                        edgeCutPart_hybrid_S = edgeCutPart_hybrid_S - streamCluster.getEdgeNum(clusterId, neighbour, "hybrid")
                                - streamCluster.getEdgeNum(neighbour, clusterId, "hybrid");
                }
            }
            double Cost = (0.3 * beta_S / k * loadPart * m + 0.3 / 2 * edgeCutPart + 0.4 * edgeCutPart_hybrid_S / 2.0);
            return Cost;
        } else {
            System.out.println("ComputeCost Error!");
            return 0.0;
        }

    }
    public void startGame() {
        boolean finish = false;
        while (!finish) {
            finish = true;
            for (Integer clusterId : clusterList) {
                double minCost = Double.MAX_VALUE;
                int minPartition = clusterPartition.get(clusterId);

                if(graphType.equals("B")) {
                    for (int j = 0 ; j < GlobalConfig.getPartitionNum() / 2; j++) {
                        double cost = computeCost(clusterId, j);
                        if (cost <= minCost) {
                            minCost = cost;
                            minPartition = j;
                        }
                    }
                } else if(graphType.equals("S")){
                    for (int j = GlobalConfig.getPartitionNum() - 1; j >= GlobalConfig.getPartitionNum() / 2; j--) {
                        double cost = computeCost(clusterId, j);
                        if (cost <= minCost) {
                            minCost = cost;
                            minPartition = j;
                        }
                    }
                }


                if (minPartition != clusterPartition.get(clusterId)) {
                    finish = false;
                    partitionLoad[minPartition] += streamCluster.getEdgeNum(clusterId, clusterId, graphType);
                    partitionLoad[clusterPartition.get(clusterId)] -= streamCluster.getEdgeNum(clusterId, clusterId, graphType);
                    clusterPartition.put(clusterId, minPartition);
                }
            }
            roundCnt++;

        }
    }

    public void startGameDouble() {
        boolean finish_B = false;
        boolean finish_S = false;
        boolean isChangeB = true;
        boolean isChangeS = true;
        while (true) {
            finish_B = true;
            finish_S = true;
            for (Integer clusterId : clusterList_B) {
                double minCost = Double.MAX_VALUE;
                int minPartition = clusterPartition_B.get(clusterId);
                for (int j = 0; j < GlobalConfig.getPartitionNum() / 2; j++) {
                    double cost = computeCost(clusterId, j, "B");
                    if (cost <= minCost) {
                        minCost = cost;
                        minPartition = j;
                    }
                }

                if (minPartition != clusterPartition_B.get(clusterId)) {
                    finish_B = false;
                    partitionLoad[minPartition] += streamCluster.getEdgeNum(clusterId, clusterId, "B");
                    partitionLoad[clusterPartition_B.get(clusterId)] -= streamCluster.getEdgeNum(clusterId, clusterId, "B");
                    clusterPartition_B.put(clusterId, minPartition);
                }
            }

            for (Integer clusterId : clusterList_S) {
                double minCost = Double.MAX_VALUE;
                int minPartition = clusterPartition_S.get(clusterId);
                for (int j = GlobalConfig.getPartitionNum() - 1; j >= GlobalConfig.getPartitionNum() / 2; j--) {
                    double cost = computeCost(clusterId, j, "S");
                    if (cost <= minCost) {
                        minCost = cost;
                        minPartition = j;
                    }
                }

                if (minPartition != clusterPartition_S.get(clusterId)) {
                    finish_S = false;
                    partitionLoad[minPartition] += streamCluster.getEdgeNum(clusterId, clusterId, "S");
                    partitionLoad[clusterPartition_S.get(clusterId)] -= streamCluster.getEdgeNum(clusterId, clusterId, "S");
                    clusterPartition_S.put(clusterId, minPartition);
                }
            }
            roundCnt++;
            if(finish_B && finish_S) {
                break;
            }
        }
    }

    public int getRoundCnt() {
        return roundCnt;
    }

    public HashMap<Integer, Integer> getClusterPartition() {
        return clusterPartition;
    }

    public HashMap<Integer, Integer> getClusterPartition_B() {
        return clusterPartition_B;
    }
    public HashMap<Integer, Integer> getClusterPartition_S() {
        return clusterPartition_S;
    }
}
