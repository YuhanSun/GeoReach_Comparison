package org.datasyslab.GeoReach;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.datasyslab.GeoReach.Config;
import org.datasyslab.GeoReach.GeoReach;
import org.datasyslab.GeoReach.MyRectangle;
import org.datasyslab.GeoReach.Neo4j_Graph_Store;
import org.datasyslab.GeoReach.OwnMethods;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Traversal {
	public Set<Integer> VisitedVertices = new HashSet<Integer>();
	static Neo4j_Graph_Store p_neo4j_graph_store = new Neo4j_Graph_Store();
	private String longitude_property_name;
	private String latitude_property_name;
	public long Neo4jTime;
	public long JudgeTime;
	public int access_count;
	public static String password = "syh19910205";

	public Traversal() {
		Config p_Config = new Config();
		this.longitude_property_name = p_Config.GetLongitudePropertyName();
		this.latitude_property_name = p_Config.GetLatitudePropertyName();
		this.Neo4jTime = 0;
		this.JudgeTime = 0;
	}

	public void Preprocess() {
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

	public boolean ReachabilityQuery(int start_id, MyRectangle rect) {
		LinkedList<Integer> queue = new LinkedList<Integer>();
		this.VisitedVertices.clear();
		this.VisitedVertices.add(start_id);
		this.access_count = 1;
		String query = String.format("match (a) where id(a) = %d return a", start_id);
		long start = System.currentTimeMillis();
		String result = p_neo4j_graph_store.Execute(query);
		this.Neo4jTime += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		JsonArray start_node = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
		JsonObject start_node_ob = start_node.get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject();
		if (start_node_ob.has(this.longitude_property_name)) {
			double lon = start_node_ob.get(this.longitude_property_name).getAsDouble();
			double lat = start_node_ob.get(this.latitude_property_name).getAsDouble();
			if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
				this.JudgeTime += System.currentTimeMillis() - start;
				return true;
			}
		}
		this.JudgeTime += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		query = "match (a)-->(b) where id(a) = " + Integer.toString(start_id) + " return id(b), b";
		result = p_neo4j_graph_store.Execute(query);
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(result);
		JsonArray jsonArr = (JsonArray)jsonObject.get("results");
		jsonObject = (JsonObject)jsonArr.get(0);
		jsonArr = (JsonArray)jsonObject.get("data");
		this.Neo4jTime += System.currentTimeMillis() - start;
		this.access_count += jsonArr.size();
		start = System.currentTimeMillis();
		int i = 0;
		while (i < jsonArr.size()) {
			double lat;
			double lon;
			jsonObject = (JsonObject)jsonArr.get(i);
			JsonArray row = (JsonArray)jsonObject.get("row");
			int id = row.get(0).getAsInt();
			jsonObject = (JsonObject)row.get(1);
			if (jsonObject.has(this.longitude_property_name) && Neo4j_Graph_Store.Location_In_Rect(lat = Double.parseDouble(jsonObject.get(this.latitude_property_name).toString()), lon = Double.parseDouble(jsonObject.get(this.longitude_property_name).toString()), rect)) {
				this.JudgeTime += System.currentTimeMillis() - start;
				System.out.println(id);
				return true;
			}
			if (!this.VisitedVertices.contains(id)) {
				this.VisitedVertices.add(id);
				queue.add(id);
			}
			++i;
		}
		this.JudgeTime += System.currentTimeMillis() - start;
		while (!queue.isEmpty()) {
			start = System.currentTimeMillis();
			int id = (Integer)queue.poll();
			query = "match (a)-->(b) where id(a) = " + Integer.toString(id) + " return id(b), b";
			result = p_neo4j_graph_store.Execute(query);
			jsonParser = new JsonParser();
			jsonObject = (JsonObject)jsonParser.parse(result);
			jsonArr = (JsonArray)jsonObject.get("results");
			jsonObject = (JsonObject)jsonArr.get(0);
			jsonArr = (JsonArray)jsonObject.get("data");
			this.Neo4jTime += System.currentTimeMillis() - start;
			this.access_count += jsonArr.size();
			start = System.currentTimeMillis();
			int i2 = 0;
			while (i2 < jsonArr.size()) {
				double lon;
				double lat;
				jsonObject = (JsonObject)jsonArr.get(i2);
				JsonArray row = (JsonArray)jsonObject.get("row");
				int neighbor_id = row.get(0).getAsInt();
				jsonObject = (JsonObject)row.get(1);
				if (jsonObject.has(this.longitude_property_name) && Neo4j_Graph_Store.Location_In_Rect(lat = Double.parseDouble(jsonObject.get(this.latitude_property_name).toString()), lon = Double.parseDouble(jsonObject.get(this.longitude_property_name).toString()), rect)) {
					this.JudgeTime += System.currentTimeMillis() - start;
					System.out.println(neighbor_id);
					return true;
				}
				if (!this.VisitedVertices.contains(neighbor_id)) {
					this.VisitedVertices.add(neighbor_id);
					queue.add(neighbor_id);
				}
				++i2;
			}
			this.JudgeTime += System.currentTimeMillis() - start;
		}
		return false;
	}

	public boolean ReachabilityQueryDFS_Recursive(int start_id, MyRectangle rect) {
		long start = System.currentTimeMillis();
		String query = "match (a)-->(b) where id(a) = " + Integer.toString(start_id) + " return id(b), b";
		String result = p_neo4j_graph_store.Execute(query);
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(result);
		JsonArray jsonArr = (JsonArray)jsonObject.get("results");
		jsonObject = (JsonObject)jsonArr.get(0);
		jsonArr = (JsonArray)jsonObject.get("data");
		this.Neo4jTime += System.currentTimeMillis() - start;
		this.access_count += jsonArr.size();
		int i = 0;
		while (i < jsonArr.size()) {
			double lat;
			double lon;
			jsonObject = (JsonObject)jsonArr.get(i);
			JsonArray row = (JsonArray)jsonObject.get("row");
			int id = row.get(0).getAsInt();
			jsonObject = (JsonObject)row.get(1);
			if (jsonObject.has(this.longitude_property_name) && Neo4j_Graph_Store.Location_In_Rect(lat = Double.parseDouble(jsonObject.get(this.latitude_property_name).toString()), lon = Double.parseDouble(jsonObject.get(this.longitude_property_name).toString()), rect)) {
				System.out.println(id);
				return true;
			}
			if (!this.VisitedVertices.contains(id)) {
				this.VisitedVertices.add(id);
				boolean reachable = this.ReachabilityQueryDFS_Recursive(id, rect);
				if (reachable) {
					return true;
				}
			}
			++i;
		}
		return false;
	}

	public boolean ReachabilityQueryDFS(int start_id, MyRectangle rect) {
		this.VisitedVertices.clear();
		this.VisitedVertices.add(start_id);
		this.access_count = 1;
		String query = String.format("match (a) where id(a) = %d return a", start_id);
		long start = System.currentTimeMillis();
		String result = p_neo4j_graph_store.Execute(query);
		this.Neo4jTime += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		JsonArray start_node = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
		JsonObject start_node_ob = start_node.get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject();
		if (start_node_ob.has(this.longitude_property_name)) {
			double lon = start_node_ob.get(this.longitude_property_name).getAsDouble();
			double lat = start_node_ob.get(this.latitude_property_name).getAsDouble();
			if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
				this.JudgeTime += System.currentTimeMillis() - start;
				return true;
			}
		}
		this.JudgeTime += System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		query = "match (a)-->(b) where id(a) = " + Integer.toString(start_id) + " return id(b), b";
		result = p_neo4j_graph_store.Execute(query);
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(result);
		JsonArray jsonArr = (JsonArray)jsonObject.get("results");
		jsonObject = (JsonObject)jsonArr.get(0);
		jsonArr = (JsonArray)jsonObject.get("data");
		this.Neo4jTime += System.currentTimeMillis() - start;
		this.access_count += jsonArr.size();
		int i = 0;
		while (i < jsonArr.size()) {
			double lon;
			double lat;
			jsonObject = (JsonObject)jsonArr.get(i);
			JsonArray row = (JsonArray)jsonObject.get("row");
			int id = row.get(0).getAsInt();
			jsonObject = (JsonObject)row.get(1);
			if (jsonObject.has(this.longitude_property_name) && Neo4j_Graph_Store.Location_In_Rect(lat = Double.parseDouble(jsonObject.get(this.latitude_property_name).toString()), lon = Double.parseDouble(jsonObject.get(this.longitude_property_name).toString()), rect)) {
				System.out.println(id);
				return true;
			}
			if (!this.VisitedVertices.contains(id)) {
				this.VisitedVertices.add(id);
				boolean reachable = this.ReachabilityQueryDFS_Recursive(id, rect);
				if (reachable) {
					return true;
				}
			}
			++i;
		}
		return false;
	}

	public static void Experiment_Distribution_Implementation() {
		try {
			double total_range_size = 1000.0;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, total_range_size, total_range_size);
			int p_split_pieces = 128;
			int ratio = 80;
			String datasource = "Patents";
			int MG = datasource.equals("go_uniprot") ? 8 : (datasource.equals("Patents") ? 32 : 128);
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			boolean isbreak = false;
			while (selectivity < 0.9) {
				if (isbreak) break;
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_GeoReach_%d", datasource, MG);
				OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
				OwnMethods.Print((Object)OwnMethods.ClearCache((String)password));
				OwnMethods.Print((Object)Neo4j_Graph_Store.StartServer(db_path));
				Thread.currentThread();
				Thread.sleep(5000);
				Traversal traversal = new Traversal();
				GeoReach geoReach = new GeoReach(p_total_range, p_split_pieces, 0);
				int visitednode_count = 0;
				int time = 0;
				int i = 0;
				while (i < nodeids.size()) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQuery(id, queryrect);
					boolean result2 = geoReach.ReachabilityQuery(id, queryrect);
					time = (int)((long)time + (System.currentTimeMillis() - start));
					visitednode_count += traversal.access_count;
					if (result) {
						++true_count;
						isbreak = true;
						break;
					}
					OwnMethods.Print((Object)result);
					OwnMethods.Print((Object)result2);
					if (result != result2) {
						OwnMethods.Print((Object)String.format("id:%d\n%f, %f, %f, %f", id, queryrect.min_x, queryrect.min_y, queryrect.max_x, queryrect.max_y));
						OwnMethods.WriteFile((String)"/home/yuhansun/Documents/test.txt", (boolean)true, (String)String.format("id:%d\n%f, %f, %f, %f", id, queryrect.min_x, queryrect.min_y, queryrect.max_x, queryrect.max_y));
						isbreak = true;
						break;
					}
					++i;
				}
				OwnMethods.Print((Object)Neo4j_Graph_Store.StopServer(db_path));
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile((String)"/home/yuhansun/Documents/test.txt", (boolean)true, (String)"\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Exception decompiling
	 */
	public void Neo4j_Graph_Loader(String entity_filepath, String graph_filepath, String db_path)
	{
		BatchInserter inserter = null;
		BufferedReader reader_entity = null;BufferedReader reader_graph = null;
		File file_entity = null;File file_graph = null;
		Map<String, String> config = new HashMap();
		config.put("dbms.pagecache.memory", "10g");

		try
		{
			file_entity = new File(entity_filepath);
			inserter = org.neo4j.unsafe.batchinsert.BatchInserters.inserter(new File(db_path).getAbsolutePath(), config);

			reader_entity = new BufferedReader(new FileReader(file_entity));
			String tempString_entity = null;
			tempString_entity = reader_entity.readLine();
			int graph_count = Integer.parseInt(tempString_entity);

			Label graph_label = DynamicLabel.label("GRAPH");
			RelationshipType graph_rel = org.neo4j.graphdb.DynamicRelationshipType.withName("LINK");
			while ((tempString_entity = reader_entity.readLine()) != null)
			{
				Map<String, Object> properties = new HashMap();
				String[] l_entity = tempString_entity.split(",");

				int id = Integer.parseInt(l_entity[0]);
				int isspatial = Integer.parseInt(l_entity[1]);
				if (isspatial == 1)
				{
					double lon = Double.parseDouble(l_entity[2]);
					double lat = Double.parseDouble(l_entity[3]);
					properties.put(longitude_property_name, Double.valueOf(lon));
					properties.put(latitude_property_name, Double.valueOf(lat));
				}

				inserter.createNode(id, properties, new Label[] { graph_label });
			}
			reader_entity.close();


			file_graph = new File(graph_filepath);
			reader_graph = new BufferedReader(new FileReader(file_graph));
			String tempString_graph = null;

			tempString_graph = reader_graph.readLine();
			if (graph_count != Integer.parseInt(tempString_graph))
			{
				System.out.println("Entity file and Graph file have different number of vertices!"); return; }


			while((tempString_graph = reader_graph.readLine()) != null)
			{
				String[] l_graph = tempString_graph.split(",");
				int start_id = Integer.parseInt(l_graph[0]);
				int count = Integer.parseInt(l_graph[1]);
				for(int i = 0;i<count;i++)
				{
					int end_id = Integer.parseInt(l_graph[i+2]);
					inserter.createRelationship(start_id, end_id, graph_rel, null);
				}
			}
			reader_graph.close();


			reader_graph.close();
			inserter.shutdown();
		}
		catch (Exception e)
		{
			e.printStackTrace();



			if (inserter != null)
				inserter.shutdown();
			if (reader_entity != null)
			{
				try
				{
					reader_entity.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
			if (reader_graph != null)
			{
				try
				{
					reader_graph.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
		finally
		{
			if (inserter != null)
				inserter.shutdown();
			if (reader_entity != null)
			{
				try
				{
					reader_entity.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (reader_graph != null)
			{
				try
				{
					reader_graph.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	public static void LoadData_Distribution_DAG() {
		String distribution = "Random_spatial_distributed";
		int ratio = 40;
		String datasource = "citeseerx";
		String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
		String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
		String db_folder_name = String.format("neo4j-community-2.3.3_Traversal_%d", ratio);
		String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/data/graph.db", datasource, db_folder_name);
		Traversal traversal = new Traversal();
		OwnMethods.Print((Object)String.format("Load data from \n%s\n%s\ninto\n%s", entity_filepath, graph_filepath, db_filepath));
		traversal.Neo4j_Graph_Loader(entity_filepath, graph_filepath, db_filepath);
	}

	public static void LoadData() {
		String distribution = "Random_spatial_distributed";
		int target_folder = 1;
		String datasource = "citeseerx";
		String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
		String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
		String db_folder_name = String.format("neo4j-community-2.3.3_Traversal_%d", target_folder);
		String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/data/graph.db", datasource, db_folder_name);
		Traversal traversal = new Traversal();
		OwnMethods.Print((Object)String.format("Load data from \n%s\n%s\ninto\n%s", entity_filepath, graph_filepath, db_filepath));
		traversal.Neo4j_Graph_Loader(entity_filepath, graph_filepath, db_filepath);
	}

	public static void Experiment_Selectivity_ColdNeo4j_DAG() {
		try {
			String datasource = "citeseerx";
			int ratio = 40;
			MyRectangle p_total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime" + ".csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount" + ".csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + ratio + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + ratio + "\tColdNeo4j\t\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%d\n", datasource, ratio));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/experiment_id.txt", datasource);
			ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
			double selectivity = 1.0E-4;
			int experiment_count = 20;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%d.txt", log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				int MG = 128;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, ratio);
				int visitednode_count = 0;
				int time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					OwnMethods.ClearCache((String)password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					Traversal traversal = new Traversal();
					long one_time = 0;
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQuery(id, queryrect);
					one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t", one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					Neo4j_Graph_Store.StopServer(db_path);
					++i;
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Yelp_Implementation(String datasource, int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " Hot\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%d\n", datasource, target_folder));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList refer_table = OwnMethods.ReadSCC((String)SCC_filepath, (String)original_graph_path);
			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, target_folder);
			OwnMethods.ClearCache((String)password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
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
					id = (Integer)refer_table.get(id);
					MyRectangle queryrect = queryrectangles.get(i);
					Traversal traversal = new Traversal();
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQueryDFS(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t", one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					++i;
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			Neo4j_Graph_Store.StopServer(db_path);
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Experiment_Yelp_Implementation_ColdNeo4j(String datasource, int target_folder) {
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_newformat.txt", datasource, distribution, target_folder);
			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " ColdNeo4j\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " ColdNeo4j\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%dColdNeo4j\n", datasource, target_folder));
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList refer_table = OwnMethods.ReadSCC((String)SCC_filepath, (String)original_graph_path);
			double selectivity = 1.0E-6;
			int experiment_count = 100;
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
				int log = (int)Math.log10(selectivity);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
				ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
				int true_count = 0;
				boolean true_count_record = true;
				String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, target_folder);
				int visitednode_count = 0;
				int time = 0;
				int total_neo4j_time = 0;
				int i = 0;
				while (i < experiment_count) {
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					id = (Integer)refer_table.get(id);
					MyRectangle queryrect = queryrectangles.get(i);
					OwnMethods.ClearCache((String)password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					Traversal traversal = new Traversal();
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQueryDFS(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t", one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					Neo4j_Graph_Store.StopServer(db_path);
					++i;
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
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
			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList refer_table = OwnMethods.ReadSCC((String)SCC_filepath, (String)original_graph_path);
			int log = (int)Math.log10(selectivity);
			String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
			ArrayList<MyRectangle> queryrectangles = Traversal.ReadExperimentQueryRectangle(queryrectangle_filepath);
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
			OwnMethods.ClearCache((String)password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			Traversal traversal = new Traversal();
			long start = System.currentTimeMillis();
			boolean result = traversal.ReachabilityQuery(id, queryrect);
			long one_time = System.currentTimeMillis() - start;
			time = (int)((long)time + one_time);
			total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
			visitednode_count += traversal.access_count;
			if (result && true_count_record) {
				++true_count;
			}
			OwnMethods.Print((Object)String.format("Time: %d", one_time));
			OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
			OwnMethods.Print((Object)result);
			OwnMethods.Print((Object)(String.valueOf(String.format("%d\t%d\t%d\t", one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
			Neo4j_Graph_Store.StopServer(db_path);
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
//			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime_true.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount_true.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal_true.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " Hot\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%d\n", datasource, target_folder));
			
			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, target_folder);
			OwnMethods.ClearCache((String)password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
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
					Traversal traversal = new Traversal();
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQueryDFS(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					++i;
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			Neo4j_Graph_Store.StopServer(db_path);
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
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
//			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime_true.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount_true.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal_true.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " Cold\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " Cold\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%d\n", datasource, target_folder));
			
			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, target_folder);
			
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
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
					
					OwnMethods.ClearCache((String)password);
					Neo4j_Graph_Store.StartServer(db_path);
					Thread.currentThread();
					Thread.sleep(2000);
					
					OwnMethods.Print((Object)i);
					int id = Integer.parseInt(nodeids.get(i));
					MyRectangle queryrect = queryrectangles.get(i);
					Traversal traversal = new Traversal();
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQueryDFS(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					++i;
					Neo4j_Graph_Store.StopServer(db_path);
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Experiment_Hot_True_Test(String datasource, int target_folder) {
		
		FileWriter id_writer = null;
		FileWriter rectangle_writer = null;
		try {
			int p_split_pieces = 128;
			String distribution = "Random_spatial_distributed";
			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
			ArrayList<Entity> entities = OwnMethods.ReadEntity(entity_path);
			String graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
			ArrayList<ArrayList<Integer>> graph = OwnMethods.ReadGraph(graph_path);
			
			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList refer_table = OwnMethods.ReadSCC((String)SCC_filepath, (String)original_graph_path);
			
			
			double selectivity = 1.0E-6;
			int experiment_count = 500;
			while (selectivity < 0.9) 
			{
				
				int log = (int)Math.log10(selectivity);
//				String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/experiment_id_%s_%d.txt", datasource, log);
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/experiment_id.txt", datasource, target_folder);
				
				ArrayList<String> nodeids = Traversal.ReadExperimentNode(querynodeid_filepath);
//				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/query_rect_%s_%d.txt", datasource, log);
				String queryrectangle_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
				
				String id_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/experiment_id_%s_%d.txt", datasource,log);
				String rectangle_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/query_rect_%s_%d.txt", datasource, log);
				id_writer = new FileWriter(new File(id_path));
				rectangle_writer = new FileWriter(new File(rectangle_path));
				
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
					
					id = (Integer) refer_table.get(id);
					
					MyRectangle queryrect = queryrectangles.get(i);
					
					boolean result = OwnMethods.ReachabilityQuery(graph, entities, id, queryrect);
					
					OwnMethods.Print((Object)result);
					if(result)
						true_count++;
					
					if(result)
					{
						id_writer.write(String.valueOf(id) + "\n");
						rectangle_writer.write(String.format("%f\t%f\t%f\t%f\n", new Object[]{queryrect.min_x, queryrect.min_y, queryrect.max_x, queryrect.max_y}));
					}
					
					++i;
				}
				OwnMethods.Print(true_count);
				id_writer.close();
				rectangle_writer.close();
				selectivity *= 10.0;
			}
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
//			MyRectangle p_total_range = OwnMethods.GetEntityRange((String)entity_path);
			String result_path_time = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_querytime_false.csv";
			String result_path_count = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/" + datasource + "_Traversal_accesscount_false.csv";
			String log_path = "/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/result/log_Traversal_false.log";
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + " Hot\n"));
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(datasource) + "\t" + target_folder + "\n"));
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)String.format("Traversal\t%s\t%d\n", datasource, target_folder));
			
			double selectivity = 1.0E-6;
			int experiment_count = 100;
			String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/neo4j-community-2.3.3_Traversal_%d", datasource, target_folder);
			OwnMethods.ClearCache((String)password);
			Neo4j_Graph_Store.StartServer(db_path);
			Thread.currentThread();
			Thread.sleep(2000);
			while (selectivity < 0.9) {
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(selectivity) + "\t"));
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(selectivity) + "\n"));
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
					Traversal traversal = new Traversal();
					long start = System.currentTimeMillis();
					boolean result = traversal.ReachabilityQueryDFS(id, queryrect);
					long one_time = System.currentTimeMillis() - start;
					time = (int)((long)time + one_time);
					total_neo4j_time = (int)((long)total_neo4j_time + traversal.Neo4jTime);
					visitednode_count += traversal.access_count;
					if (result && true_count_record) {
						++true_count;
					}
					OwnMethods.Print((Object)String.format("Time: %d", one_time));
					OwnMethods.Print((Object)String.format("Visited count: %d", traversal.access_count));
					OwnMethods.Print((Object)result);
					OwnMethods.WriteFile((String)log_path, (boolean)true, (String)(String.valueOf(String.format("%d\t%d\t%d\t%d\t", i, one_time, traversal.Neo4jTime, traversal.access_count)) + result + "\n"));
					++i;
				}
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(time / experiment_count) + "\t" + total_neo4j_time / experiment_count + "\t"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)(String.valueOf(visitednode_count / experiment_count) + "\t"));
				true_count_record = false;
				OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)(String.valueOf(true_count) + "\n"));
				OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
				OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
				selectivity *= 10.0;
			}
			Neo4j_Graph_Store.StopServer(db_path);
			OwnMethods.WriteFile((String)result_path_time, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)result_path_count, (boolean)true, (String)"\n");
			OwnMethods.WriteFile((String)log_path, (boolean)true, (String)"\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void App() {
		int start = 153933;
		MyRectangle query_rect = new MyRectangle(-115.487384, 36.047015, -115.095464, 36.120295);
		Traversal traversal = new Traversal();
		boolean result = traversal.ReachabilityQuery(start, query_rect);
		OwnMethods.Print((Object)result);
	}

//	static String datasource = "Yelp";
//	static int target_folder = 1;
	
//	static String datasource = "citeseerx";
	static String datasource = "foursquare";
	static int target_folder = 1;
	
	public static void main(String[] args) {
//		Traversal.Experiment_Yelp_Implementation(datasource, 1);
//		Experiment_Hot_False(datasource, target_folder);
		Experiment_Hot_True(datasource, target_folder);
//		Experiment_Hot_True_Test(datasource, target_folder);
//		LoadData();
	}
}