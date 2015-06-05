package clustering;

import java.util.ArrayList;
import java.util.List;

import scala.Tuple2;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.SparkConf;

public class LDAExample {
	 public static void main(String[] args) {
		 SparkConf conf = new SparkConf().setAppName("LDA Example").setMaster("local");
		    JavaSparkContext sc = new JavaSparkContext(conf);

		    // Load and parse the data
		    String path = "data/sample_lda_data.txt";
		    JavaRDD<String> data = sc.textFile(path);
		    JavaRDD<Vector> parsedData = data.map(
		        new Function<String, Vector>() {
		          public Vector call(String s) {
		            String[] sarray = s.trim().split("\\W");
		            double[] values = new double[sarray.length];
		            for (int i = 0; i < sarray.length; i++)
		              values[i] = Double.parseDouble(sarray[i]);
		            return Vectors.dense(values);
		          }
		        }
		    );
		    // Index documents with unique IDs
		    JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
		        new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
		          public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
		            return doc_id.swap();
		          }
		        }
		    ));
		    corpus.cache();

		    // Cluster the documents into three topics using LDA
		    DistributedLDAModel ldaModel = new LDA().setK(3).run(corpus);

		    // Output topics. Each is a distribution over words (matching word count vectors)
/*		    System.out.println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize()
		        + " words):");
		    Matrix topics = ldaModel.topicsMatrix();
		    for (int topic = 0; topic < 3; topic++) {
		      System.out.print("Topic " + topic + ":");
		      for (int word = 0; word < ldaModel.vocabSize(); word++) {
		        System.out.print(" " + topics.apply(word, topic));
		      }
		      System.out.println();
		    }*/
		    
		    for(Tuple2<int[], double[] > tuple:ldaModel.describeTopics()){
		    	if(tuple._1.length!=tuple._2.length){
		    		System.out.println("WTF");
		    		break;
		    	}
		    	for(int i=0;i<tuple._1.length;i++){
		    		System.out.print(tuple._1[i]+":"+tuple._2[i]+",");
		    	}
				System.out.println();
		    }
		  
		    sc.stop();
		  }

}
