package s5p.main;

import s5p.cluster.StreamCluster;
import s5p.graph.Graph;
import s5p.graph.OriginGraph;
import s5p.partitioner.Partitioner;
import s5p.properties.GlobalConfig;

import java.io.*;
import java.util.concurrent.*;

public class Main {

    static Graph graphB = new OriginGraph(GlobalConfig.inputGraphPath);
    static Graph graphS = new OriginGraph(GlobalConfig.inputGraphPath);


    Graph graph = graphB;

    public static void main(String[] args) throws Exception {
        PrintStream printStream = new PrintStream(new FileOutputStream(GlobalConfig.outputResultPath, true));
        PrintStream console = System.out;
        System.setOut(new PrintStream(new Main.TeeOutputStream(console, printStream)));
        printParaInfo();
        Runtime r = Runtime.getRuntime();
        r.gc();


        System.out.println("---------------start-------------");
        long beforeUsedMem = r.freeMemory();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        System.out.println("Start Time");
        long startTime = System.currentTimeMillis();
        long ClusterStartTime = System.currentTimeMillis();
        StreamCluster streamCluster = new StreamCluster(graphB, graphS);
        long InitialClusteringTime = System.currentTimeMillis();
        
        //Skewnes-aware Graph Clustering
        Future<Void> future_B = completionService.submit(() -> {
            streamCluster.startSteamClusterB();
            return null;
        });

        Future<Void> future_S = completionService.submit(() -> {
            streamCluster.startSteamClusterS();
            return null;
        });

        try {
            future_B.get();
            future_S.get();
        } catch (InterruptedException | ExecutionException e) {
           e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
        long ClusteringTime = System.currentTimeMillis();

        streamCluster.computeHybridInfo();
        System.out.println("End Clustering");

        
        long ClusterEndTime = System.currentTimeMillis();

        System.out.println("ComputeHybridInfo time: " + (ClusterEndTime - ClusteringTime) + " ms");
        System.out.println("Clustering Core time: " + (ClusteringTime - InitialClusteringTime) + " ms");
        System.out.println("Initial Clustering time: " + (InitialClusteringTime - ClusterStartTime) + " ms");
        System.out.println("Big clustersize:" + streamCluster.getClusterList_B().size());
        System.out.println("Small clustersize:" + streamCluster.getClusterList_S().size());


        Partitioner partitioner = new Partitioner(streamCluster);
        long gameStartTime = System.currentTimeMillis();

        //start Stackelberg Game
        partitioner.startStackelbergGame();
        long gameEndTime = System.currentTimeMillis();
        System.out.println("End Game");
        System.out.println("Cluster game time: " + (gameEndTime - gameStartTime) + " ms");
        long performStepStartTime = System.currentTimeMillis();

        //Postprocessing
        partitioner.performStep();
        long performStepEndTime= System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        System.out.println("End Time");

        //Main program ends, processing part of the program data as a log
        double rf = partitioner.getReplicateFactor();
        double lb = partitioner.getLoadBalance();
        graphB.clear();
        graphS.clear();
        System.gc();
        long afterUsedMem = r.freeMemory();
        long memoryUsed = (beforeUsedMem - afterUsedMem) >> 20;
        int roundCnt = partitioner.getGameRoundCnt();
        System.out.println("Partition num:" + GlobalConfig.getPartitionNum());
        System.out.println("Partition time: " + (endTime - startTime) + " ms");
        System.out.println("Relative balance load:" + lb);
        System.out.println("Replicate factor: " + rf);
        System.out.println("Memory cost: " + memoryUsed + " MB");
        System.out.println("Total game round:" + roundCnt);
        System.out.println("Cluster game time: " + (gameEndTime - gameStartTime) + " ms");
        System.out.println("Cluster Time: " + (ClusterEndTime - ClusterStartTime) + " ms");
        System.out.println("perform Step Time: " + (performStepEndTime - performStepStartTime) + " ms");
        System.out.println("---------------end-------------");
    }
    public static class TeeOutputStream extends OutputStream {
        private OutputStream out1;
        private OutputStream out2;

        public TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }
    }
    private static void printParaInfo() {
        System.out.println("input graph: " + GlobalConfig.inputGraphPath);
        System.out.println("outputGraphPath: " + GlobalConfig.outputGraphPath);
        System.out.println("vCount: " + GlobalConfig.vCount);
        System.out.println("eCount: " + GlobalConfig.eCount);
        System.out.println("averageDegree: " + GlobalConfig.getAverageDegree());
        System.out.println("partitionNum: " + GlobalConfig.partitionNum);
        System.out.println("alpha: " + GlobalConfig.alpha);
        System.out.println("beta: " + GlobalConfig.beta);
        System.out.println("k: " + GlobalConfig.k);
        System.out.println("batchSize: " + GlobalConfig.batchSize);
        System.out.println("partitionNum: " + GlobalConfig.partitionNum);
        System.out.println("threads: " + GlobalConfig.threads);
        System.out.println("MaxClusterVolume: " + GlobalConfig.getMaxClusterVolume());
    }
}
