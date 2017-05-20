package org.datasyslab.Experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.datasyslab.GeoReach.*;
import org.datasyslab.GeoReach.Config.Distribution;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

public class Experiment_Prepare {

	//	static String dataset;
	static ArrayList<String> dataset_a = new ArrayList<String>(Arrays.asList("uniprotenc_150m", "Patents", "go_uniprot", "citeseerx")); 
	//	static ArrayList<String> dataset_a = new ArrayList<String>(Arrays.asList("go_uniprot", "citeseerx"));
	static ArrayList<String> distribution_a = new ArrayList<String>(Arrays.asList(Distribution.Random_spatial_distributed.name(),
			Distribution.Clustered_distributed.name(), Distribution.Zipf_distributed.name())); 

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//		generateCenterIDForRatio();
		//		OwnMethods.Print(dataset_a);
		//		generateQueryRectangleForRatio();

		//		generateCenterIDForDistribution();
//		generateQueryRectangleForDistribution();
		
//		generateCenterIDForSelectivity();
		generateQueryRectangleForSelectivity();


	}


	public static void generateQueryRectangleForSelectivity()
	{
		try {
			int experiment_count = 50;
			int target_folder = 20;
			String distribution = Distribution.Random_spatial_distributed.name();
			for (String datasource : dataset_a)
				//			String datasource = "Patents";
			{
				OwnMethods.Print(String.format("%s\t%s\n", datasource, distribution));
				String entity_path = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
						datasource, distribution, target_folder);
				ArrayList<Entity> entities = OwnMethods.ReadEntity((String)entity_path);
				int spa_count = OwnMethods.GetSpatialEntityCount(entities);
				STRtree stRtree = OwnMethods.ConstructSTRee(entities);

				String center_id_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/selectivity/"
						+ "%s_%s_%d_centerids.txt", datasource, distribution, target_folder);
				ArrayList<Integer> center_ids = OwnMethods.ReadCenterID(center_id_path);
				ArrayList<Integer> final_center_ids = OwnMethods.GetRandom_NoDuplicate(center_ids, experiment_count);

				double selectivity = 0.0001;
				while ( selectivity < 0.2)
				{
					int name_suffix = (int) (selectivity * spa_count);
					String output_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/selectivity/"
							+ "%s_%s_%d_queryrect_%d.txt", datasource, distribution, target_folder, name_suffix);
					String write_line = "";
					for (int id : final_center_ids)
					{
						double lon = entities.get(id).lon;
						double lat = entities.get(id).lat;
						GeometryFactory factory = new GeometryFactory();
						Point center = factory.createPoint(new Coordinate(lon, lat));
						Object[] result = stRtree.kNearestNeighbour(center.getEnvelopeInternal(),
								new GeometryFactory().toGeometry(center.getEnvelopeInternal()),
								new GeometryItemDistance(), name_suffix);
						double radius = 0.0;
						for (Object object : result)
						{
							Point point = (Point) object;
							double dist = center.distance(point);
							if(dist > radius)
								radius = dist;
						}
						double a = Math.sqrt(Math.PI) * radius;
						double minx = center.getX() - a / 2;
						double miny = center.getY() - a / 2;
						double maxx = center.getX() + a / 2;
						double maxy = center.getY() + a / 2;

						write_line = String.format("%f\t%f\t%f\t%f\n", minx, miny, maxx, maxy);
						OwnMethods.WriteFile(output_path, true, write_line);
					}
					selectivity *= 10;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void generateCenterIDForSelectivity()
	{
		int target_folder = 20;
		String distribution = Distribution.Random_spatial_distributed.name();
		for (String datasource : dataset_a)
		{
			OwnMethods.Print(String.format("%s\t%s\n", datasource, distribution));
			String entityPath = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
					datasource, distribution, target_folder);
			ArrayList<Entity> entities = OwnMethods.ReadEntity(entityPath);
			String outputPath = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/selectivity/"
					+ "%s_%s_%d_centerids.txt", datasource, distribution, target_folder);
			generateQueryRectangleCenterID(entities, outputPath, 500);
		}
	}

	public static void generateQueryRectangleForDistribution()
	{
		try {
			int experiment_count = 500;
			int target_folder = 20;
			for (String datasource : dataset_a)
				//			String datasource = "Patents";
			{
				for ( String distribution : distribution_a)
				{
					OwnMethods.Print(String.format("%s\t%s\n", datasource, distribution));
					String entity_path = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
							datasource, distribution, target_folder);
					ArrayList<Entity> entities = OwnMethods.ReadEntity((String)entity_path);
					int spa_count = OwnMethods.GetSpatialEntityCount(entities);
					STRtree stRtree = OwnMethods.ConstructSTRee(entities);

					String center_id_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/distribution/"
							+ "%s_%s_%d_centerids.txt", datasource, distribution, target_folder);
					ArrayList<Integer> center_ids = OwnMethods.ReadCenterID(center_id_path);
					ArrayList<Integer> final_center_ids = OwnMethods.GetRandom_NoDuplicate(center_ids, experiment_count);

					double selectivity = 0.001;
					int name_suffix = (int) (selectivity * spa_count);
					String write_line = "";
					for (int id : final_center_ids)
					{
						double lon = entities.get(id).lon;
						double lat = entities.get(id).lat;
						GeometryFactory factory = new GeometryFactory();
						Point center = factory.createPoint(new Coordinate(lon, lat));
						Object[] result = stRtree.kNearestNeighbour(center.getEnvelopeInternal(),
								new GeometryFactory().toGeometry(center.getEnvelopeInternal()),
								new GeometryItemDistance(), name_suffix);
						double radius = 0.0;
						for (Object object : result)
						{
							Point point = (Point) object;
							double dist = center.distance(point);
							if(dist > radius)
								radius = dist;
						}
						double a = Math.sqrt(Math.PI) * radius;
						double minx = center.getX() - a / 2;
						double miny = center.getY() - a / 2;
						double maxx = center.getX() + a / 2;
						double maxy = center.getY() + a / 2;

						write_line += String.format("%f\t%f\t%f\t%f\n", minx, miny, maxx, maxy);
					}
					String output_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/distribution/"
							+ "%s_%s_%d_queryrect_%d.txt", datasource, distribution, target_folder, name_suffix);
					OwnMethods.WriteFile(output_path, true, write_line);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void generateCenterIDForDistribution()
	{
		int target_folder = 20;
		for (String datasource : dataset_a)
		{
			for ( String distribution : distribution_a)
			{
				OwnMethods.Print(String.format("%s\t%s\n", datasource, distribution));
				String entityPath = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
						datasource, distribution, target_folder);
				ArrayList<Entity> entities = OwnMethods.ReadEntity(entityPath);
				String outputPath = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/distribution/"
						+ "%s_%s_%d_centerids.txt", datasource, distribution, target_folder);
				generateQueryRectangleCenterID(entities, outputPath, 500);
			}
		}
	}

	public static void generateQueryRectangleForRatio()
	{
		try {
			int experiment_count = 500;
			String distribution = Distribution.Random_spatial_distributed.name();
			for (String datasource : dataset_a)
				//			String datasource = "Patents";
			{
				OwnMethods.Print(datasource);
				for ( int target_folder = 20; target_folder < 90; target_folder += 20)
					//				int target_folder = 80;
				{
					String entity_path = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
							datasource, distribution, target_folder);
					ArrayList<Entity> entities = OwnMethods.ReadEntity((String)entity_path);
					int spa_count = OwnMethods.GetSpatialEntityCount(entities);
					STRtree stRtree = OwnMethods.ConstructSTRee(entities);

					String center_id_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/%s/"
							+ "%s_%d_centerids.txt", datasource, distribution, target_folder);
					ArrayList<Integer> center_ids = OwnMethods.ReadCenterID(center_id_path);
					ArrayList<Integer> final_center_ids = OwnMethods.GetRandom_NoDuplicate(center_ids, experiment_count);

					double selectivity = 0.001;
					//					while (selectivity < 0.9)
					{
						int name_suffix = (int) (selectivity * spa_count);
						String write_line = "";
						for (int id : final_center_ids)
						{
							double lon = entities.get(id).lon;
							double lat = entities.get(id).lat;
							GeometryFactory factory = new GeometryFactory();
							Point center = factory.createPoint(new Coordinate(lon, lat));
							Object[] result = stRtree.kNearestNeighbour(center.getEnvelopeInternal(),
									new GeometryFactory().toGeometry(center.getEnvelopeInternal()),
									new GeometryItemDistance(), name_suffix);
							double radius = 0.0;
							for (Object object : result)
							{
								Point point = (Point) object;
								double dist = center.distance(point);
								if(dist > radius)
									radius = dist;
							}
							//							OwnMethods.Print(radius);
							double a = Math.sqrt(Math.PI) * radius;
							double minx = center.getX() - a / 2;
							double miny = center.getY() - a / 2;
							double maxx = center.getX() + a / 2;
							double maxy = center.getY() + a / 2;

							write_line += String.format("%f\t%f\t%f\t%f\n", minx, miny, maxx, maxy);
						}
						String output_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/"
								+ "%s/%s_%d_queryrect_%d.txt", datasource, distribution, target_folder, name_suffix);
						OwnMethods.WriteFile(output_path, true, write_line);
						selectivity *= 10;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static void generateCenterIDForRatio()
	{
		for (String datasource : dataset_a)
		{
			OwnMethods.Print(datasource);
			for ( int target_folder = 20; target_folder < 90; target_folder += 20)
				GenerateQueryRectangleCenterID(datasource, Distribution.Random_spatial_distributed.name(), target_folder);
		}
	}

	/**
	 * generate square query range
	 * center location space oriented
	 * spatial selectivity considering space area
	 * @param experiment_count
	 * @param rect_size
	 * @param total_range_size
	 * @param filepath
	 */
	public static void GenerateQueryRectangle(int experiment_count, double rect_size, double total_range_size, String filepath) {
		block13 : {
		FileWriter fileWriter = null;
		try {
			try {
				fileWriter = new FileWriter(filepath, false);
				Random r = new Random();
				int i = 0;
				while (i < experiment_count) {
					double minx = r.nextDouble() * (total_range_size - rect_size);
					double miny = r.nextDouble() * (total_range_size - rect_size);
					fileWriter.write(String.format("%f\t%f\t%f\t%f\n", minx, miny, minx + rect_size, miny + rect_size));
					++i;
				}
				fileWriter.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				if (fileWriter == null) break block13;
				try {
					fileWriter.close();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	}

	/**
	 * spatial selectivity considering space area
	 * center location space oriented
	 * @param experiment_count
	 * @param rect_size_x
	 * @param rect_size_y
	 * @param total_range
	 * @param filepath
	 */
	public static void GenerateQueryRectangle(int experiment_count, double rect_size_x, double rect_size_y, MyRectangle total_range, String filepath) {
		block13 : {
		FileWriter fileWriter = null;
		try {
			try {
				fileWriter = new FileWriter(filepath, false);
				Random r = new Random();
				int i = 0;
				while (i < experiment_count) {
					double minx = r.nextDouble() * (total_range.max_x - total_range.min_x) + total_range.min_x;
					double miny = r.nextDouble() * (total_range.max_y - total_range.min_y) + total_range.min_y;
					fileWriter.write(String.format("%f\t%f\t%f\t%f\n", minx, miny, minx + rect_size_x, miny + rect_size_x));
					++i;
				}
				fileWriter.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				if (fileWriter == null) break block13;
				try {
					fileWriter.close();
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	}

	/**
	 * center location oriented query rectangle
	 * spatial selectivity considering space area
	 * @param entities
	 * @param center_ids
	 * @param experiment_count
	 * @param rect_size_x
	 * @param rect_size_y
	 * @param total_range
	 * @param filepath
	 */
	public static void GenerateQueryRectangleReferEntity(ArrayList<Entity> entities, ArrayList<Integer> center_ids, 
			int experiment_count, double rect_size_x, double rect_size_y, MyRectangle total_range, String filepath)
	{
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(filepath, false);
			for (int i = 0; i < experiment_count; i++)
			{
				int center_id = ((Integer)center_ids.get(i)).intValue();
				Entity p_Entity = (Entity)entities.get(center_id);
				if (p_Entity.IsSpatial)
				{
					double minx = p_Entity.lon - rect_size_x / 2.0;
					double miny = p_Entity.lat - rect_size_y / 2.0;
					fileWriter.write(String.format("%f\t%f\t%f\t%f\n", Double.valueOf(minx), Double.valueOf(miny), 
							Double.valueOf(minx + rect_size_x), Double.valueOf(miny + rect_size_y)));
				}
				else
				{
					OwnMethods.Print(String.format("%dth Entity %d is not spatial", Integer.valueOf(i), Integer.valueOf(center_id)));
					fileWriter.close();
					return;
				}
			}
			fileWriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();


			if (fileWriter != null) {
				try
				{
					fileWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		finally
		{
			if (fileWriter != null) {
				try
				{
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * generate false query
	 * space oriented
	 * spatial selectivity considering space area
	 * @param graph
	 * @param entities
	 * @param experiment_count
	 * @param rect_size_x
	 * @param rect_size_y
	 * @param total_range
	 * @param id_path
	 * @param rectangle_path
	 */
	public static void GenerateFalseQuery(ArrayList<ArrayList<Integer>> graph, ArrayList<Entity> entities, int experiment_count, 
			double rect_size_x, double rect_size_y, MyRectangle total_range, String id_path, String rectangle_path) {
		FileWriter id_writer = null;
		FileWriter rectangle_writer = null;
		try {
			int current_count = 0;
			id_writer = new FileWriter(new File(id_path));
			rectangle_writer = new FileWriter(new File(rectangle_path));
			Random random = new Random();
			while(true)
			{
				int start_id = (int)(random.nextDouble() * (double)entities.size());
				if (entities.get((int)start_id).IsSpatial)
					continue;
				else
				{
					double minx = random.nextDouble() * (total_range.max_x - total_range.min_x) + total_range.min_x;
					double miny = random.nextDouble() * (total_range.max_y - total_range.min_y) + total_range.min_y;
					MyRectangle query_rect = new MyRectangle(minx, minx + rect_size_x, miny, miny + rect_size_y);
					if(OwnMethods.ReachabilityQuery(graph, entities, start_id, query_rect) == false)
					{
						current_count++;
						id_writer.write(String.valueOf(start_id) + "\n");
						rectangle_writer.write(String.format("%f\t%f\t%f\t%f\n", new Object[]{minx, miny, minx + rect_size_x, miny + rect_size_y}));
						if(current_count == experiment_count)
							break;
					}
				}
			}
			id_writer.close();
			rectangle_writer.close();
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void GenerateFalseQuery(String datasource, int target_folder, String distribution)
	{
		String graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
		ArrayList<ArrayList<Integer>> graph = OwnMethods.ReadGraph(graph_path);

		String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
		ArrayList<Entity> entities = OwnMethods.ReadEntity(entity_path);
		MyRectangle total_range = OwnMethods.GetEntityRange(entity_path);

		double selectivity = 1.0E-6;
		while (selectivity < 0.2) 
		{
			int log = (int)Math.log10(selectivity);
			String id_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/false/experiment_id_%s_%d.txt", datasource,log);
			String rectangle_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/false/query_rect_%s_%d.txt", datasource, log);

			double rect_size_x = (total_range.max_x - total_range.min_x) * Math.sqrt(selectivity);
			double rect_size_y = (total_range.max_y - total_range.min_y) * Math.sqrt(selectivity);

			GenerateFalseQuery(graph, entities, 500, rect_size_x, rect_size_y, total_range, id_path, rectangle_path);
			selectivity *= 10;
		}
	}

	public static void GenerateTrueQuery(ArrayList<ArrayList<Integer>> graph, ArrayList<Entity> entities, int experiment_count, double rect_size_x, double rect_size_y, MyRectangle total_range, String id_path, String rectangle_path) {
		FileWriter id_writer = null;
		FileWriter rectangle_writer = null;
		try {
			int current_count = 0;
			id_writer = new FileWriter(new File(id_path));
			rectangle_writer = new FileWriter(new File(rectangle_path));
			Random random = new Random();
			while(true)
			{
				int start_id = (int)(random.nextDouble() * (double)entities.size());
				if (entities.get((int)start_id).IsSpatial)
					continue;
				else
				{
					double minx = random.nextDouble() * (total_range.max_x - total_range.min_x) + total_range.min_x;
					double miny = random.nextDouble() * (total_range.max_y - total_range.min_y) + total_range.min_y;
					MyRectangle query_rect = new MyRectangle(minx, miny, minx + rect_size_x, miny + rect_size_y);
					boolean result = OwnMethods.ReachabilityQuery(graph, entities, start_id, query_rect);
					if(result)
					{
						current_count++;
						id_writer.write(String.valueOf(start_id) + "\n");
						rectangle_writer.write(String.format("%f\t%f\t%f\t%f\n", new Object[]{minx, miny, minx + rect_size_x, miny + rect_size_y}));
						if(current_count == experiment_count)
							break;
					}
				}
			}
			id_writer.close();
			rectangle_writer.close();
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void GenerateTrueQuery(String datasource, int target_folder, String distribution)
	{
		FileWriter id_writer = null;
		FileWriter rectangle_writer = null;
		try
		{
			String graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_entity_newformat.txt", datasource, distribution, target_folder);
			ArrayList<ArrayList<Integer>> graph = OwnMethods.ReadGraph(graph_path);
			OwnMethods.Print("Read graph done");
			int node_count = graph.size();

			String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
			ArrayList<Entity> entities = OwnMethods.ReadEntity(entity_path);
			MyRectangle total_range = OwnMethods.GetEntityRange(entity_path);

			String SCC_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/SCC.txt", datasource, target_folder);
			String original_graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/Random_spatial_distributed/%d/graph_entity_newformat.txt", datasource, target_folder);
			ArrayList refer_table = OwnMethods.ReadSCC((String)SCC_filepath, (String)original_graph_path);

			String graph_dag_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
			ArrayList<ArrayList<Integer>> graph_dag = OwnMethods.ReadGraph(graph_dag_path);
			Random random = new Random();

			OwnMethods.Print("start generating");
			double selectivity = 1.0E-6;
			while (selectivity < 0.2) 
			{
				int log = (int)Math.log10(selectivity);
				String id_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/experiment_id_%s_%d.txt", datasource,log);
				String rectangle_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/true/query_rect_%s_%d.txt", datasource, log);
				id_writer = new FileWriter(new File(id_path));
				rectangle_writer = new FileWriter(new File(rectangle_path));

				double rect_size_x = (total_range.max_x - total_range.min_x) * Math.sqrt(selectivity);
				double rect_size_y = (total_range.max_y - total_range.min_y) * Math.sqrt(selectivity);

				int current_count = 0;
				while(true)
				{
					int id = (int) (random.nextDouble() * node_count);
					id = (Integer) refer_table.get(id);

					OwnMethods.Print(String.format("%d", id));

					double minx = random.nextDouble() * (total_range.max_x - total_range.min_x) + total_range.min_x;
					double miny = random.nextDouble() * (total_range.max_y - total_range.min_y) + total_range.min_y;
					MyRectangle query_rect = new MyRectangle(minx, miny, minx + rect_size_x, miny+rect_size_y);

					OwnMethods.Print(String.format("%f\t%f\t%f\t%f\t",minx,miny,minx + rect_size_x, miny+rect_size_y));

					if(OwnMethods.ReachabilityQuery(graph_dag, entities, id, query_rect))
					{
						current_count++;
						id_writer.write(String.valueOf(id) + "\n");
						rectangle_writer.write(String.format("%f\t%f\t%f\t%f\n", new Object[]{minx, miny, minx + rect_size_x, miny + rect_size_y}));
						if(current_count == 500)
							break;
					}
				}
				id_writer.close();
				rectangle_writer.close();

				//				GenerateTrueQuery(graph, entities, 500, rect_size_x, rect_size_y, total_range, id_path, rectangle_path);
				selectivity *= 10;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * generic function
	 * @param entities
	 * @param center_id_path
	 * @param experimentCount
	 */
	public static void generateQueryRectangleCenterID(ArrayList<Entity> entities, String center_id_path, int experimentCount) 
	{
		try {
			TreeSet<Integer> center_ids_set = new TreeSet<Integer>();
			ArrayList<Integer> center_ids_list = new ArrayList<Integer>();
			Random random = new Random();
			do {
				int center_id = (int)(random.nextDouble() * (double)entities.size());
				if (!((Entity)entities.get((int)center_id)).IsSpatial || !center_ids_set.add(center_id)) continue;
				center_ids_list.add(center_id);
			} while (center_ids_set.size() != experimentCount);
			OwnMethods.WriteFile((String)center_id_path, (boolean)false, (String)"");
			Iterator iterator = center_ids_list.iterator();
			while (iterator.hasNext()) {
				int id = (Integer)iterator.next();
				OwnMethods.WriteFile((String)center_id_path, (boolean)true, (String)String.format("%d\n", id));
			}
		}
		catch (Exception e) {
			// empty catch block
			e.printStackTrace();
		}
	}

	/**
	 * generate center id
	 */
	public static void GenerateQueryRectangleCenterID(String datasource, String distribution, int target_folder) {
		int experiment_count = 500;
		try {
			String entity_path = String.format("/mnt/hgfs/Ubuntu_shared/Real_Data/%s/%s/%s/new_entity.txt", 
					datasource, distribution, target_folder);
			OwnMethods.Print(entity_path);
			ArrayList<Entity> entities = OwnMethods.ReadEntity((String)entity_path);
			String center_id_path = String.format("/mnt/hgfs/Experiment_Result/GeoReach_Experiment/experiment_query/%s/"
					+ "%s_%d_centerids.txt", datasource, distribution, target_folder);
			TreeSet<Integer> center_ids_set = new TreeSet<Integer>();
			ArrayList<Integer> center_ids_list = new ArrayList<Integer>();
			Random random = new Random();
			do {
				int center_id = (int)(random.nextDouble() * (double)entities.size());
				if (!((Entity)entities.get((int)center_id)).IsSpatial || !center_ids_set.add(center_id)) continue;
				center_ids_list.add(center_id);
			} while (center_ids_set.size() != experiment_count);
			OwnMethods.WriteFile((String)center_id_path, (boolean)false, (String)"");
			Iterator iterator = center_ids_list.iterator();
			while (iterator.hasNext()) {
				int id = (Integer)iterator.next();
				OwnMethods.WriteFile((String)center_id_path, (boolean)true, (String)String.format("%d\n", id));
			}
		}
		catch (Exception e) {
			// empty catch block
			e.printStackTrace();
		}
	}

	/**
	 * center location's randomness is space oriented
	 * spatial selectivity considering space area
	 */
	public static void GenerateQueryRectangleRandom() {
		FileWriter fWriter = null;
		try {
			int experiment_count = 500;
			MyRectangle total_range = new MyRectangle(0.0, 0.0, 1000.0, 1000.0);
			Random random = new Random();
			double selectivity = 1.0E-6;
			while (selectivity < 0.2) {
				double rect_size_x = (total_range.max_x - total_range.min_x) * Math.sqrt(selectivity);
				double rect_size_y = (total_range.max_y - total_range.min_y) * Math.sqrt(selectivity);
				int log = (int)Math.log10(selectivity);
				String query_rect_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%d.txt", log);
				fWriter = new FileWriter(query_rect_path);
				int i = 0;
				while (i < experiment_count) {
					double minx = random.nextDouble() * (total_range.max_x - rect_size_x);
					double miny = random.nextDouble() * (total_range.max_y - rect_size_y);
					fWriter.write(String.format("%f\t%f\t%f\t%f\n", minx, miny, minx + rect_size_x, miny + rect_size_y));
					++i;
				}
				fWriter.close();
				selectivity *= 10.0;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void GenerateQueryRectangle(String datasource, String distribution, int target_folder) {
		int experiment_count = 500;
		String entity_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_newformat.txt", 
				datasource, distribution, target_folder);
		ArrayList entities = OwnMethods.ReadEntity((String)entity_path);
		MyRectangle total_range = OwnMethods.GetEntityRange((String)entity_path);
		double selectivity = 1.0E-6;
		while (selectivity < 0.2) {
			String center_id_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_centerids.txt", datasource);
			ArrayList<Integer> center_ids = new ArrayList<Integer>();
			BufferedReader reader = null;
			File file = null;
			try {
				file = new File(center_id_path);
				reader = new BufferedReader(new FileReader(file));
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					center_ids.add(Integer.parseInt(temp));
				}
				reader.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			double rect_size_x = (total_range.max_x - total_range.min_x) * Math.sqrt(selectivity);
			double rect_size_y = (total_range.max_y - total_range.min_y) * Math.sqrt(selectivity);
			int log = (int)Math.log10(selectivity);
			String query_rect_path = String.format("/home/yuhansun/Documents/share/Real_Data/GeoReach_Experiment/experiment_query/%s_%d.txt", datasource, log);
			GenerateQueryRectangleReferEntity(entities, center_ids, experiment_count, rect_size_x, rect_size_y, total_range, query_rect_path);
			selectivity *= 10.0;
		}
	}

	public static void GenerateExperimentID(String datasource, String distribution, int target_folder) {
		Random random = new Random();
		String graph_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_entity_newformat.txt", 
				datasource, distribution, target_folder);
		int graph_nodecount = OwnMethods.GetNodeCountGeneral((String)graph_path);
		int i = 0;
		while (i < 500) {
			double ran = random.nextDouble();
			Double Ran = ran * (double)graph_nodecount;
			int id = Ran.intValue();
			OwnMethods.Print((Object)id);
			String id_path = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/experiment_id.txt", datasource, distribution, target_folder);
			OwnMethods.WriteFile((String)id_path, (boolean)true, (String)String.format("%d\n", id));
			++i;
		}
	}


}
