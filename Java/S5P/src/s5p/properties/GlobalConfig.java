package s5p.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GlobalConfig {

    public static final byte hashNum;
    public static final int k;
    public static String inputGraphPath;
    public static String inputGraphPath_B;
    public static String inputGraphPath_S;
    public static String outputGraphPath;
    public static String outputResultPath;
    public static int vCount;
    public static int eCount;
    public static int eCount_B;
    public static int eCount_S;
    public static int partitionNum;
    public static final float alpha;
    public static final double beta;
    public static final double tao;
    public static int batchSize;
    public static int threads;

    static {
        InputStream inputStream = GlobalConfig.class.getResourceAsStream("project.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        hashNum = Byte.parseByte(properties.getProperty("hashNum", "1"));
        inputGraphPath = properties.getProperty("inputGraphPath");
        vCount = Integer.parseInt(properties.getProperty("vCount"));
        eCount = Integer.parseInt(properties.getProperty("eCount"));
        eCount_B = Integer.parseInt(properties.getProperty("eCount_B"));
        eCount_S = Integer.parseInt(properties.getProperty("eCount_S"));
        partitionNum = Integer.parseInt(properties.getProperty("partitionNum"));
        alpha = Float.parseFloat(properties.getProperty("alpha"));
        beta = Double.parseDouble(properties.getProperty("beta", "1.0"));
        k = Integer.parseInt(properties.getProperty("k"));
        batchSize = Integer.parseInt(properties.getProperty("batchSize"));
        threads = Integer.parseInt(properties.getProperty("threads"));
        outputGraphPath = properties.getProperty("outputGraphPath");
        outputResultPath = properties.getProperty("outputGraphPath", "newpartition/src/ouput/output.txt");
        inputGraphPath_S = properties.getProperty("inputGraphPath_S");
        inputGraphPath_B = properties.getProperty("inputGraphPath_B");
        tao = Double.parseDouble(properties.getProperty("tao", "1.0"));

    }

    public static byte getHashNum() {
        return hashNum;
    }

    public static String getInputGraphPath() {
        return inputGraphPath;
    }

    public static int getVCount() {
        return vCount;
    }

    public static int getECount() {
        return eCount;
    }

    public static int getPartitionNum() {
        return partitionNum;
    }

    public static double getMaxClusterVolume() {
        return  0.25 * (2 * eCount) / k;
    }

    public static float getAlpha() {
        return alpha;
    }

    public static double getAverageDegree() {
        return 2 * eCount / vCount;
    }
    public static int getK() {
        return k;
    }

    public static int getBatchSize() {
        return batchSize;
    }

    public static int getThreads() {
        return threads;
    }

    public static String getOutputGraphPath() {
        return outputGraphPath;
    }
    public static double getTao() {
        return tao;
    }
}
