import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * 这个类用来定义结点
 */
public class Vertex {
    private String label;
    private int clusterID;

    private TreeMap<String, Double[]> neighborhood;
    public String getLabel(){
        return label;
    }
    public void setSimilarity(String toVertex, double similarity){

        Double ws[] = neighborhood.get(toVertex);
        ws[1] = similarity;
    }

    public int getClusterId() {
        return clusterID;
    }

    public Vertex( String label ) {
        this.label = label;
        this.clusterID    = -1;
        this.neighborhood = new TreeMap<String, Double[]>();
    }
    public boolean isNeighbor(String neighbor){
        return neighborhood.containsKey(neighbor);
    }

    public void addNeighbor(String neighbor) {

        if ( ! neighborhood.containsKey(neighbor)) {
            Double ws[] = new Double[2];
            ws[0] = 1.0;
            ws[1] = 0.0;										// similarity=0 tentatively
            neighborhood.put (neighbor, ws);
        }

    }
    public Set getNeighborhood() {
            return neighborhood.keySet();
    }

    /**
     * Description:计算余弦相似度，由于是无向、无权图，故权重均设置为1
     * Date:2019.08.24
     */
    public double calculateSimilarity(Vertex toVertex, int similarityFunc){
        HashSet<String> neighborhood1 = new HashSet<String>( this.getNeighborhood() );
        HashSet<String> neighborhood2 = new HashSet<String>( toVertex.getNeighborhood() );
        double dot_product = 0.0;
        double vec_len1 = 0.0;
        double vec_len2 = 0.0;
        double sim = 0.0;
        Iterator itNeighbors = neighborhood1.iterator();
        while (itNeighbors.hasNext()) {
            String neighbor = (String) itNeighbors.next();
            double weight1 = this.getWeight(neighbor);
            vec_len1 += (weight1 * weight1);
            if ( toVertex.isNeighbor(neighbor) ){
                double weight2 = toVertex.getWeight(neighbor);
                dot_product += (weight1 * weight2);
            }

        }
        Iterator itNeighbors2 = neighborhood2.iterator();
        while (itNeighbors2.hasNext()) {
            String neighbor2 = (String) itNeighbors2.next();
            double weight2 = toVertex.getWeight(neighbor2);
            vec_len2 += (weight2 * weight2);
        }


        vec_len1 = Math.sqrt(vec_len1);
        vec_len2 = Math.sqrt(vec_len2);
        if (vec_len1 == 0.0 ||  vec_len2 == 0.0) { return 0.0; }
        sim =  (double) (dot_product / ( vec_len1 * vec_len2 ));
        BigDecimal bd = new BigDecimal(sim);
        bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
        sim = bd.doubleValue();

        return sim;

    }


    public double getWeight(String toVertex){
        Double ws[] = new Double[2];
        ws = (Double[]) neighborhood.get(toVertex);
        return ws[0];
    }

    public void setClusterId(int clusterId){
        this.clusterID = clusterId;
    }

    public double getDegreeFactor(int direction, int function){

        double degreeFactor = 0.0;
        HashSet<String> neighborhood = new HashSet<String>( this.getNeighborhood() );
        Iterator itNeighbors = neighborhood.iterator();
        while (itNeighbors.hasNext()) {
            String neighbor = (String) itNeighbors.next();
            degreeFactor += this.getEdgeFactor(neighbor, direction, function);

        }
        return degreeFactor;
    }

    public double getEdgeFactor(String toVertex, int direction, int function){
        double Aij = 0.0;
        if (toVertex.equals(this.getLabel()) ){ Aij = 0.0; return Aij;}
        switch (function)
        {
            case 0:
                Aij = 1.0;  break;
            case 1:
                Aij = this.getSimilarity(toVertex); break;
            default:
                Aij = 1.0;  break;
        }
        return Aij;

    }
    public double getSimilarity(String toVertex){
        Double ws[] = neighborhood.get(toVertex);
        return ws[1];
    }

}
