package org.datasyslab.GeoReach;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.postgresql.gss.MakeGSS;

/**
 * Hello world!
 *
 */
public class App 
{
	public static ArrayList<String> datasource_a = new ArrayList<String>(){{
	    add("citeseerx");
	    add("go_uniprot");
	    add("Patents");
	    add("uniprotenc_150m");
	    
	}};
	
	public static void LoadData_MG()
	{
		String distribution = "Random_spatial_distributed";
		int ratio = 20;
		
//		for (String datasource : datasource_a)
		String datasource = "go_uniprot";
		{
			String graph_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/new_graph.txt", datasource);
			String entity_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
			
//			for (int MG = 32; MG <= 2048; MG *=4)
			ArrayList<Integer> MGs = new ArrayList<Integer>()
			{
				{
					add(8);
					add(16);
					add(64);
				}
			};
			for (int MG : MGs)
			{
				String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MG/GeoReach_%d.txt", datasource, MG);
				String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d", MG);
				String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/%s/data/graph.db", datasource, db_folder_name);
				new Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
			}
		}
	}
	

	public static void LoadData_MR()
	{
		String distribution = "Random_spatial_distributed";
		int ratio = 20;
		
		for (String datasource : datasource_a)
//		String datasource = "go_uniprot";
		{
			String graph_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/new_graph.txt", datasource);
			String entity_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
			
			for (int MR = 0; MR <= 105; MR += 35)
//			ArrayList<Integer> MGs = new ArrayList<Integer>()
//			{
//				{
//					add(8);
//					add(16);
//					add(64);
//				}
//			};
//			for (int MG : MGs)
			{
				String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MR/GeoReach_%d.txt", datasource, MR);
				String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d", MR);
				String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/%s/data/graph.db", datasource, db_folder_name);
				new Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
			}
		}
	}
	
	
    public static void main( String[] args )
    {
//    	LoadData_MR();
//    	LoadData_MG();
//    	test();
    }
}
