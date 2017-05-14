/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.datasyslab.GeoReach.App$1
 *  org.datasyslab.GeoReach.App$2
 *  org.datasyslab.GeoReach.App$3
 *  org.datasyslab.GeoReach.Batch_Inserter
 *  org.datasyslab.GeoReach.OwnMethods
 */
package org.datasyslab.GeoReach;

import java.util.ArrayList;
import java.util.Iterator;
import org.datasyslab.GeoReach.App;
import org.datasyslab.GeoReach.Batch_Inserter;
import org.datasyslab.GeoReach.OwnMethods;

public class App {
    public static ArrayList<String> datasource_a = new ArrayList<String>();

    public static void LoadData_Ratio() {
        String distribution = "Random_spatial_distributed";
        int MC = 0;
        int MR = 100;
        int ratio = 20;
        while (ratio <= 80) {
            for (String datasource : datasource_a) {
                String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
                String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
                int MG = datasource.equals("go_uniprot") ? 128 : (datasource.equals("Patents") ? 128 : 128);
                String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/GeoReach_%s_%d_%d.txt", datasource, distribution, ratio, MG);
                String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%s_%d_%d_%d_%d", distribution, ratio, MG, MR, MC);
                String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/data/graph.db", datasource, db_folder_name);
                new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
            }
            ratio += 20;
        }
    }

    public static void LoadData_Distribution() {
        String distribution = "Random_spatial_distributed";
        int ratio = 40;
        String datasource = "citeseerx";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
        int MG = 128;
        String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/GeoReach_%s_%d_%d.txt", datasource, distribution, ratio, MG);
        String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d", MG);
        String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/%s/data/graph.db", datasource, db_folder_name);
        new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
    }

    public static void LoadData_YelpFolder() {
        String distribution = "Random_spatial_distributed";
        int target_folder = 1;
        int MT = 0;
        int MR = 200;
        String datasource = "foursquare";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
        int MG = 16384;
        String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MG/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution, target_folder, MG, MR, MT);
        String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MG, target_folder);
        String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/%s/data/graph.db", datasource, db_folder_name);
        new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
    }

    public static void LoadData_foursquare() {
        String distribution = "Random_spatial_distributed";
        int target_folder = 2;
        int MT = 0;
        int MR = 200;
        String datasource = "foursquare";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/graph_dag_newformat.txt", datasource, distribution, target_folder);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/entity_dag_newformat.txt", datasource, distribution, target_folder);
        int MG = 16384;
        String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MG/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution, target_folder, MG, MR, MT);
        String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MG, target_folder);
        String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/%s/data/graph.db", datasource, db_folder_name);
        new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
    }

    public static void LoadData_MG() {
        int MG;
        String distribution = "Random_spatial_distributed";
        int ratio = 40;
        int MT = 0;
        int MR = 200;
        String datasource = "citeseerx";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
        ArrayList<Integer> MGs = new ArrayList<Integer>();
        if (datasource.equals("go_uniprot")) {
            MG = 8;
            while (MG <= 64) {
                MGs.add(MG);
                MG *= 2;
            }
        } else {
            MGs.add(32);
            MGs.add(512);
            MGs.add(2048);
        }
        Iterator iterator = MGs.iterator();
        while (iterator.hasNext()) {
            MG = (Integer)iterator.next();
            String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_data/%s/GeoReachIndex/MG/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution, ratio, MG, MR, MT);
            String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MG, ratio);
            String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MG_Experiment/%s/data/graph.db", datasource, db_folder_name);
            new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
        }
    }

    public static void LoadData_MR() {
        String distribution = "Random_spatial_distributed";
        int ratio = 40;
        int MG = 0;
        int MT = 0;
        String datasource = "citeseerx";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
        ArrayList<Integer> MRs = new ArrayList<Integer>();
        Iterator iterator = MRs.iterator();
        while (iterator.hasNext()) {
            int MR = (Integer)iterator.next();
            String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/GeoReachIndex/MR/GeoReach_%s_%d_%d_%d_%d.txt", datasource, distribution, ratio, MG, MR, MT);
            String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d_%d", MR, ratio);
            String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MR_Experiment/%s/data/graph.db", datasource, db_folder_name);
            OwnMethods.Print((Object)String.format("Loading to %s", datasource));
            new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
        }
    }

    public static void LoadData_MC() {
        String distribution = "Random_spatial_distributed";
        int ratio = 40;
        int MG = 16384;
        String datasource = "citeseerx";
        String graph_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/new_graph.txt", datasource);
        String entity_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/%s/%d/new_entity.txt", datasource, distribution, ratio);
        ArrayList<Integer> MCs = new ArrayList<Integer>();
        Iterator iterator = MCs.iterator();
        while (iterator.hasNext()) {
            int MC = (Integer)iterator.next();
            String GeoReach_filepath = String.format("/home/yuhansun/Documents/share/Real_Data/%s/GeoReachIndex/MC/GeoReach_%s_%d_%d_%d.txt", datasource, distribution, ratio, MG, MC);
            String db_folder_name = String.format("neo4j-community-2.3.3_GeoReach_%d", MC);
            String db_filepath = String.format("/home/yuhansun/Documents/Real_data/%s/MC_Experiment/%s/data/graph.db", datasource, db_folder_name);
            OwnMethods.Print((Object)String.format("Loading to %s", datasource));
            new org.datasyslab.GeoReach.Batch_Inserter(graph_filepath, entity_filepath, GeoReach_filepath, db_filepath);
        }
    }

    public static void Arbitary() {
        OwnMethods.Print((Object)"Success");
    }

    public static void main(String[] args) {
        App.LoadData_MC();
    }
}