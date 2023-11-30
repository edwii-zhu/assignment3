import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	
	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}

		private boolean allSameLabel(ArrayList<Datum> data) {
			int firstLabel = data.get(0).y;
			for (Datum d : data) {
				if (d.y != firstLabel) {
					return false;
				}
			}
			return true;
		}
		DTNode fillDTNode(ArrayList<Datum> datalist) {
			if (datalist.size() >= minSizeDatalist) {
				if (allSameLabel(datalist)) {
					DTNode leaf = new DTNode();
					leaf.label = datalist.get(0).y;
					leaf.left = null;
					leaf.right = null;
					return leaf;
				} else {
					double[] bestSplit = findBestSplit(datalist);
					DTNode node = new DTNode();
					node.leaf = false;
					node.attribute = (int) bestSplit[0];
					node.threshold = bestSplit[1];
					ArrayList<Datum> left = new ArrayList<>();
					ArrayList<Datum> right = new ArrayList<>();
                    for (Datum datum : datalist) {
                        if (datum.x[node.attribute] < node.threshold) {
                            left.add(datum);
                        } else {
                            right.add(datum);
                        }
                    }
					node.left = fillDTNode(left);
					node.right = fillDTNode(right);
					return node;
				}
			}
			else
			{
				DTNode leaf = new DTNode();
				leaf.label = findMajority(datalist);
				leaf.left = null;
				leaf.right = null;
				return leaf;
			}
		}
		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {
			
			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			
			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}

		private double[] findBestSplit(ArrayList<Datum> datalist) {
			// attribute is index and threshold is value
			double bestAvgEntropy = Double.MAX_VALUE;
			double bestAttribute = -1;
			double bestThreshold = -1;
			for (int i = 0 ; i < datalist.get(0).x.length ; i++){
				for(int j = 0 ; j < datalist.size() ; j++){
					ArrayList<Datum> left = new ArrayList<>();
					ArrayList<Datum> right = new ArrayList<>();

                    for (int k = 0 ; k < datalist.size() ; k++) {
                        if (k <= j) {
                            left.add(datalist.get(k));
                        } else {
                            right.add(datalist.get(k));
                        }
                    }
					double avgEntropy = (left.size() * calcEntropy(left) + right.size() * calcEntropy(right)) / datalist.size();
					double split = datalist.get(j).x[i];
					if (bestAvgEntropy > avgEntropy){
						bestAvgEntropy = avgEntropy;
						bestAttribute = i;
						bestThreshold = split;
					}
				}
			}
            return new double[]{bestAttribute, bestThreshold};
        }


		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			
			//ADD CODE HERE

			return -1; //dummy code.  Update while completing the assignment.
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		private void preorder(DTNode node, ArrayList<Integer> list)
		{
			if (node == null)
				return;
			else
			{
				list.add(node.attribute);
				preorder(node.left, list);
				preorder(node.right, list);
			}
		}
		public boolean equals(Object dt2)
		{
			if (!(dt2 instanceof DTNode))
				return false;
			ArrayList<Integer> list1 = new ArrayList<>();
			ArrayList<Integer> list2 = new ArrayList<>();
			preorder(this, list1);
			preorder((DTNode) dt2, list2);
			if (!list1.equals(list2))
				return false;
			else if (this.attribute != ((DTNode)dt2).attribute || this.threshold != ((DTNode)dt2).threshold || this.label != ((DTNode)dt2).label)
				return false;
			else
				return this.leaf == ((DTNode) dt2).leaf;
        }
	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
