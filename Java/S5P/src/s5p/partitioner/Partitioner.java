package s5p.partitioner;

import s5p.cluster.StreamCluster;
import s5p.game.ClusterPackGame;
import s5p.graph.Edge;
import s5p.graph.Graph;
import s5p.properties.GlobalConfig;
import s5p.thread.ClusterGameTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

public class Partitioner {
    private Graph graph;
    private StreamCluster streamCluster_S;
    private StreamCluster streamCluster;
    private StreamCluster streamCluster_B;
    private int gameRoundCnt = 0;
    private int[] partitionLoad;
    private int[] degree;
    private int[][] v2p;

    public Partitioner(StreamCluster streamCluster) {
        this.streamCluster = streamCluster;
        this.graph = streamCluster.getGraph();
        this.partitionLoad = new int[GlobalConfig.partitionNum];
        this.degree = streamCluster.getDegree();
        v2p = new int[graph.getVCount()][GlobalConfig.partitionNum];
    }

    public void performStep() {
        double maxLoad = (double) GlobalConfig.eCount / GlobalConfig.partitionNum * 1.1;
        processGraph(maxLoad);
    }

    private void processGraph(double maxLoad) {
        this.graph.readGraphFromFile();
        Edge edge;
        //for e(u,v) ∈ E do
        while ((edge = this.graph.readStep()) != null) {
            int src = edge.getSrcVId();
            int dest = edge.getDestVId();
            if(degree[src] >= GlobalConfig.getTao() *  GlobalConfig.getAverageDegree() && degree[dest] >= GlobalConfig.getTao() * GlobalConfig.getAverageDegree()) {
                //if e ∈ E(H) then
                this.clusterPartition = this.clusterPartition_B;
                int srcPartition = 0;
                int destPartition = 0;

                //Pu = C2P(Clu(u)) Clu(u) = V2CH(u). where Pu is srcPartition
                if(clusterPartition.containsKey(streamCluster.getClusterId(src, "B")) ){
                    srcPartition = clusterPartition.get(streamCluster.getClusterId(src, "B"));
                } else {
                    srcPartition = clusterPartition.get(streamCluster.getClusterId(src, "S"));
                }

                //Pv = C2P(Clu(v)) Clu(v) = V2CH(v). where Pv is destPartition
                if(clusterPartition.containsKey(streamCluster.getClusterId(dest, "B"))) {
                    destPartition = clusterPartition.get(streamCluster.getClusterId(dest, "B"));
                } else {
                    destPartition = clusterPartition.get(streamCluster.getClusterId(dest, "S"));
                }
                int edgePartition = -1;

                
                if (partitionLoad[srcPartition] > maxLoad && partitionLoad[destPartition] > maxLoad) {
                    //if Load(Pu) > L and Load(Pv) > L then Place e in a partition with available space
                    for (int i = 0; i < GlobalConfig.partitionNum; i++) {
                        if (partitionLoad[i] <= maxLoad) {
                            //this partition is available
                            edgePartition = i;
                            srcPartition = i;
                            destPartition = i;
                            break;
                        }
                    }
                } else if (partitionLoad[srcPartition] > partitionLoad[destPartition]) {
                    //else if Load(Pu) > Load(Pv) then place e into Pv
                    edgePartition = destPartition;
                    srcPartition = destPartition;
                } else {
                    //else  place e into Pu
                    edgePartition = srcPartition;
                    destPartition = srcPartition;
                }
                
                partitionLoad[edgePartition]++;
                v2p[src][srcPartition] = 1;
                v2p[dest][destPartition] = 1;
            } else {
                //if e ∈ E(L) then
                this.clusterPartition = this.clusterPartition_S;
                int srcPartition = 0;
                int destPartition = 0;

                //Pu = C2P(Clu(u)) Clu(u) = V2CT(u). where Pu is srcPartition
                if(clusterPartition.containsKey(streamCluster.getClusterId(src, "S"))) {
                    srcPartition = clusterPartition.get(streamCluster.getClusterId(src, "S"));
                } else {
                    srcPartition = clusterPartition.get(streamCluster.getClusterId(src, "B"));
                }

                //Pv = C2P(Clu(v)) Clu(v) = V2CT(v). where Pv is destPartition
                if(clusterPartition.containsKey(streamCluster.getClusterId(dest, "S"))) {
                    destPartition = clusterPartition.get(streamCluster.getClusterId(dest, "S"));
                } else {
                    destPartition = clusterPartition.get(streamCluster.getClusterId(dest, "B"));
                }
                int edgePartition = -1;

                if (partitionLoad[srcPartition] > maxLoad && partitionLoad[destPartition] > maxLoad) {
                    //if Load(Pu) > L and Load(Pv) > L then Place e in a partition with available space
                    for (int i = GlobalConfig.partitionNum - 1; i >= 0; i--) {
                        if (partitionLoad[i] <= maxLoad) {
                            //this partition is available
                            edgePartition = i;
                            srcPartition = i;
                            destPartition = i;
                            break;
                        }
                    }
                } else if (partitionLoad[srcPartition] > partitionLoad[destPartition]) {
                    //else if Load(Pu) > Load(Pv) then place e into Pv
                    edgePartition = destPartition;
                    srcPartition = destPartition;
                } else {
                    //else place e into Pu
                    edgePartition = srcPartition;
                    destPartition = srcPartition;
                }
                partitionLoad[edgePartition]++;

                v2p[src][srcPartition] = 1;
                v2p[dest][destPartition] = 1;
            }
            
        }
    }

