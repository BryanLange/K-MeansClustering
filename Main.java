import java.io.*;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		
		if(args.length == 2) {
			// display file names on console
			for(int i=0; i<args.length; i++) {
				System.out.println("args["+ i +"] : " + args[i]);
			}
			
			
			// open input and output files
			Scanner inFile = null;
			PrintWriter outFile = null;
			try {
				inFile = new Scanner(new File(args[0]));
				outFile = new PrintWriter(new File(args[1]));
			} catch(IOException e) {
				System.out.println("A file was not found, program terminated.");
				System.exit(0);
			}
			
			
			// read numRows, numCols, and numPts from inFile
			// prompt user for K value
			// dynamically allocate arrays
			int numRows = inFile.nextInt();
			int numCols = inFile.nextInt();
			int numPts = inFile.nextInt();
			Scanner scan = new Scanner(System.in);
			System.out.print("Please enter the value of K: ");
			int k = scan.nextInt();
			scan.close();	
			Kmean x = new Kmean(numRows, numCols, numPts, k);
					
			
			// read in points from inFile
			x.loadPointSet(inFile);
					
			
			// run K-Means Clustering algorithm
			x.kMeansClustering(outFile);
			
			
			// close input and output files
			inFile.close();
			outFile.close();
		}
		else {
			System.out.println("Invalid number of arguments.");
		}
	} // end main
	
	
	public static class Kmean {
		int K;
		int numPts;
		Point[] pointSet;
		int numRows;
		int numCols;
		int[][]	imgAry;
		Point[] Kcentroids;
		int change;
		
		
		// Point class to store coordinates, label, and distance
		public static class Point {
			double Xcoord;
			double Ycoord;
			int label;
			double distance;
					
			Point() {
				Xcoord = 0.0;
				Ycoord = 0.0;
				label = 0;
				distance = 99999.0;
			}
		} // end nested class: Point
		
		
		// constructor, dynamically allocate arrays
		public Kmean(int r, int c, int n, int pk) {
			numRows = r;
			numCols = c;
			numPts = n;
			K = pk;
			change = 0;
			
			imgAry = new int[numRows][numCols];
			for(int i=0; i<numRows; i++) {
				for(int j=0; j<numCols; j++) {
					imgAry[i][j] = 0;
				}
			}
			
			pointSet = new Point[numPts];
			for(int i=0; i<numPts; i++) {
				pointSet[i] = new Point();
			}
			
			Kcentroids = new Point[K+1];
			for(int i=0; i<K+1; i++) {
				Kcentroids[i] = new Point();
			}		
		} // end constructor
		
		
		// load points from inFile into pointSet[]
		public void loadPointSet(Scanner inFile) {
			int index = 0;
			while(inFile.hasNext()) {
				int x = inFile.nextInt();
				int y = inFile.nextInt();
				
				pointSet[index].Xcoord = (double) x;
				pointSet[index].Ycoord = (double) y;
				pointSet[index].label = 0;
				pointSet[index].distance = 99999.0;
				
				index++;
			}
		} // end loadPointSet()
		
		
		// K-Means Clustering algorithm
		public void kMeansClustering(PrintWriter outFile) {
			int iteration = 0;
			assignLabel();
			
			do {
				point2Image();
				
				prettyPrint(outFile, iteration);
				
				change = 0;
				
				computeCentroids();
				
				for(int i=0; i<numPts; i++) {
					distanceMinLabel(pointSet[i]);
				}
				
				iteration++;
			} while(change > 2);
		} // end kMeansClustering()
		
		
		// assign a "random" label to all points in the point set
		public void assignLabel() {
			int front = 0;
			int back = numPts - 1;
			int label = 1;
			while(front <= back) {
				pointSet[front].label = label;
				front++;
				label++;
				if(label > K) label = 1;
				
				pointSet[back].label = label;
				back--;
				label++;
				if(label > K) label = 1;
			}
		} // end assignLabel()
		
		
		// compute centroid of each group
		public void computeCentroids() {
			double[] sumX = new double[K+1];
			double[] sumY = new double[K+1];
			double[] totalPt = new double[K+1];
			for(int i=0; i<=K; i++) {
				sumX[i] = 0.0;
				sumY[i] = 0.0;
				totalPt[i] = 0.0;
			}
			
			for(int i=0; i<numPts; i++) {
				int label = pointSet[i].label;
				sumX[label] += pointSet[i].Xcoord;
				sumY[label] += pointSet[i].Ycoord;
				totalPt[label]++;
			}
			
			for(int label=1; label<=K; label++) {
				if(totalPt[label] > 0.0) {
					Kcentroids[label].Xcoord = (sumX[label] / totalPt[label]);
					Kcentroids[label].Ycoord = (sumY[label] / totalPt[label]);
				}
			}
		} // end computeCentroids()
		
		
		// finds the minimum distance to a centroid of the given point
		public void distanceMinLabel(Point pt) {
			double minDist = 99999.0;
			int minLabel = 0;
			
			for(int label=1; label<=K; label++) {
				double dist = computeDist(pt, Kcentroids[label]);
				if(dist < minDist) {
					minLabel = label;
					minDist = dist;
				}
			}
			
			pt.distance = minDist;
			
			if(pt.label != minLabel) {
				pt.label = minLabel;
				change++;
			}
		} // end distanceMinLabel()
		
		
		// returns the distance between two points
		public double computeDist(Point pt, Point cent) {
			double ac = Math.abs(pt.Ycoord - cent.Ycoord);
		    double cb = Math.abs(pt.Xcoord - cent.Xcoord);     
		    return Math.hypot(ac, cb);
		} // end computeDist()
		
		
		// plot all points into the imgAry[][]
		public void point2Image() {
			for(int i=0; i<numPts; i++) {
				int xCoord = (int) pointSet[i].Xcoord;
				int yCoord = (int) pointSet[i].Ycoord;
				
				imgAry[xCoord][yCoord] = pointSet[i].label;
			}
		} // end point2Image()
		
		
		// print imgAry[][] to given outFile at the given interation
		// ignores zeros
		public void prettyPrint(PrintWriter outFile, int iteration) {
			outFile.println("*** Result of iteration " + iteration + " ***");
			for(int i=0; i<numRows; i++) {
				for(int j=0; j<numCols; j++) {
					if(imgAry[i][j] > 0) outFile.format("%2d", imgAry[i][j]);
					else outFile.print(" ");
				}
				outFile.println();
			}
		} // end prettyPrint()
			
	} // end class: Kmean
	
} // end wrapper class: Main
