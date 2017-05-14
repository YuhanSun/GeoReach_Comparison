package org.datasyslab.Experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.datasyslab.Experiment.Experiment;
import org.datasyslab.GeoReach.Config.Distribution;
import org.datasyslab.GeoReach.Entity;
import org.datasyslab.GeoReach.GeoReach;
import org.datasyslab.GeoReach.MyRectangle;
import org.datasyslab.GeoReach.Neo4j_Graph_Store;
import org.datasyslab.GeoReach.OwnMethods;
import org.datasyslab.GeoReach.Traversal;
import org.neo4j.register.Register.Int;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Experiment {

	public static ArrayList<String> ReadExperimentNode(String filepath) {
		ArrayList<String> al;
		block13 : {
			al = new ArrayList<String>();
			BufferedReader reader = null;
			File file = null;
			try {
				try {
					file = new File(filepath);
					reader = new BufferedReader(new FileReader(file));
					String temp = null;
					while ((temp = reader.readLine()) != null) {
						al.add(temp);
					}
					reader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
					if (reader == null) break block13;
					try {
						reader.close();
					}
					catch (IOException var6_6) {}
				}
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException var6_8) {}
				}
			}
		}
		return al;
	}

	public static ArrayList<MyRectangle> ReadExperimentQueryRectangle(String filepath) {
		ArrayList<MyRectangle> queryrectangles;
		block13 : {
			queryrectangles = new ArrayList<MyRectangle>();
			BufferedReader reader = null;
			File file = null;
			try {
				try {
					file = new File(filepath);
					reader = new BufferedReader(new FileReader(file));
					String temp = null;
					while ((temp = reader.readLine()) != null) {
						String[] line_list = temp.split("\t");
						MyRectangle rect = new MyRectangle(Double.parseDouble(line_list[0]), Double.parseDouble(line_list[1]), Double.parseDouble(line_list[2]), Double.parseDouble(line_list[3]));
						queryrectangles.add(rect);
					}
					reader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
					if (reader == null) break block13;
					try {
						reader.close();
					}
					catch (IOException var8_8) {}
				}
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (IOException var8_10) {}
				}
			}
		}
		return queryrectangles;
	}

	public static void Experiment_MG_Implementation() {
		try {
			int ratio = 40;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MG/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MG/accesscount_" + ratio + ".csv";
			String datasource = "citeseerx";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_time, true, "selectivity\t32_time\t128_time\t512_time\t2048_time\n");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\t32_count\t128_count\t512_count\t2048_count\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			while (selectivity < 0.9) {
				int MG;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/yelp_%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				ArrayList<Integer> MGs = new ArrayList<Integer>();
				if (datasource.equals("go_uniprot")) {
					MG = 8;
					while (MG <= 64) {
						MGs.add(MG);
						MG *= 2;
					}
				} else if (datasource.equals("Yelp")) {
					MGs.add(16384);
				} else {
					MG = 32;
					while (MG <= 2048) {
						MGs.add(MG);
						MG *= 4;
					}
				}
				MG = 128;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MG, ratio);
				OwnMethods.Print((Object)OwnMethods.ClearCache(password));
				OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
				Thread.currentThread();
				Thread.sleep(5000);
				GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
				int visitednode_count = 0;
				int time = 0;
				int experiment_count = 50;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					long start = System.currentTimeMillis();
					boolean result = geo.ReachabilityQuery((long)id, queryrect);
					time = (int)((long)time + (System.currentTimeMillis() - start));
					visitednode_count += geo.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					++i;
				}
				OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_MG_Implementation_ColdNeo4j() {
		try {
			int ratio = 40;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MG/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MG/accesscount_" + ratio + ".csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/query_GeoReach.log";
			String datasource = "citeseerx";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, ratio));
			OwnMethods.WriteFile(result_path_time, true, "selectivity\t32_time\t128_time\t512_time\t2048_time\n");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\t32_count\t128_count\t512_count\t2048_count\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 0.001;
			int experiment_count = 50;
			while (selectivity < 0.01) {
				int MG;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				ArrayList<Integer> MGs = new ArrayList<Integer>();
				if (datasource.equals("go_uniprot")) {
					MG = 8;
					while (MG <= 64) {
						MGs.add(MG);
						MG *= 2;
					}
				} else if (datasource.equals("Yelp")) {
					MGs.add(16384);
				} else {
					MG = 32;
					while (MG <= 2048) {
						MGs.add(MG);
						MG *= 4;
					}
				}
				MG = 128;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MG, ratio);
				int visitednode_count = 0;
				int time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					OwnMethods.ClearCache(password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
					long one_time = 0;
					long start = System.currentTimeMillis();
					boolean result = geo.ReachabilityQuery((long)id, queryrect);
					one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					visitednode_count += geo.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t", one_time, geo.neo4j_time, geo.visited_count)) + result + "\n"));
					Neo4j_Graph_Store.StopServer(db_path);
					++i;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * for dag dataset change spatial selectivity
	 */
	public static void Experiment_Selectivity_ColdNeo4j_DAG() {
		try {
			String datasource = "citeseerx";
			int ratio = 40;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
			int p_split_pieces = 128;

			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" 
					+ datasource + "_GeoReach_querytime" + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" 
					+ datasource + "_GeoReach_accesscount" + ".csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/query_GeoReach.log";

			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\t" + ratio + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\t" + ratio + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, ratio));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			int experiment_count = 50;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int MG = 128;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MG, ratio);
				int visitednode_count = 0;
				int time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					OwnMethods.ClearCache(password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
					long one_time = 0;
					long start = System.currentTimeMillis();
					boolean result = geo.ReachabilityQuery((long)id, queryrect);
					one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					visitednode_count += geo.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t", one_time, geo.neo4j_time, geo.visited_count)) + result + "\n"));
					Neo4j_Graph_Store.StopServer(db_path);
					++i;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Yelp_Implementation_ColdNeo4j(int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange(entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_querytime.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_accesscount.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/query.log";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, target_folder));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList<Integer> refer_table = OwnMethods.ReadSCC(SCC_filepath, original_graph_path);
			double selectivity = 1.0E-6;
			int experiment_count = 20;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int MG = 16384;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MG, target_folder);
				int visitednode_count = 0;
				int time = 0;
				int total_neo4j_time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					id = (Integer)refer_table.get(id);
					MyRectangle queryrect = queryrectangles.get(i);
					OwnMethods.ClearCache(password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
					long start = System.currentTimeMillis();
					boolean result = geo.ReachabilityQuery((long)id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time += geo.neo4j_time;
					visitednode_count += geo.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", geo.visited_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t", one_time, geo.neo4j_time, geo.visited_count)) + result + "\n"));
					Neo4j_Graph_Store.StopServer(db_path);
					++i;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_ColdNeo4jOnce(int target_folder, int i, double selectivity) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange(entity_path);
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList<Integer> refer_table = OwnMethods.ReadSCC(SCC_filepath, original_graph_path);
			int log = (int)Math.log10(selectivity);
			String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
			ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
			int true_count = 0;
			boolean true_count_record = true;
			int MG = 16384;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MG, target_folder);
			int visitednode_count = 0;
			int time = 0;
			int total_neo4j_time = 0;
			OwnMethods.Print((Object)i);
			int id = Integer.parseInt(nodeids.get(i));
			id = (Integer)refer_table.get(id);
			MyRectangle queryrect = queryrectangles.get(i);
			OwnMethods.ClearCache(password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
			long start = System.currentTimeMillis();
			boolean result = geo.ReachabilityQuery((long)id, queryrect);
			long one_time = System.currentTimeMillis() - start;
			time = (int)((long)time + one_time);
			total_neo4j_time += geo.neo4j_time;
			visitednode_count += geo.visited_count;
			if (result && true_count_record) {
				++true_count;
			}
			OwnMethods.Print((Object)String.format("Time: %d", one_time));
			OwnMethods.Print((Object)String.format("Visited count: %d", geo.visited_count));
			OwnMethods.Print((Object)result);
			OwnMethods.Print((Object)(String.valueOf(String.format("%d\t%d\t%d\t", one_time, geo.neo4j_time, geo.visited_count)) + result + "\n"));
			Neo4j_Graph_Store.StopServer(db_path);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_MR_Implementation() {
		try {
			int ratio = 40;
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MR/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MR/accesscount_" + ratio + ".csv";
			String datasource = "citeseerx";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			ArrayList<Integer> MRs = new ArrayList<Integer>();
			OwnMethods.WriteFile(result_path_time, true, "selectivity\t");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\t");
			Iterator<Integer> iterator = MRs.iterator();
			while (iterator.hasNext()) {
				int MR = (Integer)iterator.next();
				OwnMethods.WriteFile(result_path_time, true, String.format("%d_time", MR));
				OwnMethods.WriteFile(result_path_count, true, String.format("%d_count", MR));
			}
			OwnMethods.WriteFile(result_path_time, true, "true_count\n");
			OwnMethods.WriteFile(result_path_count, true, "true_count\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			boolean isbreak = false;
			while (selectivity < 0.9) {
				if (isbreak) break;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				Iterator iterator2 = MRs.iterator();
				while (iterator2.hasNext()) {
					int MR = (Integer)iterator2.next();
					boolean record_true_count = false;
					if (MR == (Integer)MRs.get(0)) {
						record_true_count = true;
					}
					String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MR, ratio);
					OwnMethods.Print((Object)OwnMethods.ClearCache(password));
					OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
					Thread.currentThread();
					Thread.sleep(2000);
					GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
					int visitednode_count = 0;
					int time = 0;
					int experiment_count = 50;
					int i = 0;
					while (i < experiment_count) {
						OwnMethods.Print((Object)i);
						int id = Integer.parseInt(nodeids.get(i));
						MyRectangle queryrect = queryrectangles.get(i);
						long start = System.currentTimeMillis();
						Boolean result = geo.ReachabilityQuery((long)id, queryrect);
						time = (int)((long)time + (System.currentTimeMillis() - start));
						visitednode_count += geo.visited_count;
						if (result.booleanValue() && record_true_count) {
							++true_count;
						}
						OwnMethods.Print((Object)result);
						++i;
					}
					OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t"));
					OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_MR_Implementation_ColdNeo4j() {
		try {
			int ratio = 40;
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MR/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MR/accesscount_" + ratio + ".csv";
			String datasource = "citeseerx";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			ArrayList<Integer> MRs = new ArrayList<Integer>();
			OwnMethods.WriteFile(result_path_time, true, "selectivity\t");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\t");
			Iterator iterator = MRs.iterator();
			while (iterator.hasNext()) {
				int MR = (Integer)iterator.next();
				OwnMethods.WriteFile(result_path_time, true, String.format("%d_time\t", MR));
				OwnMethods.WriteFile(result_path_count, true, String.format("%d_count\t", MR));
			}
			OwnMethods.WriteFile(result_path_time, true, "true_count\n");
			OwnMethods.WriteFile(result_path_count, true, "true_count\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 0.001;
			boolean isbreak = false;
			while (selectivity < 0.02) {
				if (isbreak) break;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				Iterator iterator2 = MRs.iterator();
				while (iterator2.hasNext()) {
					int MR = (Integer)iterator2.next();
					boolean record_true_count = false;
					if (MR == (Integer)MRs.get(0)) {
						record_true_count = true;
					}
					String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/neo4j-community-2.3.3_GeoReach_%d_%d", datasource, MR, ratio);
					int visitednode_count = 0;
					int time = 0;
					int experiment_count = 50;
					int i = 0;
					while (i < experiment_count) {
						OwnMethods.ClearCache(password);
						Neo4j_Graph_Store.StartServer(db_path);
						Thread.currentThread();
						Thread.sleep(2000);
						GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
						OwnMethods.Print((Object)i);
						int id = Integer.parseInt(nodeids.get(i));
						MyRectangle queryrect = queryrectangles.get(i);
						long start = System.currentTimeMillis();
						Boolean result = geo.ReachabilityQuery((long)id, queryrect);
						time = (int)((long)time + (System.currentTimeMillis() - start));
						visitednode_count += geo.visited_count;
						if (result.booleanValue() && record_true_count) {
							++true_count;
						}
						OwnMethods.Print((Object)result);
						Neo4j_Graph_Store.StopServer(db_path);
						++i;
					}
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t"));
					OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Distribution_Implementation() {
		try {
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			int ratio = 20;
			String result_path_time = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/random/querytime_%d.csv", ratio);
			String result_path_count = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/random/accesscount_%d.csv", ratio);
			String datasource = "Patents";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_time, true, "selectivity\ttime\n");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\tcount\n");
			int MG = datasource.equals("go_uniprot") ? 8 : (datasource.equals("Patents") ? 32 : 128);
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_GeoReach_%d", datasource, MG);
				Thread.currentThread();
				Thread.sleep(5000);
				GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
				int visitednode_count = 0;
				int time = 0;
				int i = 0;
				while (i < nodeids.size()) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					long start = System.currentTimeMillis();
					boolean result = geo.ReachabilityQuery((long)id, queryrect);
					time = (int)((long)time + (System.currentTimeMillis() - start));
					visitednode_count += geo.visited_count;
					if (result) {
						++true_count;
					}
					++i;
				}
				OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / nodeids.size()) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count) + "\t"));
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void IndexSize() {
		for (String distribution : distribution_a) {
			String resultpath = String.format("/home/yuhansun/Documents/GeoReach_Comparison_Experiment_Result/%s_index_size.csv", distribution);
			for (String datasource : dataset_a) {
				OwnMethods.WriteFile(resultpath, true, (String.valueOf(datasource) + "\n"));
				int MG = datasource.equals("Patents") ? 32 : (datasource.equals("go_uniprot") ? 8 : 128);
				int ratio = 20;
				while (ratio <= 80) {
					String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/GeoReachIndex/GeoReach_%s_%d_%d.txt", datasource, distribution, ratio, MG);
					long size = OwnMethods.GeoReachIndexSize(GeoReach_filepath);
					OwnMethods.WriteFile(resultpath, true, (String.valueOf(ratio) + "\t" + size + "\n"));
					ratio += 20;
				}
				OwnMethods.WriteFile(resultpath, true, "\n");
			}
		}
	}

	public static void Experiment_MC_Implementation() {
		try {
			int ratio = 40;
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/accesscount_" + ratio + ".csv";
			String datasource = "Patents";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_time, true, "selectivity\tMC0_time\tMC2_time\tMC3_time\n");
			OwnMethods.WriteFile(result_path_count, true, "selectivity\tMC0_count\tMC2_count\tMC3_count\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				ArrayList al_true_count = new ArrayList();
				ArrayList<Integer> MCs = new ArrayList<Integer>();
				MCs.add(0);
				MCs.add(2);
				MCs.add(3);
				Iterator iterator = MCs.iterator();
				while (iterator.hasNext()) {
					int MC = (Integer)iterator.next();
					String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MC_Experiment/neo4j-community-2.3.3_GeoReach_%d", datasource, MC);
					OwnMethods.Print((Object)OwnMethods.ClearCache(password));
					OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
					Thread.currentThread();
					Thread.sleep(2000);
					GeoReach geo = new GeoReach(p_total_range, p_split_pieces, MC);
					int visitednode_count = 0;
					int time = 0;
					int sum_neo4j = 0;
					int i = 0;
					while (i < nodeids.size()) {
						OwnMethods.Print((Object)i);
						int id = Integer.parseInt(nodeids.get(i));
						MyRectangle queryrect = queryrectangles.get(i);
						long start = System.currentTimeMillis();
						boolean result = geo.ReachabilityQuery((long)id, queryrect);
						time = (int)((long)time + (System.currentTimeMillis() - start));
						sum_neo4j += geo.neo4j_time;
						visitednode_count += geo.visited_count;
						if (result && true_count_record) {
							++true_count;
						}
						++i;
					}
					OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / nodeids.size()) + "\t" + sum_neo4j / nodeids.size() + "\t"));
					OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count) + "\t"));
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\t"));
					true_count_record = false;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_MC_ImplementationColdNeo4j() {
		try {
			int ratio = 40;
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/accesscount_" + ratio + ".csv";
			ArrayList<Integer> MCs = new ArrayList<Integer>();
			MCs.add(1);
			MCs.add(2);
			MCs.add(3);
			MCs.add(4);
			String datasource = "citeseerx";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\n"));
			OwnMethods.WriteFile(result_path_time, true, "selectivity");
			OwnMethods.WriteFile(result_path_count, true, "selectivity");
			Iterator iterator = MCs.iterator();
			while (iterator.hasNext()) {
				int MC = (Integer)iterator.next();
				OwnMethods.WriteFile(result_path_time, true, String.format("\tMC_%d\tneo4j_time\ttruecount", MC));
				OwnMethods.WriteFile(result_path_count, true, String.format("\tMC_%d", MC));
			}
			OwnMethods.WriteFile(result_path_time, true, String.format("\n", new Object[0]));
			OwnMethods.WriteFile(result_path_count, true, String.format("\n", new Object[0]));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			while (selectivity < 0.02) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				ArrayList al_true_count = new ArrayList();
				Iterator iterator2 = MCs.iterator();
				while (iterator2.hasNext()) {
					int MC = (Integer)iterator2.next();
					String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MC_Experiment/neo4j-community-2.3.3_GeoReach_%d", datasource, MC);
					int visitednode_count = 0;
					int time = 0;
					int sum_neo4j = 0;
					int experiment_count = 50;
					int i = 0;
					while (i < experiment_count) {
						OwnMethods.Print((Object)OwnMethods.ClearCache(password));
						OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
						Thread.currentThread();
						Thread.sleep(2000);
						GeoReach geo = new GeoReach(p_total_range, p_split_pieces, MC);
						OwnMethods.Print((Object)i);
						int id = Integer.parseInt(nodeids.get(i));
						MyRectangle queryrect = queryrectangles.get(i);
						long start = System.currentTimeMillis();
						boolean result = geo.ReachabilityQuery((long)id, queryrect);
						time = (int)((long)time + (System.currentTimeMillis() - start));
						sum_neo4j += geo.neo4j_time;
						visitednode_count += geo.visited_count;
						if (result && true_count_record) {
							++true_count;
						}
						OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
						++i;
					}
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t" + sum_neo4j / experiment_count + "\t"));
					OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
					OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\t"));
					true_count_record = false;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Hot_False(String datasource, int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange(entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_querytime_false.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_accesscount_false.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_GeoReach_false.log";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\t" + target_folder + " Hot\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, target_folder));

			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_GeoReach_%d", datasource, target_folder);
			OwnMethods.ClearCache(password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/false/experiment_id_%s_%d.txt", datasource, log);
				ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/false/query_rect_%s_%d.txt", datasource, log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int visitednode_count = 0;
				int time = 0;
				int total_neo4j_time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					GeoReach geoReach = new GeoReach(p_total_range, split_pieces, 0);
					long start = System.currentTimeMillis();
					boolean result = geoReach.ReachabilityQuery(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + geoReach.neo4j_time);
					visitednode_count += geoReach.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", geoReach.visited_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, geoReach.neo4j_time, geoReach.visited_count)) + result + "\n"));
					++i;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				OwnMethods.WriteFile(log_path, true, "\n");
				selectivity *= 10.0;
			}
			Neo4j_Graph_Store.StopServer(db_path);
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Hot_True(String datasource, int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange(entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_querytime_true.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_accesscount_true.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_GeoReach_true.log";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\t" + target_folder + " Hot\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, target_folder));

			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_GeoReach_%d", datasource, target_folder);
			OwnMethods.ClearCache(password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/experiment_id_%s_%d.txt", datasource, log);
				ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/query_rect_%s_%d.txt", datasource, log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int visitednode_count = 0;
				int time = 0;
				int total_neo4j_time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					GeoReach geoReach = new GeoReach(p_total_range, split_pieces, 0);
					long start = System.currentTimeMillis();
					boolean result = geoReach.ReachabilityQuery(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + geoReach.neo4j_time);
					visitednode_count += geoReach.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", geoReach.visited_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, geoReach.neo4j_time, geoReach.visited_count)) + result + "\n"));
					++i;
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				OwnMethods.WriteFile(log_path, true, "\n");
				selectivity *= 10.0;
			}
			Neo4j_Graph_Store.StopServer(db_path);
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Cold_True(String datasource, int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange(entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_querytime_true.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_GeoReach_accesscount_true.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_GeoReach_true.log";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\t" + target_folder + " Cold\n"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\t" + target_folder + " Cold\n"));
			OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t%d\n", datasource, target_folder));

			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_GeoReach_%d", datasource, target_folder);

			while (selectivity < 0.9) {
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile(log_path, true, (String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/experiment_id_%s_%d.txt", datasource, log);
				ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/query_rect_%s_%d.txt", datasource, log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int visitednode_count = 0;
				int time = 0;
				int total_neo4j_time = 0;
				int i = 0;
				while (i < experiment_count) {

					OwnMethods.ClearCache(password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);

					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					GeoReach geoReach = new GeoReach(p_total_range, split_pieces, 0);
					long start = System.currentTimeMillis();
					boolean result = geoReach.ReachabilityQuery(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + geoReach.neo4j_time);
					visitednode_count += geoReach.visited_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", geoReach.visited_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile(log_path, true, (String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, geoReach.neo4j_time, geoReach.visited_count)) + result + "\n"));
					++i;

					Neo4j_Graph_Store.StopServer(db_path);
				}
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile(result_path_count, true, "\n");
				OwnMethods.WriteFile(log_path, true, "\n");
				selectivity *= 10.0;
			}

			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			OwnMethods.WriteFile(log_path, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void TrueCountTest() {
		try {
			int ratio = 80;
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/querytime_" + ratio + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/MC/accesscount_" + ratio + ".csv";
			String datasource = "Patents";
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(datasource) + "\nselectivity"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(datasource) + "\nselectivity"));
			ArrayList<Integer> MCs = new ArrayList<Integer>();
			MCs.add(2);
			MCs.add(3);
			Iterator iterator = MCs.iterator();
			while (iterator.hasNext()) {
				int MC = (Integer)iterator.next();
				OwnMethods.WriteFile(result_path_time, true, String.format("\tMC%d_time", MC));
				OwnMethods.WriteFile(result_path_count, true, String.format("\tMC%d_count", MC));
			}
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(selectivity) + "\t"));
			OwnMethods.WriteFile(result_path_count, true, (String.valueOf(selectivity) + "\t"));
			int log = (int)Math.log10(selectivity);
			String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
			ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
			int true_count = 0;
			boolean true_count_record = true;
			ArrayList al_true_count = new ArrayList();
			Iterator iterator2 = MCs.iterator();
			while (iterator2.hasNext()) {
				int MC = (Integer)iterator2.next();
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MC_Experiment/neo4j-community-2.3.3_GeoReach_%d", datasource, MC);
				OwnMethods.Print((Object)OwnMethods.ClearCache(password));
				OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
				Thread.currentThread();
				Thread.sleep(5000);
				GeoReach geo = new GeoReach(p_total_range, p_split_pieces, MC);
				int visitednode_count = 0;
				int time = 0;
				int neo4j_time = 0;
				int i = 0;
				OwnMethods.Print((Object)i);
				int id = Integer.parseInt(nodeids.get(i));
				MyRectangle queryrect = queryrectangles.get(i);
				long start = System.currentTimeMillis();
				boolean result = geo.ReachabilityQuery((long)id, queryrect);
				time = (int)((long)time + (System.currentTimeMillis() - start));
				visitednode_count += geo.visited_count;
				neo4j_time += geo.neo4j_time;
				if (result && true_count_record) {
					++true_count;
				}
				OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(time) + "\t" + neo4j_time + "\t"));
				OwnMethods.WriteFile(result_path_count, true, (String.valueOf(visitednode_count) + "\t"));
				OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\t"));
				true_count_record = false;
			}
			OwnMethods.WriteFile(result_path_time, true, (String.valueOf(true_count) + "\n"));
			OwnMethods.WriteFile(result_path_count, true, "\n");
			selectivity *= 10.0;
			OwnMethods.WriteFile(result_path_time, true, "\n");
			OwnMethods.WriteFile(result_path_count, true, "\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void MC0Test() {
		String db_path = "/home/yuhansun/Documents/Real_data/Patents/neo4j-community-2.3.3_GeoReach_32";
		OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
		int id = 2393369;
		MyRectangle queryrect = new MyRectangle(927.477972, 244.053534, 937.477972, 254.053534);
		MyRectangle total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
		GeoReach pGeoReach = new GeoReach(total_range, 128, 0);
		pGeoReach.ReachabilityQuery((long)id, queryrect);
		Iterator iterator = pGeoReach.Visited.iterator();
		while (iterator.hasNext()) {
			long element = (Long)iterator.next();
			OwnMethods.WriteFile("/home/yuhansun/Documents/share/MC0_test.txt", true, String.format("%d\n", element));
		}
		OwnMethods.WriteFile("/home/yuhansun/Documents/share/MC0_test.txt", true, "\n");
	}

	public static void MC3Test() {
		String db_path = "/home/yuhansun/Documents/Real_data/Patents/MC_Experiment/neo4j-community-2.3.3_GeoReach_3";
		OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
		int id = 2393369;
		MyRectangle queryrect = new MyRectangle(927.477972, 244.053534, 937.477972, 254.053534);
		MyRectangle total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
		GeoReach pGeoReach = new GeoReach(total_range, 128, 3);
		pGeoReach.ReachabilityQuery((long)id, queryrect);
		Iterator iterator = pGeoReach.Visited.iterator();
		while (iterator.hasNext()) {
			long element = (Long)iterator.next();
			OwnMethods.WriteFile("/home/yuhansun/Documents/share/MC3_test.txt", true, String.format("%d\n", element));
		}
		OwnMethods.WriteFile("/home/yuhansun/Documents/share/MC3_test.txt", true, "\n");
	}

	/**
	 * for dag dataset change spatial ratio
	 */
	public static void Experiment_Ratio_ColdNeo4j_DAG() {
		try {
			for (String dataset : dataset_a)
			{
				String graph_path = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/new_graph.txt", dataset);
				int nodeCount = OwnMethods.GetNodeCountGeneral(graph_path);

				MyRectangle p_total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
				int p_split_pieces = 128;
				double selectivity = 0.001;
				int experiment_count = 50;

				String result_path_time = "/mnt/hgfs/Experiment_Result/GeoReach_Experiment/result/ratio/" 
						+ dataset + "_GeoReach_querytime" + ".csv";
				String result_path_count = "/mnt/hgfs/Experiment_Result/GeoReach_Experiment/result/ratio/" 
						+ dataset + "_GeoReach_accesscount" + ".csv";
				String log_path = "/mnt/hgfs/Experiment_Result/GeoReach_Experiment/result/ratio/query_GeoReach.log";

				OwnMethods.WriteFile(result_path_time, true, dataset + "\tColdNeo4j\t"+String.valueOf(selectivity)+"\n");
				OwnMethods.WriteFile(result_path_count, true, dataset + "\tColdNeo4j\t"+String.valueOf(selectivity)+"\n");
				OwnMethods.WriteFile(log_path, true, String.format("GeoReach\t%s\t"+String.valueOf(selectivity)+"\n", dataset));

				for ( int ratio = 20; ratio < 90; ratio +=20)
				{
					int spaNodeCount = (int) (nodeCount * (100-ratio) / 100.0); 

					String querynodeid_filepath = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment"
							+ "/experiment_query/%s/experiment_id.txt", dataset);
					ArrayList<String> nodeids = Experiment.ReadExperimentNode(querynodeid_filepath);
					OwnMethods.WriteFile(result_path_time, true, ratio + "\t");
					OwnMethods.WriteFile(result_path_count, true, ratio + "\t");
					OwnMethods.WriteFile(log_path, true, ratio + "\n");
					String queryrectangle_filepath = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment"
							+ "/experiment_query/%s/%s_%d_queryrect_%d.txt", dataset, distribution, ratio, (int) (spaNodeCount * selectivity));
					OwnMethods.Print(queryrectangle_filepath);
					ArrayList<MyRectangle> queryrectangles = Experiment.ReadExperimentQueryRectangle(queryrectangle_filepath);
					int true_count = 0;
					boolean true_count_record = true;
					int MG = 128, MR = 200, MC = 0;
					String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/ratio/"
							+ "neo4j-community-2.3.3_GeoReach_Random_spatial_distributed_%d_%d_%d_%d", dataset, ratio, MG, MR, MC);
					int visitednode_count = 0;
					int time = 0;
					int i = 0;
					while (i < experiment_count) 
					{
						OwnMethods.Print(i);
						int id = Integer.parseInt(nodeids.get(i));
						MyRectangle queryrect = queryrectangles.get(i);
						OwnMethods.ClearCache(password);
						Neo4j_Graph_Store.StartServer(db_path);
						Thread.currentThread();
						Thread.sleep(2000);
						GeoReach geo = new GeoReach(p_total_range, p_split_pieces, 0);
						long one_time = 0;
						long start = System.currentTimeMillis();
						boolean result = geo.ReachabilityQuery((long)id, queryrect);
						one_time = System.currentTimeMillis() - start;
						time = (int)((long)time + one_time);
						visitednode_count += geo.visited_count;
						if (result && true_count_record) 
							++true_count;
						OwnMethods.WriteFile(log_path, true, String.format(
								"%d\t%d\t%d\t", one_time, geo.neo4j_time, geo.visited_count) + result + "\n");
						Neo4j_Graph_Store.StopServer(db_path);
						++i;
					}
					OwnMethods.WriteFile(result_path_time, true, String.valueOf(time / experiment_count) + "\t"+true_count+"\t\n");
					OwnMethods.WriteFile(result_path_count, true, String.valueOf(visitednode_count / experiment_count) + "\t\n");
					true_count_record = false;
					OwnMethods.WriteFile(log_path, true, "\n");
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static int split_pieces = 128;
	public static String password = "syh19910205";
	static ArrayList<String> dataset_a = new ArrayList<>(Arrays.asList("uniprotenc_150m", "Patents", "go_uniprot", "citeseerx"));
	public static ArrayList<String> distribution_a = new ArrayList<String>();
	//	public static String datasource = "Gowalla";
	public static String datasource = "foursquare";
	public static String distribution = Distribution.Random_spatial_distributed.name();
	public static int target_folder = 1;

	public static void main(String[] args) {
		try {

			//			Experiment.Experiment_MG_Implementation();
			//			GenerateFalseQuery(datasource, target_folder, distribution);
			//			GenerateTrueQuery(datasource, target_folder, distribution);
			//			Experiment_Hot_False(datasource, target_folder);
			//			Experiment_Hot_True(datasource, target_folder);

			//			Traversal.Experiment_Cold_True(datasource, target_folder);
			//			Experiment_Cold_True(datasource, target_folder);

			Experiment_Ratio_ColdNeo4j_DAG();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}