/** author nxyuruk@ualr.edu
 *
 *  September 6, 2007
 *
 *
 */


import java.math.BigDecimal;
import java.util.Iterator;

public class Evaluate{

	private NetMap net;

	public Evaluate(NetMap network){
			this.net = network;
	}
	public double calculateUndirectedModularity(int optFunction)
	{
		double m = net.getSumEdgeFactor(0, optFunction);
		double Q=0.0;
		double Aij=0.0;
		double local_minus=0.0;

		int vertexClust_fr;
		int vertexClust_to;

		Vertex vertex_fr;
		Vertex vertex_to;
		Iterator itVertex_fr = net.getVertexIterator();
		while (itVertex_fr.hasNext())
		{
			vertex_fr = (Vertex) itVertex_fr.next();

			Iterator itVertex_to = net.getVertexIterator();
			while (itVertex_to.hasNext())
			{
				vertex_to = (Vertex) itVertex_to.next();

				vertexClust_fr = vertex_fr.getClusterId();
				vertexClust_to = vertex_to.getClusterId();
				if ( vertexClust_fr <= 0 ||  vertexClust_to <= 0 || vertexClust_fr != vertexClust_to ) continue;

				if ( vertex_fr.isNeighbor(vertex_to.getLabel()) )	{
					Aij = vertex_fr.getEdgeFactor(vertex_to.getLabel(), 0, optFunction);
				}
				else { Aij = 0.0; }
				local_minus = vertex_fr.getDegreeFactor(0, optFunction)*vertex_to.getDegreeFactor(0, optFunction);
				local_minus= (double) (local_minus / (2*m));
				Q= Q + Aij - local_minus;
			}
		}
		Q = Q / (2*m);
		BigDecimal bd = new BigDecimal(Q);
		bd = bd.setScale(4,BigDecimal.ROUND_HALF_UP);
    	Q = bd.doubleValue();
		return Q;
	}
}

