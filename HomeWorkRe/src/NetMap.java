import java.util.Iterator;
import java.util.TreeMap;

/**
 * Description:这个类用来表示集群存储顶点及边
 * Date:2019.08.24
 */
public class NetMap {
    private TreeMap<String,Vertex> vMap;
    private TreeMap<Integer, Edge> eMap;
    private int cosineSimilarity;

    public NetMap() {
        eMap   = new TreeMap<Integer, Edge>();
        vMap = new TreeMap<String, Vertex>();
    }

    public int getCosineSimilarity() {
        return cosineSimilarity;
    }

    public void setCosineSimilarity(int cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }

    public TreeMap<String, Vertex> getvMap() {
        return vMap;
    }

    public void setvMap(TreeMap<String, Vertex> vMap) {
        this.vMap = vMap;
    }

    public Iterator getVertexIterator(){
        return this.vMap.values().iterator();
    }



    public void addEdge(String v1, String v2){
        if(vMap.get(v1)==null){
            addVertex(v1);
        }
        if(vMap.get(v2)==null){
            addVertex(v2);
        }
        addUEdge(v1,v2);

    }

    public void addUEdge(String vertexA_s, String vertexB_s) {

        Vertex vertexA, vertexB;

        vertexA = vMap.get(vertexA_s);
        vertexB = vMap.get(vertexB_s);

        if ( !vertexA.isNeighbor(vertexB_s) || !vertexB.isNeighbor(vertexA_s) ){
            vertexA.addNeighbor(vertexB_s);
            vertexB.addNeighbor(vertexA_s);

            int id;
            if (eMap.size() > 0){
                id = (Integer) eMap.lastKey() + 1;
            } else {
                id = 0;
            }
            eMap.put(id, new Edge(id, vertexA_s, vertexB_s));
        }

    }
    public void calculateSimilarities() {
        //遍历图中每一条边计算余弦相似度
        Iterator itEdge = getEdgeIterator();
        Edge edge;
        while (itEdge.hasNext()) {
            edge = (Edge) itEdge.next();
            Vertex vertexA = vMap.get(edge.getVertexA());
            Vertex vertexB = vMap.get(edge.getVertexB());
            double sim = vertexA.calculateSimilarity(vertexB, 0);
            if (!(vertexA.getLabel().equals(vertexB.getLabel())) ){
                System.out.println(vertexA.getLabel() + "\t" + vertexB.getLabel() + "\t" + sim);
            }
            vertexA.setSimilarity(edge.getVertexB(), sim);
            vertexB.setSimilarity(edge.getVertexA(), sim);
            edge.setSimilarity(sim);
        }
    }
    public double getSumEdgeFactor(int direction, int optFunction){
        double totalFactor  = 0.0;
        Iterator itEdge = getEdgeIterator();
        while (itEdge.hasNext()) {
            Edge edge = (Edge) itEdge.next();
            Vertex vertexA, vertexB;
            vertexA = getVertex(edge.getVertexA() );
            vertexB = getVertex(edge.getVertexB() );
            totalFactor += vertexA.getEdgeFactor(vertexB.getLabel(), direction, optFunction);

        }
        return totalFactor;
    }

    public Iterator getEdgeIterator(){
        return this.eMap.values().iterator();
    }


    public Vertex getVertex(String vertexId){
        return this.vMap.get(vertexId);
    }

    public void addVertex(String vertex_s){
        vMap.put(vertex_s, new Vertex(vertex_s));
        addUEdge(vertex_s, vertex_s);
    }



}