    public int getGameRoundCnt() {
        return gameRoundCnt;
    }

    public HashMap<Integer, Integer> getClusterPartition() {
        return clusterPartition;
    }

    private HashMap<Integer, Integer> clusterPartition = new HashMap<>();
    private HashMap<Integer, Integer> clusterPartition_B = new HashMap<>();
    private HashMap<Integer, Integer> clusterPartition_S = new HashMap<>();
    public Partitioner() {

    }

    public void startStackelbergGame() {

        ExecutorService taskPoolCPG = Executors.newFixedThreadPool(GlobalConfig.getThreads());
        CompletionService<ClusterPackGame> completionServiceCPG  = new ExecutorCompletionService<>(taskPoolCPG);
        List<Integer> clusterList_B = streamCluster.getClusterList_B();
        List<Integer> clusterList_S = streamCluster.getClusterList_S();
        int clusterSize_B = clusterList_B.size();
        int clusterSize_S = clusterList_S.size();
        int taskNum_B = (clusterSize_B + GlobalConfig.getBatchSize() - 1) / GlobalConfig.getBatchSize();
        int taskNum_S = (clusterSize_S + GlobalConfig.getBatchSize() - 1) / GlobalConfig.getBatchSize();
        int i = 0, j = 0;

        //Streaming execution of mixed games, after executing head or tail, the remaining items still need to be executed
        //The Bound of the streaming execution process is determined by the batchSize in the configuration (taskNum)
        for(; i < taskNum_B && j < taskNum_S; i++, j++){
            completionServiceCPG.submit(new ClusterGameTask("hybrid", streamCluster, i, j));
        }
        for(; i < taskNum_B; ++i) {
            completionServiceCPG.submit(new ClusterGameTask("B", i, streamCluster));
        }
        for(; j < taskNum_S; ++j) {
            completionServiceCPG.submit(new ClusterGameTask("S", j, streamCluster));
        }


        //Combine the results of streaming games and output clusterPartition
        for(int p = 0; p < Math.max(taskNum_B, taskNum_S); p++){
            try{
                Future<ClusterPackGame> result = completionServiceCPG.take();
                ClusterPackGame game = result.get();
                if(game.getGraphType().equals("S")) {
                    clusterPartition_S.putAll(game.getClusterPartition());
                } else if(game.getGraphType().equals("B")) {
                    clusterPartition_B.putAll(game.getClusterPartition());
                } else if(game.getGraphType().equals("hybrid")) {
                    clusterPartition_S.putAll(game.getClusterPartition_S());
                    clusterPartition_B.putAll(game.getClusterPartition_B());
                }
                gameRoundCnt += game.getRoundCnt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        taskPoolCPG.shutdownNow();
    }

    public double getReplicateFactor() {
        int replicateTotal = 0;
        for (int[] ints : v2p) {
            for (int anInt : ints) {
                replicateTotal += anInt;
            }
        }
        return (double) replicateTotal / GlobalConfig.vCount;
    }

    public double getLoadBalance() {
        double maxLoad = 0.0;
        for (int i = 0; i < GlobalConfig.getPartitionNum(); i++) {
            if (maxLoad < partitionLoad[i]) {
                maxLoad = partitionLoad[i];
            }
        }
        return (double) GlobalConfig.getPartitionNum() / GlobalConfig.getECount() * maxLoad;
    }
}
