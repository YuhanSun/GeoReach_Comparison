package org.datasyslab.GeoReach;

import com.sun.jersey.api.client.WebResource;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import org.datasyslab.GeoReach.Entity;
import org.datasyslab.GeoReach.MyRectangle;
import org.datasyslab.GeoReach.Neo4j_Graph_Store;
import org.datasyslab.GeoReach.OwnMethods;
import org.neo4j.graphdb.Node;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OwnMethods {
	
	public static ArrayList<Integer> ReadCenterID(String path)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(new File(path)));
			while ( (line = reader.readLine()) != null )
			{
				int id = Integer.parseInt(line);
				ids.add(id);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ids;
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
	
    public static ArrayList<ArrayList<Integer>> ReadGraph(String graph_path) {
        ArrayList graph = null;
        BufferedReader reader = null;
        String str = null;
        try {
            reader = new BufferedReader(new FileReader(new File(graph_path)));
            str = reader.readLine();
            int node_count = Integer.parseInt(str);
            graph = new ArrayList(node_count);
            while ((str = reader.readLine()) != null) {
                String[] l_str = str.split(",");
                int id = Integer.parseInt(l_str[0]);
                int neighbor_count = Integer.parseInt(l_str[1]);
                ArrayList<Integer> line = new ArrayList<Integer>(neighbor_count);
                if (neighbor_count == 0) {
                    graph.add(line);
                    continue;
                }
                int i = 2;
                while (i < l_str.length) {
                    line.add(Integer.parseInt(l_str[i]));
                    ++i;
                }
                graph.add(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static boolean ReachabilityQuery(ArrayList<ArrayList<Integer>> graph, ArrayList<Entity> entities, int start_id, MyRectangle query_rect) {
        HashSet<Integer> visited_vertices = new HashSet<Integer>();
        LinkedList<Integer> queue = new LinkedList<Integer>();
        Entity p_Entity = entities.get(start_id);
        if (p_Entity.IsSpatial && OwnMethods.Location_In_Rect(p_Entity.lat, p_Entity.lon, query_rect)) {
            return true;
        }
        visited_vertices.add(start_id);
        int i = 0;
        while (i < graph.get(start_id).size()) {
            queue.add(graph.get(start_id).get(i));
            ++i;
        }
        while (!queue.isEmpty()) {
            int id = (Integer)queue.poll();
            visited_vertices.add(id);
            if (entities.get((int)id).IsSpatial && OwnMethods.Location_In_Rect(entities.get((int)id).lat, entities.get((int)id).lon, query_rect)) {
                return true;
            }
            int i2 = 0;
            while (i2 < graph.get(id).size()) {
                int neighbor_id = graph.get(id).get(i2);
                if (!visited_vertices.contains(neighbor_id)) {
                    queue.add(neighbor_id);
                }
                ++i2;
            }
        }
        return false;
    }

    public static ArrayList<Entity> ReadEntity(String entity_path) {
        ArrayList<Entity> entities = null;
        BufferedReader reader = null;
        String str = null;
        try {
            reader = new BufferedReader(new FileReader(new File(entity_path)));
            str = reader.readLine();
            int node_count = Integer.parseInt(str);
            entities = new ArrayList<Entity>(node_count);
            int id = 0;
            while ((str = reader.readLine()) != null) {
                Entity entity;
                String[] str_l = str.split(",");
                int flag = Integer.parseInt(str_l[1]);
                if (flag == 0) {
                    entity = new Entity();
                    entities.add(entity);
                } else {
                    entity = new Entity(Double.parseDouble(str_l[2]), Double.parseDouble(str_l[3]));
                    entities.add(entity);
                }
                ++id;
            }
            reader.close();
        }
        catch (Exception node_count) {
            // empty catch block
        }
        return entities;
    }

    public static MyRectangle GetEntityRange(String entity_path) {
        Entity p_Entity;
        ArrayList<Entity> entities = OwnMethods.ReadEntity(entity_path);
        MyRectangle range = null;
        int i = 0;
        while (i < entities.size()) {
            p_Entity = entities.get(i);
            if (p_Entity.IsSpatial) {
                range = new MyRectangle(p_Entity.lon, p_Entity.lat, p_Entity.lon, p_Entity.lat);
                break;
            }
            ++i;
        }
        while (i < entities.size()) {
            p_Entity = entities.get(i);
            if (p_Entity.lon < range.min_x) {
                range.min_x = p_Entity.lon;
            }
            if (p_Entity.lat < range.min_y) {
                range.min_y = p_Entity.lat;
            }
            if (p_Entity.lon > range.max_x) {
                range.max_x = p_Entity.lon;
            }
            if (p_Entity.lat > range.max_y) {
                range.max_y = p_Entity.lat;
            }
            ++i;
        }
        return range;
    }

    public static ArrayList<Integer> ReadSCC(String SCC_filepath, String original_graph_path) {
        ArrayList<Integer> list = null;
        String string = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(SCC_filepath)));
            string = reader.readLine();
            long node_count = OwnMethods.GetNodeCountGeneral(original_graph_path);
            list = new ArrayList<Integer>();
            long i = 0;
            while (i < node_count) {
                list.add(0);
                ++i;
            }
            Integer scc_id = 0;
            while ((string = reader.readLine()) != null) {
                string = string.substring(1, string.length() - 1);
                String[] lString = string.split(", ");
                int i2 = 0;
                while (i2 < lString.length) {
                    long ori_id = Long.parseLong(lString[i2]);
                    list.set((int)ori_id, scc_id);
                    ++i2;
                }
                scc_id = scc_id + 1;
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static long GeoReachIndexSize(String GeoReach_filepath) {
        long bits;
        block21 : {
            BufferedReader reader_GeoReach;
            reader_GeoReach = null;
            File file_GeoReach = null;
            bits = 0;
            try {
                try {
                    file_GeoReach = new File(GeoReach_filepath);
                    reader_GeoReach = new BufferedReader(new FileReader(file_GeoReach));
                    String tempString_GeoReach = null;
                    while ((tempString_GeoReach = reader_GeoReach.readLine()) != null) {
                        String[] l_GeoReach = tempString_GeoReach.split(",");
                        int type = Integer.parseInt(l_GeoReach[1]);
                        switch (type) {
                            case 0: {
                                RoaringBitmap r = new RoaringBitmap();
                                int i = 2;
                                while (i < l_GeoReach.length) {
                                    int out_neighbor = Integer.parseInt(l_GeoReach[i]);
                                    r.add(out_neighbor);
                                    ++i;
                                }
                                String bitmap_ser = OwnMethods.Serialize_RoarBitmap_ToString(r);
                                bits += (long)(bitmap_ser.getBytes().length * 8);
                                break;
                            }
                            case 1: {
                                bits += 128;
                                break;
                            }
                            case 2: {
                                ++bits;
                            }
                        }
                    }
                    reader_GeoReach.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if (reader_GeoReach != null) {
                        try {
                            reader_GeoReach.close();
                        }
                        catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    break block21;
                }
            }
            catch (Exception e) {
                if (reader_GeoReach != null) {
                    try {
                        reader_GeoReach.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (reader_GeoReach != null) {
                try {
                    reader_GeoReach.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bits / 8;
    }

    public static ArrayList<Long> ReadExperimentNode(String datasource) {
        ArrayList<Long> al;
        block13 : {
            String filepath = "/home/yuhansun/Documents/Real_data/" + datasource + "/experiment_id.txt";
            al = new ArrayList<Long>();
            BufferedReader reader = null;
            File file = null;
            try {
                try {
                    file = new File(filepath);
                    reader = new BufferedReader(new FileReader(file));
                    String temp = null;
                    while ((temp = reader.readLine()) != null) {
                        al.add(Long.parseLong(temp));
                    }
                    reader.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (reader == null) break block13;
                    try {
                        reader.close();
                    }
                    catch (IOException var7_7) {}
                }
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException var7_9) {}
                }
            }
        }
        return al;
    }

    public static void PrintArray(String[] l) {
        int i = 0;
        while (i < l.length) {
            System.out.print(String.valueOf(l[i]) + "\t");
            ++i;
        }
        System.out.print("\n");
    }

    public static void Print(Object o) {
        System.out.println(o);
    }

    public static ArrayList<Integer> GetRandom_NoDuplicate(ArrayList<Integer> wholeset, int count)
	{
		ArrayList<Integer> result = new ArrayList<Integer>(count);
		HashSet<Integer> hashSet = new HashSet<Integer>();
		Random random = new Random();
		while ( hashSet.size() < count)
		{
			int index = (int) (random.nextFloat() * wholeset.size());
			if(hashSet.contains(index) == false)
			{
				hashSet.add(index);
				result.add(wholeset.get(index));
			}
		}
		return result;
	}
    
    public static HashSet<Long> GenerateRandomInteger(long graph_size, int node_count) {
        HashSet<Long> ids = new HashSet<Long>();
        Random random = new Random();
        while (ids.size() < node_count) {
            Long id = (long)(random.nextDouble() * (double)graph_size);
            ids.add(id);
        }
        return ids;
    }

    public static ArrayList<String> GenerateStartNode(WebResource resource, HashSet<String> attribute_ids, String label) {
        String query = "match (a:" + label + ") where a.id in " + attribute_ids.toString() + " return id(a)";
        String result = Neo4j_Graph_Store.Execute(resource, query);
        ArrayList<String> graph_ids = Neo4j_Graph_Store.GetExecuteResultData(result);
        return graph_ids;
    }

    public static ArrayList<String> GenerateStartNode(HashSet<String> attribute_ids, String label) {
        Neo4j_Graph_Store p_neo4j_graph_store = new Neo4j_Graph_Store();
        String query = "match (a:" + label + ") where a.id in " + attribute_ids.toString() + " return id(a)";
        String result = p_neo4j_graph_store.Execute(query);
        ArrayList<String> graph_ids = Neo4j_Graph_Store.GetExecuteResultData(result);
        return graph_ids;
    }

    public ArrayList<String> ReadFile(String filename) {
        ArrayList<String> lines;
        block13 : {
            lines = new ArrayList<String>();
            File file = new File(filename);
            BufferedReader reader = null;
            try {
                try {
                    reader = new BufferedReader(new FileReader(file));
                    String tempString = null;
                    while ((tempString = reader.readLine()) != null) {
                        lines.add(tempString);
                    }
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if (reader == null) break block13;
                    try {
                        reader.close();
                    }
                    catch (IOException var7_7) {}
                }
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException var7_9) {}
                }
            }
        }
        return lines;
    }

    public static void WriteFile(String filename, boolean app, ArrayList<String> lines) {
        try {
            FileWriter fw = new FileWriter(filename, app);
            int i = 0;
            while (i < lines.size()) {
                fw.write(String.valueOf(lines.get(i)) + "\n");
                ++i;
            }
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteFile(String filename, boolean app, String str) {
        try {
            FileWriter fw = new FileWriter(filename, app);
            fw.write(str);
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getDirSize(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                long size = 0;
                File[] arrfile = children;
                int n = arrfile.length;
                int n2 = 0;
                while (n2 < n) {
                    File f = arrfile[n2];
                    size += OwnMethods.getDirSize(f);
                    ++n2;
                }
                return size;
            }
            long size = file.length();
            return size;
        }
        System.out.println("File not exists!");
        return 0;
    }
    
    public static int GetSpatialEntityCount(ArrayList<Entity> entities)
    {
    	int count = 0;
    	for ( Entity entity : entities)
    		if(entity.IsSpatial)
    			count++;
    	return count;
    }

    public static int GetNodeCount(String datasource) {
        int node_count;
        node_count = 0;
        File file = null;
        BufferedReader reader = null;
        try {
            try {
                file = new File("/home/yuhansun/Documents/Real_data/" + datasource + "/graph.txt");
                reader = new BufferedReader(new FileReader(file));
                String str = reader.readLine();
                String[] l = str.split(" ");
                node_count = Integer.parseInt(l[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    reader.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return node_count;
    }

    public static int GetNodeCountGeneral(String filepath) {
        int node_count;
        node_count = 0;
        File file = null;
        BufferedReader reader = null;
        try {
            try {
                file = new File(filepath);
                reader = new BufferedReader(new FileReader(file));
                String str = reader.readLine();
                String[] l = str.split(" ");
                node_count = Integer.parseInt(l[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    reader.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return node_count;
    }

    public static String ClearCache(String password) {
        String[] cmd = new String[]{"/bin/bash", "-c", "echo " + password + " | sudo -S sh -c \"sync; echo 3 > /proc/sys/vm/drop_caches\""};
        String result = null;
        try {
            String line;
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
            result = String.valueOf(result) + "\n";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String Serialize_RoarBitmap_ToString(RoaringBitmap r) {
        r.runOptimize();
        ByteBuffer outbb = ByteBuffer.allocate(r.serializedSizeInBytes());
        try {
        	r.serialize(new DataOutputStream(new OutputStream(){
			    ByteBuffer mBB;
			    OutputStream init(ByteBuffer mbb) {mBB=mbb; return this;}
			    public void close() {}
			    public void flush() {}
			    public void write(int b) {
			        mBB.put((byte) b);}
			    public void write(byte[] b) {mBB.put(b);}            
			    public void write(byte[] b, int off, int l) {mBB.put(b,off,l);}
			}.init(outbb)));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        outbb.flip();
        String serializedstring = Base64.getEncoder().encodeToString(outbb.array());
        return serializedstring;
    }

    public static ImmutableRoaringBitmap Deserialize_String_ToRoarBitmap(String serializedstring) {
        ByteBuffer newbb = ByteBuffer.wrap(Base64.getDecoder().decode(serializedstring));
        ImmutableRoaringBitmap ir = new ImmutableRoaringBitmap(newbb);
        return ir;
    }

    public static void PrintNode(Node node) {
        Iterator iter = node.getPropertyKeys().iterator();
        HashMap<String, String> properties = new HashMap<String, String>();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            properties.put(key, node.getProperty(key).toString());
        }
        System.out.println(properties.toString());
    }

    public static boolean Location_In_Rect(double lat, double lon, MyRectangle rect) {
        if (lat < rect.min_y || lat > rect.max_y || lon < rect.min_x || lon > rect.max_x) {
            return false;
        }
        return true;
    }
    
    public static STRtree ConstructSTRee(ArrayList<Entity> entities)
    {
    	STRtree strtree = new STRtree();

    	GeometryFactory fact=new GeometryFactory();
    	for(Entity entity : entities)
    		if(entity.IsSpatial)
    		{
    			Point datapoint = fact.createPoint(new Coordinate(entity.lon, entity.lat));
    			strtree.insert(datapoint.getEnvelopeInternal(), datapoint);
    			
    		}
    	return strtree;
    }
}
