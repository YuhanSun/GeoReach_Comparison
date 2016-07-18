package org.datasyslab.GeoReach;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.spec.MGF1ParameterSpec;
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
	
	public static void LoadData_Distribution()
	{
		String distribution = "Random_spatial_distributed";
		int ratio = 20;
		
		for (String datasource : datasource_a)
//		String datasource = "Patents";
		{
			String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
			String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
			
			int MG;
			if(datasource.equals("go_uniprot"))
				MG = 8;
			else
			{
				if(datasource.equals("Patents"))
					MG = 32;
				else
					MG = 128;
			}
				
			{
				String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/GeoReach_%s_%d_%d.txt", datasource, distribution, ratio, MG);
				String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d", MG);
				String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/data/graph.db", datasource, db_folder_name);
				new Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
			}
		}
	}
	
	public static void LoadData_MG()
	{
		String distribution = "Random_spatial_distributed";
		int ratio = 80, MT = 0, MR = 200;
		
		
		for (String datasource : datasource_a)
//		String datasource = "Patents";
		{
			String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
			String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
			
			ArrayList<Integer> MGs = new ArrayList<Integer>();
			
			if(datasource.equals("go_uniprot"))
			{
				for(int MG = 8;MG<=64; MG*=2)
					MGs.add(MG);
			}
			else
			{
				for(int MG = 32; MG <= 2048; MG *= 4)
					MGs.add(MG);
			}
			for (int MG : MGs)
			{
				String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MG/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution,ratio, MG, MR, MT);
				String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MG, ratio);
				String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/%s/data/graph.db", datasource, db_folder_name);
				new Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
			}
		}
	}
	

	public static void LoadData_MR()
	{
		String distribution = "Random_spatial_distributed";
		int ratio = 80, MG = 0, MT = 0;
		
		for (String datasource : datasource_a)
//		String datasource = "go_uniprot";
		{
			String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
			String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
			
//			for (int MR = 0; MR <= 105; MR += 35)
			int MR = 50;
			{
				String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MR/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution,ratio, MG, MR, MT);
				String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MR, ratio);
				String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/%s/data/graph.db", datasource, db_folder_name);
				new Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
			}
		}
	}
	
//	public static void CheckCorrectness(String GeoReach_filepath, String ReachGrid_filepath)
//	{
//		String GeoReach_line = null, ReachGrid_line = null;
//		BufferedReader GeoReach_reader = null, ReachGrid_reader = null;
//		
//		try
//		{
//			GeoReach_reader = new BufferedReader(new FileReader(new File(GeoReach_line)));
//			ReachGrid_reader = new BufferedReader(new FileReader(new File(ReachGrid_filepath)));
//			
//			ReachGrid_reader.readLine();
//			
//			while((GeoReach_line = GeoReach_reader.readLine())!=null)
//			{
//				String[] GeoReach_l = GeoReach_line.split(",");
//				
//				String[] ReachGrid_l = ReachGrid_line.split(" ");
//				
//				int id = Integer.parseInt(GeoReach_l[0]);
//				
//				int type = Integer.parseInt(GeoReach_l[1]);
//				if(type == 0)
//				{
//					if(Integer.parseInt(ReachGrid_l[1]) != (GeoReach_l.length - 2))
//						OwnMethods.Print("Grid count inconsistent on id " + id);
//					for(int i = 2;i<GeoReach_l.length - 1;i++)
//					{
//						if(Integer.parseInt(ReachGrid_l[i]) != Integer.parseInt(GeoReach_l[i]))
//							OwnMethods.Print(String.format("%d:\n%s\n%s", ReachGrid_l[i], GeoReach_l[i]));
//					}
//				}
//				if(type == 1)
//				{
//					
//				}
//			}
//		}
//	}
	
	
    public static void main( String[] args )
    {
//    	LoadData_Distribution();
    	LoadData_MR();
//    	LoadData_MG();
//    	test();
    }
}
