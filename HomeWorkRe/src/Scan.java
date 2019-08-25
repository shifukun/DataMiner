import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * 作者：石傅琨
 * Email: fkshi@stu.xidian.edu.cn
 * Github:
 * Blog:blog.fukunshi.tk:8089
 */
public class Scan {
    public static void main(String[] args) {
        //输入文件路径
        System.out.println("请输入文件路径");
        Scanner in = new Scanner(System.in);
        String path = in.nextLine();
        openFileScan(path);
    }
    /**
     * Description:将文件进行读取对边进行拆分并将相应结点与边的信息存储到NetMap类中
     * Date:2019.08.24
     */
    private static void openFileScan(String path) {
        String line = null;
        //从路径读取数据文件
        NetMap netMap = new NetMap();
        System.out.println(path);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            //读入的行不为空
            String[] data = new String[2];

            while ((line = in.readLine())!=null){
                data = line.split(" |\t");
                if(data.length==2){
                    String v1 = data[0];
                    String v2 = data[1];
                    netMap.addEdge(v1,v2);
                }
            }
            System.out.println("文件读取成功");
            in.close();
            netMap.setCosineSimilarity(0);
            netMap.calculateSimilarities();
        }catch(Exception e) {
            e.printStackTrace();
            System.err.println("文件读取错误");
        }
        double modularity = 0.0;
        long start = System.currentTimeMillis();
        for (double eps_iterator=0.1; eps_iterator<=1.0; eps_iterator+=0.1){
            BigDecimal bd = new BigDecimal(eps_iterator);
            bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
            double eps = bd.doubleValue();
            //取不同的eps参数运行Scan算法
            runScanAlgorithm(eps, 2 ,netMap);
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            Evaluate evaluate = new Evaluate(netMap);
            System.out.println("\n\neps= " + eps + " mu= " + 2);
            System.out.println("共耗费时间 " + elapsedTimeSec + "秒" );
            modularity= evaluate.calculateUndirectedModularity(0);
            clustResultsCommaDelimited(path+"Result", eps, 2, modularity,netMap);

        }

    }

    /**
     * Description:对挖掘结果进行输出输出文件名为输入path+Result
     * Date:2019.08.24
     */
    public static void clustResultsCommaDelimited(String filename, double eps, int mu, double modularity,NetMap netMap){
        int nonmembers = 0;
        String outFile = filename + "__SCAN_clusters.txt";
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outFile, true));
            out.write("\n\n---------- SCAN result ----------");
            out.write("\n" + filename + ", epsilon=" + eps + ", mu=" + mu);
            TreeMap <Integer, TreeSet> clusterMembers = new TreeMap<Integer, TreeSet>();
            Iterator itVertex = netMap.getVertexIterator();
            while (itVertex.hasNext()) {
                Vertex vertex = (Vertex) itVertex.next();
                if ( ! clusterMembers.containsKey(vertex.getClusterId() ) ) {
                    clusterMembers.put (vertex.getClusterId(), new TreeSet<String>());
                }
                clusterMembers.get( vertex.getClusterId() ).add( vertex.getLabel() );
                vertex.setClusterId(-1);//未遍历结点
            }
            Iterator itCluster = clusterMembers.keySet().iterator();
            while (itCluster.hasNext()) {
                int cluster = (Integer) itCluster.next();
                TreeSet<String> members  = (TreeSet) clusterMembers.get( cluster );
                if ( members.size() == 0 ) { continue; }
                else {
                    if (cluster == -4)  { out.write("\nOUTLIERS:");  }
                    else if (cluster == -3) { out.write("\nHUBS:");  }
                    else 				     { out.write("\nCluster[" + cluster + "]:"); }
                }
                Iterator<String> itMembers = members.iterator();
                while ( itMembers.hasNext() ) {
                    String member = itMembers.next();
                    out.write(member + ",");
                    if (cluster == -4 || cluster == -3) {nonmembers++;}
                }
            }
            out.write("\nModularity: " + modularity);
            out.close();
        }
        catch (IOException e) { System.out.println("结果输出错误"); }

        System.out.println("outliers: " + nonmembers);
    }

    /**
     * Description:对经过计算余弦相似度的集合运行Scan算法
    * Date:2019.08.24
     */
    public static void runScanAlgorithm(double eps, int mu,NetMap netMap) { // Scan algorithm
        int clusterID = 0;
        LinkedList queue = new LinkedList();
        Vertex vertex, yVertex, xVertex;
        HashSet epsNeighborhood = new HashSet<String>();
        HashSet epsNeighborhoodY = new HashSet<String>();
        Iterator itVertex = netMap.getVertexIterator();
        while (itVertex.hasNext()) {
            vertex = (Vertex) itVertex.next();
            //对没有遍历的结点进行遍历
            if (vertex.getClusterId() == -1){
                epsNeighborhood =  getEpsNeighborhood(vertex, eps);
                //结点为集群的core
                if (epsNeighborhood.size() >= mu){
                    clusterID++;
                    //遍历过的结点ID设置为自增值
                    vertex.setClusterId(clusterID);
                    Iterator itEpsNeighbor =  epsNeighborhood.iterator();
                    //如果他还有相邻结点未被遍历，对其进行DFS（深度优先）搜索，将其邻进结点加入队列中
                    while (itEpsNeighbor.hasNext() ){
                        String epsNeighbor = (String) itEpsNeighbor.next();
                        queue.add(epsNeighbor);
                    }
                    //依次遍历队列中的结点
                    while (queue.size() > 0){
                        yVertex = (Vertex) netMap.getVertex( (String) queue.removeFirst() );
                        yVertex.setClusterId(clusterID);
                        epsNeighborhoodY =  getEpsNeighborhood(yVertex, eps);
                        Iterator itEpsNeighborY =  epsNeighborhoodY.iterator();
                        if (epsNeighborhoodY.size() >= mu){
                            while (itEpsNeighborY.hasNext() ){
                                String epsNeighborY = (String) itEpsNeighborY.next();
                                xVertex = (Vertex) netMap.getVertex( epsNeighborY );
                                if (xVertex.getClusterId() == -1){
                                    queue.add( epsNeighborY );
                                }
                                if (xVertex.getClusterId() == -1 || xVertex.getClusterId() == -2){
                                    xVertex.setClusterId(clusterID);
                                }
                            }

                        }
                    }

                }
                else{
                    vertex.setClusterId(-2);
                }

            }
        }
        // 找出集群的Hub和Outliers
        itVertex = netMap.getVertexIterator();
        while (itVertex.hasNext()) {
            vertex = (Vertex) itVertex.next();
            if (vertex.getClusterId() == -2){
                Set neighbors = vertex.getNeighborhood();
                Iterator itNeighbors = neighbors.iterator();
                Set neighbors_clusters = new HashSet();
                while (itNeighbors.hasNext() ){
                    String neighbor_s = (String) itNeighbors.next();
                    Vertex neighbor = netMap.getVertex(neighbor_s);
                    neighbors_clusters.add(neighbor.getClusterId() );
                }
                //邻居属于超过两个不同的集群
                if (neighbors.size() > 10 && neighbors_clusters.size() > 2) {
                    vertex.setClusterId(-4); //设置HUB
                }
                else{
                    vertex.setClusterId(-3);//设置局外单独结点(Outliers)
                }
            }
        }


    }
    public static HashSet getEpsNeighborhood(Vertex vertex, double eps) {
        HashSet epsNeighborhood = new HashSet<String>();
        Iterator itNeighbors = vertex.getNeighborhood().iterator();
        while (itNeighbors.hasNext()){
            String neighbor   = (String) itNeighbors.next();
            double similarity = vertex.getSimilarity(neighbor); // get similarity of vertex to its neighbor
            if (similarity >= eps){
                epsNeighborhood.add(neighbor);
            }
        }
        return epsNeighborhood;

    }

}
