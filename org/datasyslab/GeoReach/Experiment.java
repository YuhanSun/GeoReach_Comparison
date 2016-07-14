package org.datasyslab.GeoReach;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.print.attribute.Size2DSyntax;

import org.neo4j.logging.Log;
import org.neo4j.unsafe.impl.batchimport.NodeStage;

public class Experiment {
	
	public static ArrayList<String> datasource_a = new ArrayList<String>(){{
	    add("citeseerx");
	    add("go_uniprot");
	    add("Patents");
	    add("uniprotenc_150m");
	    
	}};

	public static void GenerateQueryRectangle(int experiment_count, double rect_size, double total_range_size, String filepath)
	{
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(filepath, false);
			Random r = new Random();
			for (int i = 0;i<experiment_count; i++)
			{
				double minx = r.nextDouble() * (total_range_size - rect_size);
				double miny = r.nextDouble() * (total_range_size - rect_size);
				
				fileWriter.write(String.format("%f\t%f\t%f\t%f\n", minx, miny, minx + rect_size, miny + rect_size));
			}
			fileWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			if(fileWriter != null)
			{
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void GenerateQueryRectangle()
	{
		int experiment_count = 500;
		double total_range_size = 1000;
		
		double selectivity = 0.0001;
		while (selectivity < 0.9)
		{
			double rect_size = total_range_size * Math.sqrt(selectivity);
			
			int log = (int)Math.log10(selectivity);
			String filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
			
			GenerateQueryRectangle(experiment_count, rect_size, total_range_size, filepath);
			selectivity *= 10;
		}
	}
	
	public static ArrayList<String> ReadExperimentNode(String filepath)
	{
		ArrayList<String> al = new ArrayList<String>();
		BufferedReader reader  = null;
		File file = null;
		try
		{
			file = new File(filepath);
			reader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = reader.readLine())!=null)
			{
				al.add(temp);
			}
			reader.close();
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
		finally
		{
			if(reader!=null)
			{
				try
				{
					reader.close();
				}
				catch(IOException e)
				{					
				}
			}
		}
		return al;
	}
	
	public static ArrayList<MyRectangle> ReadExperimentQueryRectangle(String filepath)
	{
		ArrayList<MyRectangle> queryrectangles = new ArrayList<MyRectangle>();
		BufferedReader reader  = null;
		File file = null;
		try
		{
			file = new File(filepath);
			reader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = reader.readLine())!=null)
			{
				String[] line_list = temp.split("\t");
				MyRectangle rect = new MyRectangle(Double.parseDouble(line_list[0]), Double.parseDouble(line_list[1]), Double.parseDouble(line_list[2]), Double.parseDouble(line_list[3]));
				queryrectangles.add(rect);
			}
			reader.close();
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
		finally
		{
			if(reader!=null)
			{
				try
				{
					reader.close();
				}
				catch(IOException e)
				{					
				}
			}
		}
		return queryrectangles;
	}
	
