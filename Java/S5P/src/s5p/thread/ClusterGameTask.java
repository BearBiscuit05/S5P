package s5p.thread;

import s5p.cluster.StreamCluster;
import s5p.game.ClusterPackGame;
import s5p.properties.GlobalConfig;

import java.util.List;
import java.util.concurrent.Callable;

public class ClusterGameTask implements Callable<ClusterPackGame> {

    private StreamCluster streamCluster;
    private StreamCluster streamCluster_B = null;
    private StreamCluster streamCluster_S = null;
    private List<Integer> cluster;
    private List<Integer> cluster_B;
    private List<Integer> cluster_S;

    private final String graphType;

    public ClusterGameTask(String graphType, int taskId, StreamCluster streamCluster) {
        List<Integer> clusterList = (graphType.equals("B") ? streamCluster.getClusterList_B() : streamCluster.getClusterList_S());
        int batchSize = GlobalConfig.getBatchSize();
        int begin = batchSize * taskId;
        int end = Math.min(batchSize * (taskId + 1), clusterList.size());
        cluster = clusterList.subList(begin, end);
        this.streamCluster = streamCluster;
        this.graphType = graphType;
    }

    public ClusterGameTask(String graphType, StreamCluster streamCluster, int taskId_B, int taskId_S) {
        this.graphType = graphType;
        this.streamCluster = streamCluster;
        int batchSize = GlobalConfig.getBatchSize();
        int begin = batchSize * taskId_B;
        List<Integer> clusterList_B = streamCluster.getClusterList_B();
        List<Integer> clusterList_S = streamCluster.getClusterList_S();

        //Streaming execute B and S，config Bound
        int end = Math.min(batchSize * (taskId_B + 1), clusterList_B.size());
        cluster_B = clusterList_B.subList(begin, end);

        begin = batchSize * taskId_S;
        end = Math.min(batchSize * (taskId_S + 1), clusterList_S.size());
        cluster_S = clusterList_S.subList(begin, end);

    }

    @Override
    public ClusterPackGame call() {
        //Rewrite the call function, multithreading the initGame() operation of Stackeberg Game
        try {
            if(graphType.equals("hybrid")) {
                ClusterPackGame clusterPackGame = new ClusterPackGame(streamCluster, cluster_B, cluster_S, graphType);
                clusterPackGame.initGameDouble();
                return clusterPackGame;
            } else if (graphType.equals("B")){
                ClusterPackGame clusterPackGame = new ClusterPackGame(streamCluster, cluster, graphType);
                clusterPackGame.initGame();
                return clusterPackGame;
            } else if(graphType.equals("S")) {
                ClusterPackGame clusterPackGame = new ClusterPackGame(streamCluster, cluster, graphType);
                clusterPackGame.initGame();
                return clusterPackGame;
            } else {
                System.out.println("graphType error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