	public static void Experiment_MG_Implementation()
	{
		try
		{
			double total_range_size = 1000;
			MyRectangle p_total_range = new MyRectangle(0,0,total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/MG/querytime.csv";
			String result_path_count = "/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/MG/accesscount.csv";
			
			
//			for (String datasource : datasource_a)
			String datasource = "Patents";
			{
				OwnMethods.WriteFile(result_path_time, true, datasource+"\n");
				OwnMethods.WriteFile(result_path_count, true, datasource+"\n");
				
				OwnMethods.WriteFile(result_path_time, true, "selectivity\t32_time\t128_time\t512_time\t2048_time\n");
				OwnMethods.WriteFile(result_path_count, true, "selectivity\t32_count\t128_count\t512_count\t2048_count\n");
				
//				OwnMethods.WriteFile(result_path_time, true, "selectivity\t8_time\t16_time\t32_time\t64_time\n");
//				OwnMethods.WriteFile(result_path_count, true, "selectivity\t8_count\t16_count\t32_count\t64_count\n");
				
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
				ArrayList<String> nodeids = ReadExperimentNode(querynodeid_filepath);
				double selectivity = 0.0001;
				while (selectivity < 0.9)
				{
					OwnMethods.WriteFile(result_path_time, true, selectivity+"\t");
					OwnMethods.WriteFile(result_path_count, true, selectivity+"\t");
					
					int log = (int)Math.log10(selectivity);
					String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
					ArrayList<MyRectangle> queryrectangles = ReadExperimentQueryRectangle(queryrectangle_filepath);

					int true_count = 0;
					boolean true_count_record = true;
					for (int MG = 32; MG <= 2048; MG *= 4)
					//for( int MG = 2048; MG >= 32; MG /= 4)
//					for (int MG = 8; MG <= 64; MG *= 2)
					{
						String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/neo4j-community-2.3.3_GeoReach_%d", datasource, MG);
						OwnMethods.Print(OwnMethods.RestartNeo4jServerClearCache(db_path));
						
						Thread.currentThread().sleep(5000);

						GeoReach geo = new GeoReach(p_total_range, p_split_pieces);
						int visitednode_count = 0;
						int time = 0;

						for(int i = 0;i<nodeids.size();i++)
						{
							OwnMethods.Print(i);
							int id = Integer.parseInt(nodeids.get(i));
							MyRectangle queryrect = queryrectangles.get(i);

							long start = System.currentTimeMillis();
							boolean result = geo.ReachabilityQuery(id, queryrect);
							time += System.currentTimeMillis() - start;
							visitednode_count += geo.visited_count;
							if(result && true_count_record)
								true_count++;
						}
						
						OwnMethods.Print(Neo4j_Graph_Store.StopServer(db_path));

						OwnMethods.WriteFile(result_path_time, true, time / nodeids.size()+"\t");
						OwnMethods.WriteFile(result_path_count, true, visitednode_count + "\t");
						
						true_count_record = false;
					}
					
					OwnMethods.WriteFile(result_path_time, true, true_count + "\n");
					OwnMethods.WriteFile(result_path_count, true, "\n");

					selectivity *= 10;
				}
				
				OwnMethods.WriteFile(result_path_time, true, "\n");
				OwnMethods.WriteFile(result_path_count, true, "\n");
				
				

			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void Experiment_MR_Implementation()
	{
		try
		{
			double total_range_size = 1000;
			MyRectangle p_total_range = new MyRectangle(0,0,total_range_size, total_range_size);
			int p_split_pieces = 128;
			String result_path_time = "/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/MR/querytime.csv";
			String result_path_count = "/home/yuhansun/Documents/Real_data/GeoReach_Experiment/result/MR/accesscount.csv";
			
			
			for (String datasource : datasource_a)
//			String datasource = "go_uniprot";
			{
				OwnMethods.WriteFile(result_path_time, true, datasource+"\n");
				OwnMethods.WriteFile(result_path_count, true, datasource+"\n");
				
//				OwnMethods.WriteFile(result_path_time, true, "selectivity\t32_time\t128_time\t512_time\t2048_time\n");
//				OwnMethods.WriteFile(result_path_count, true, "selectivity\t32_count\t128_count\t512_count\t2048_count\n");
				
				OwnMethods.WriteFile(result_path_time, true, "selectivity\t0_time\t35_time\t70_time\t105_time\n");
				OwnMethods.WriteFile(result_path_count, true, "selectivity\t0_count\t35_count\t70_count\t105_count\n");
				
				String querynodeid_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/experiment_id.txt", datasource);
				ArrayList<String> nodeids = ReadExperimentNode(querynodeid_filepath);
				double selectivity = 0.0001;
				while (selectivity < 0.9)
				{
					OwnMethods.WriteFile(result_path_time, true, selectivity+"\t");
					OwnMethods.WriteFile(result_path_count, true, selectivity+"\t");
					
					int log = (int)Math.log10(selectivity);
					String queryrectangle_filepath = String.format("/home/yuhansun/Documents/Real_data/GeoReach_Experiment/experiment_query/%d.txt", log);
					ArrayList<MyRectangle> queryrectangles = ReadExperimentQueryRectangle(queryrectangle_filepath);

					for (int MR = 0; MR <= 105; MR += 35)
					{
						String db_path = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/neo4j-community-2.3.3_GeoReach_%d", datasource, MR);
						OwnMethods.Print(OwnMethods.RestartNeo4jServerClearCache(db_path));
						
						Thread.currentThread().sleep(5000);

						GeoReach geo = new GeoReach(p_total_range, p_split_pieces);
						int visitednode_count = 0;
						int time = 0;

						for(int i = 0;i<nodeids.size();i++)
						{
							OwnMethods.Print(i);
							int id = Integer.parseInt(nodeids.get(i));
							MyRectangle queryrect = queryrectangles.get(i);

							long start = System.currentTimeMillis();
							geo.ReachabilityQuery(id, queryrect);
							time += System.currentTimeMillis() - start;
							visitednode_count += geo.visited_count;
						}
						
						OwnMethods.Print(Neo4j_Graph_Store.StopServer(db_path));

						OwnMethods.WriteFile(result_path_time, true, time / nodeids.size()+"\t");
						OwnMethods.WriteFile(result_path_count, true, visitednode_count + "\t");
					}
					
					OwnMethods.WriteFile(result_path_time, true, "\n");
					OwnMethods.WriteFile(result_path_count, true, "\n");

					selectivity *= 10;
				}
				
				OwnMethods.WriteFile(result_path_time, true, "\n");
				OwnMethods.WriteFile(result_path_count, true, "\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public static void IndexSize()
	{
		for(String distribution: new ArrayList<String>(){{add("Clustered_distributed"); add("Zipf_distributed");}})
		{
			String resultpath = String.format("/home/yuhansun/Documents/GeoReach_Comparison_Experiment_Result/%s_index_size.csv", distribution);
			for (String datasource : datasource_a)
			{
				OwnMethods.WriteFile(resultpath, true, datasource + "\n");
				
				int MG;
				if(datasource.equals("Patents"))
					MG = 32;
				else {
					if(datasource.equals("go_uniprot"))
						MG = 8;
					else
						MG = 128;
				}
				for (int ratio = 20; ratio <= 80; ratio += 20)
				{
					String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/GeoReachIndex/GeoReach_%s_%d_%d.txt", datasource, distribution, ratio, MG);
					long size = OwnMethods.GeoReachIndexSize(GeoReach_filepath);
					OwnMethods.WriteFile(resultpath, true, ratio + "\t" + size + "\n");
				}
				OwnMethods.WriteFile(resultpath, true, "\n");
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Experiment_MG_Implementation();
//		IndexSize();
//		GenerateQueryRectangle();
//		Experiment_MR_Implementation();
	}

}
