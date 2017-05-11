/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.sun.jersey.api.client.WebResource
 *  org.datasyslab.GeoReach.Config
 *  org.datasyslab.GeoReach.MyRectangle
 *  org.datasyslab.GeoReach.ReachabilityQuerySolver
 *  org.roaringbitmap.buffer.ImmutableRoaringBitmap
 */
package org.datasyslab.GeoReach;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.WebResource;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.datasyslab.GeoReach.Config;
import org.datasyslab.GeoReach.MyRectangle;
import org.datasyslab.GeoReach.Neo4j_Graph_Store;
import org.datasyslab.GeoReach.ReachabilityQuerySolver;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class GeoReach
implements ReachabilityQuerySolver {
    public Set<Integer> VisitedVertices = new HashSet<Integer>();
    public Neo4j_Graph_Store p_neo4j_graph_store;
    private static WebResource resource;
    private String longitude_property_name;
    private String latitude_property_name;
    private String RMBR_minx_name;
    private String RMBR_miny_name;
    private String RMBR_maxx_name;
    private String RMBR_maxy_name;
    private String GeoB_name;
    private String bitmap_name;
    public MyRectangle total_range;
    public int split_pieces;
    public double resolution_x;
    public double resolution_y;
    public HashMap<Integer, Double> multi_resolution_x = new HashMap();
    public HashMap<Integer, Double> multi_resolution_y = new HashMap();
    public HashMap<Integer, Integer> multi_offset = new HashMap();
    public int level_count;
    private int merge_ratio;
    private double Max_RMBR_ratio;
    private int Max_ReachGrid;
    private double Max_RMBR_area;
    public int visited_count = 0;
    public int neo4j_time = 0;
    public ArrayList<Long> Visited = new ArrayList();

    public GeoReach(MyRectangle p_total_range, int p_split_pieces, int merge_ratio) {
        Config config = new Config();
        this.longitude_property_name = config.GetLongitudePropertyName();
        this.latitude_property_name = config.GetLatitudePropertyName();
        this.RMBR_minx_name = config.GetRMBR_minx_name();
        this.RMBR_miny_name = config.GetRMBR_miny_name();
        this.RMBR_maxx_name = config.GetRMBR_maxx_name();
        this.RMBR_maxy_name = config.GetRMBR_maxy_name();
        this.merge_ratio = merge_ratio;
        this.total_range = new MyRectangle();
        this.total_range.min_x = p_total_range.min_x;
        this.total_range.min_y = p_total_range.min_y;
        this.total_range.max_x = p_total_range.max_x;
        this.total_range.max_y = p_total_range.max_y;
        this.Max_RMBR_ratio = config.GetMax_RMBR_Ratio();
        this.Max_RMBR_area = (this.total_range.max_y - this.total_range.min_y) * (this.total_range.max_x - this.total_range.min_x) * this.Max_RMBR_ratio;
        this.Max_ReachGrid = config.GetMax_ReachGrid();
        this.split_pieces = p_split_pieces;
        this.GeoB_name = config.GetGeoB_name();
        this.bitmap_name = config.GetBitmap_name();
        this.resolution_x = (this.total_range.max_x - this.total_range.min_x) / (double)this.split_pieces;
        this.resolution_y = (this.total_range.max_y - this.total_range.min_y) / (double)this.split_pieces;
        int i = 1;
        while (i <= this.split_pieces) {
            this.multi_resolution_x.put(i, (this.total_range.max_x - this.total_range.min_x) / (double)i);
            this.multi_resolution_y.put(i, (this.total_range.max_y - this.total_range.min_y) / (double)i);
            i *= 2;
        }
        int sum = 0;
        int i2 = this.split_pieces;
        while (i2 >= 1) {
            this.multi_offset.put(i2, sum);
            sum += i2 * i2;
            i2 /= 2;
        }
        this.level_count = (int)(Math.log(this.split_pieces) / Math.log(2.0)) + 1;
        this.p_neo4j_graph_store = new Neo4j_Graph_Store();
        resource = this.p_neo4j_graph_store.GetCypherResource();
    }

    private boolean TraverseQuery_MT0(long start_id, MyRectangle rect, int lb_x, int lb_y, int rt_x, int rt_y) {
        int id;
        JsonArray row;
        JsonObject jsonObject;
        long start = System.currentTimeMillis();
        String query = String.format("match (a)-->(b) where id(a) = %d return id(b), b.%s, b.%s, b.%s, b.%s, b.%s, b.%s, b.%s, b.%s", start_id, this.GeoB_name, this.bitmap_name, this.RMBR_minx_name, this.RMBR_miny_name, this.RMBR_maxx_name, this.RMBR_maxy_name, this.longitude_property_name, this.latitude_property_name);
        String result = Neo4j_Graph_Store.Execute(resource, query);
        this.neo4j_time = (int)((long)this.neo4j_time + (System.currentTimeMillis() - start));
        JsonArray jsonArr = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
        this.visited_count += jsonArr.size();
        int false_count = 0;
        int i = 0;
        while (i < jsonArr.size()) {
            jsonObject = (JsonObject)jsonArr.get(i);
            row = (JsonArray)jsonObject.get("row");
            id = row.get(0).getAsInt();
            this.Visited.add(Long.valueOf(id));
            if (this.VisitedVertices.contains(id)) {
                ++false_count;
            } else {
                if (!row.get(7).isJsonNull()) {
                    double lon = row.get(7).getAsDouble();
                    double lat = row.get(8).getAsDouble();
                    if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
                        return true;
                    }
                }
                if (!(row.get(1).isJsonNull() || row.get(2).isJsonNull() || row.get(3).isJsonNull())) {
                    ++false_count;
                    this.VisitedVertices.add(id);
                } else if (!row.get(2).isJsonNull()) {
                    int grid_id;
                    int j;
                    String ser = row.get(2).getAsString();
                    ByteBuffer newbb = ByteBuffer.wrap(Base64.getDecoder().decode(ser));
                    ImmutableRoaringBitmap reachgrid = new ImmutableRoaringBitmap(newbb);
                    if (rt_x - lb_x > 1 && rt_y - lb_y > 1) {
                        int k = lb_x + 1;
                        while (k < rt_x) {
                            j = lb_y + 1;
                            while (j < rt_y) {
                                grid_id = k * this.split_pieces + j;
                                if (reachgrid.contains(grid_id)) {
                                    return true;
                                }
                                ++j;
                            }
                            ++k;
                        }
                    }
                    boolean flag = false;
                    j = lb_y;
                    while (j <= rt_y) {
                        grid_id = lb_x * this.split_pieces + j;
                        if (reachgrid.contains(grid_id)) {
                            flag = true;
                        }
                        if (reachgrid.contains(grid_id = rt_x * this.split_pieces + j)) {
                            flag = true;
                        }
                        ++j;
                    }
                    j = lb_x + 1;
                    while (j < rt_x) {
                        grid_id = j * this.split_pieces + lb_y;
                        if (reachgrid.contains(grid_id)) {
                            flag = true;
                        }
                        if (reachgrid.contains(grid_id = j * this.split_pieces + rt_y)) {
                            flag = true;
                        }
                        ++j;
                    }
                    if (!flag) {
                        ++false_count;
                        this.VisitedVertices.add(id);
                    }
                } else if (!row.get(3).isJsonNull()) {
                    MyRectangle RMBR = new MyRectangle(row.get(3).getAsDouble(), row.get(4).getAsDouble(), row.get(5).getAsDouble(), row.get(6).getAsDouble());
                    if (RMBR.min_x > rect.min_x && RMBR.max_x < rect.max_x && RMBR.min_y > rect.min_y && RMBR.max_y < rect.max_y) {
                        return true;
                    }
                    if (RMBR.min_x > rect.max_x || RMBR.max_x < rect.min_x || RMBR.min_y > rect.max_y || RMBR.max_y < rect.min_y) {
                        ++false_count;
                        this.VisitedVertices.add(id);
                    }
                }
            }
            ++i;
        }
        if (false_count == jsonArr.size()) {
            return false;
        }
        i = 0;
        while (i < jsonArr.size()) {
            jsonObject = (JsonObject)jsonArr.get(i);
            row = (JsonArray)jsonObject.get("row");
            id = row.get(0).getAsInt();
            if (!this.VisitedVertices.contains(id)) {
                this.VisitedVertices.add(id);
                boolean reachable = this.TraverseQuery_MT0(id, rect, lb_x, lb_y, rt_x, rt_y);
                if (reachable) {
                    return true;
                }
            }
            ++i;
        }
        return false;
    }

    public boolean ReachabilityQuery_MT0(long start_id, MyRectangle rect) {
        this.VisitedVertices.clear();
        this.Visited.clear();
        this.neo4j_time = 0;
        long start = System.currentTimeMillis();
        String query = String.format("match (n) where id(n) = %d return n.%s, n.%s, n.%s, n.%s, n.%s, n.%s, n.%s, n.%s", start_id, this.GeoB_name, this.bitmap_name, this.RMBR_minx_name, this.RMBR_miny_name, this.RMBR_maxx_name, this.RMBR_maxy_name, this.longitude_property_name, this.latitude_property_name);
        String result = Neo4j_Graph_Store.Execute(resource, query);
        this.neo4j_time = (int)((long)this.neo4j_time + (System.currentTimeMillis() - start));
        JsonArray jsonArr = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
        jsonArr = jsonArr.get(0).getAsJsonObject().get("row").getAsJsonArray();
        this.Visited.add(start_id);
        ++this.visited_count;
        if (!jsonArr.get(6).isJsonNull()) {
            double lon = jsonArr.get(6).getAsDouble();
            double lat = jsonArr.get(7).getAsDouble();
            if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
                return true;
            }
        }
        if (jsonArr.get(0).isJsonNull() && jsonArr.get(1).isJsonNull() && jsonArr.get(2).isJsonNull()) {
            return false;
        }
        int lb_x = (int)((rect.min_x - this.total_range.min_x) / this.resolution_x);
        int lb_y = (int)((rect.min_y - this.total_range.min_y) / this.resolution_y);
        int rt_x = (int)((rect.max_x - this.total_range.min_x) / this.resolution_x);
        int rt_y = (int)((rect.max_y - this.total_range.min_y) / this.resolution_y);
        if (!jsonArr.get(1).isJsonNull()) {
            int i;
            int grid_id;
            String ser = null;
            ByteBuffer newbb = null;
            ImmutableRoaringBitmap reachgrid = null;
            boolean flag = false;
            ser = jsonArr.get(1).getAsString();
            newbb = ByteBuffer.wrap(Base64.getDecoder().decode(ser));
            reachgrid = new ImmutableRoaringBitmap(newbb);
            if (rt_x - lb_x > 1 && rt_y - lb_y > 1) {
                i = lb_x + 1;
                while (i < rt_x) {
                    int j = lb_y + 1;
                    while (j < rt_y) {
                        int grid_id2 = i * this.split_pieces + j;
                        if (reachgrid.contains(grid_id2)) {
                            return true;
                        }
                        ++j;
                    }
                    ++i;
                }
            }
            i = lb_y;
            while (i <= rt_y) {
                grid_id = lb_x * this.split_pieces + i;
                if (reachgrid.contains(grid_id)) {
                    flag = true;
                }
                if (reachgrid.contains(grid_id = rt_x * this.split_pieces + i)) {
                    flag = true;
                }
                ++i;
            }
            i = lb_x + 1;
            while (i < rt_x) {
                grid_id = i * this.split_pieces + lb_y;
                if (reachgrid.contains(grid_id)) {
                    flag = true;
                }
                if (reachgrid.contains(grid_id = i * this.split_pieces + rt_y)) {
                    flag = true;
                }
                ++i;
            }
            if (!flag) {
                return false;
            }
            return this.TraverseQuery_MT0(start_id, rect, lb_x, lb_y, rt_x, rt_y);
        }
        if (!jsonArr.get(2).isJsonNull()) {
            MyRectangle RMBR = new MyRectangle(jsonArr.get(2).getAsDouble(), jsonArr.get(3).getAsDouble(), jsonArr.get(4).getAsDouble(), jsonArr.get(5).getAsDouble());
            if (RMBR.min_x > rect.max_x || RMBR.max_x < rect.min_x || RMBR.min_y > rect.max_y || RMBR.max_y < rect.min_y) {
                return false;
            }
            if (RMBR.min_x > rect.min_x && RMBR.max_x < rect.max_x && RMBR.min_y > rect.min_y && RMBR.max_y < rect.max_y) {
                return true;
            }
            return this.TraverseQuery_MT0(start_id, rect, lb_x, lb_y, rt_x, rt_y);
        }
        return this.TraverseQuery_MT0(start_id, rect, lb_x, lb_y, rt_x, rt_y);
    }

    private boolean TraverseQuery(long start_id, MyRectangle rect, HashMap<Integer, Integer> lb_x_hash, HashMap<Integer, Integer> lb_y_hash, HashMap<Integer, Integer> rt_x_hash, HashMap<Integer, Integer> rt_y_hash) {
        JsonObject jsonObject;
        int id;
        JsonArray row;
        long start = System.currentTimeMillis();
        String query = String.format("match (a)-->(b) where id(a) = %d return id(b), b.%s, b.%s, b.%s, b.%s, b.%s, b.%s, b.%s, b.%s", start_id, this.GeoB_name, this.bitmap_name, this.RMBR_minx_name, this.RMBR_miny_name, this.RMBR_maxx_name, this.RMBR_maxy_name, this.longitude_property_name, this.latitude_property_name);
        String result = Neo4j_Graph_Store.Execute(resource, query);
        this.neo4j_time = (int)((long)this.neo4j_time + (System.currentTimeMillis() - start));
        JsonArray jsonArr = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
        this.visited_count += jsonArr.size();
        int false_count = 0;
        int i = 0;
        while (i < jsonArr.size()) {
            jsonObject = (JsonObject)jsonArr.get(i);
            row = (JsonArray)jsonObject.get("row");
            id = row.get(0).getAsInt();
            this.Visited.add(Long.valueOf(id));
            if (this.VisitedVertices.contains(id)) {
                ++false_count;
            } else {
                if (!row.get(7).isJsonNull()) {
                    double lon = row.get(7).getAsDouble();
                    double lat = row.get(8).getAsDouble();
                    if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
                        return true;
                    }
                }
                if (!(row.get(1).isJsonNull() || row.get(2).isJsonNull() || row.get(3).isJsonNull())) {
                    ++false_count;
                    this.VisitedVertices.add(id);
                } else if (!row.get(2).isJsonNull()) {
                    String ser = row.get(2).getAsString();
                    ByteBuffer newbb = ByteBuffer.wrap(Base64.getDecoder().decode(ser));
                    ImmutableRoaringBitmap reachgrid = new ImmutableRoaringBitmap(newbb);
                    int outside_count = 0;
                    int level_pieces = this.split_pieces;
                    while (level_pieces >= 1) {
                        int lb_x = lb_x_hash.get(level_pieces);
                        int lb_y = lb_y_hash.get(level_pieces);
                        int rt_x = rt_x_hash.get(level_pieces);
                        int rt_y = rt_y_hash.get(level_pieces);
                        int offset = this.multi_offset.get(level_pieces);
                        Iterator iter = reachgrid.iterator();
                        boolean flag = false;
                        while (iter.hasNext()) {
                            int grid_id = (Integer)iter.next() - offset;
                            int row_index = grid_id / level_pieces;
                            int col_index = grid_id - row_index * level_pieces;
                            if (row_index < rt_x && row_index > lb_x && col_index < rt_y && col_index > lb_y) {
                                return true;
                            }
                            if (row_index > rt_x || row_index < lb_x || col_index > rt_y || col_index < lb_y) {
                                flag = false;
                                continue;
                            }
                            flag = true;
                            break;
                        }
                        if (flag) break;
                        ++outside_count;
                        level_pieces /= 2;
                    }
                    if (outside_count == this.level_count) {
                        ++false_count;
                        this.VisitedVertices.add(id);
                    }
                } else if (!row.get(3).isJsonNull()) {
                    MyRectangle RMBR = new MyRectangle(row.get(3).getAsDouble(), row.get(4).getAsDouble(), row.get(5).getAsDouble(), row.get(6).getAsDouble());
                    if (RMBR.min_x > rect.min_x && RMBR.max_x < rect.max_x && RMBR.min_y > rect.min_y && RMBR.max_y < rect.max_y) {
                        return true;
                    }
                    if (RMBR.min_x > rect.max_x || RMBR.max_x < rect.min_x || RMBR.min_y > rect.max_y || RMBR.max_y < rect.min_y) {
                        ++false_count;
                        this.VisitedVertices.add(id);
                    }
                }
            }
            ++i;
        }
        if (false_count == jsonArr.size()) {
            return false;
        }
        i = 0;
        while (i < jsonArr.size()) {
            jsonObject = (JsonObject)jsonArr.get(i);
            row = (JsonArray)jsonObject.get("row");
            id = row.get(0).getAsInt();
            if (!this.VisitedVertices.contains(id)) {
                this.VisitedVertices.add(id);
                boolean reachable = this.TraverseQuery(id, rect, lb_x_hash, lb_y_hash, rt_x_hash, rt_y_hash);
                if (reachable) {
                    return true;
                }
            }
            ++i;
        }
        return false;
    }

    public boolean ReachabilityQuery(long start_id, MyRectangle rect) {
        this.visited_count = 0;
        this.VisitedVertices.clear();
        this.Visited.clear();
        this.neo4j_time = 0;
        if (this.merge_ratio == 0) {
            return this.ReachabilityQuery_MT0(start_id, rect);
        }
        long start = System.currentTimeMillis();
        String query = String.format("match (n) where id(n) = %d return n.%s, n.%s, n.%s, n.%s, n.%s, n.%s, n.%s, n.%s", start_id, this.GeoB_name, this.bitmap_name, this.RMBR_minx_name, this.RMBR_miny_name, this.RMBR_maxx_name, this.RMBR_maxy_name, this.longitude_property_name, this.latitude_property_name);
        String result = Neo4j_Graph_Store.Execute(resource, query);
        this.neo4j_time = (int)((long)this.neo4j_time + (System.currentTimeMillis() - start));
        JsonArray jsonArr = Neo4j_Graph_Store.GetExecuteResultDataASJsonArray(result);
        jsonArr = jsonArr.get(0).getAsJsonObject().get("row").getAsJsonArray();
        ++this.visited_count;
        if (!jsonArr.get(6).isJsonNull()) {
            double lon = jsonArr.get(6).getAsDouble();
            double lat = jsonArr.get(7).getAsDouble();
            if (Neo4j_Graph_Store.Location_In_Rect(lat, lon, rect)) {
                return true;
            }
        }
        if (jsonArr.get(0).isJsonNull() && jsonArr.get(1).isJsonNull() && jsonArr.get(2).isJsonNull()) {
            return false;
        }
        HashMap<Integer, Integer> lb_x_hash = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> lb_y_hash = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> rt_x_hash = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> rt_y_hash = new HashMap<Integer, Integer>();
        int level_pieces = 1;
        while (level_pieces <= this.split_pieces) {
            int lb_x = (int)((rect.min_x - this.total_range.min_x) / this.multi_resolution_x.get(level_pieces));
            int lb_y = (int)((rect.min_y - this.total_range.min_y) / this.multi_resolution_y.get(level_pieces));
            int rt_x = (int)((rect.max_x - this.total_range.min_x) / this.multi_resolution_x.get(level_pieces));
            int rt_y = (int)((rect.max_y - this.total_range.min_y) / this.multi_resolution_y.get(level_pieces));
            lb_x_hash.put(level_pieces, lb_x);
            lb_y_hash.put(level_pieces, lb_y);
            rt_x_hash.put(level_pieces, rt_x);
            rt_y_hash.put(level_pieces, rt_y);
            level_pieces *= 2;
        }
        if (!jsonArr.get(1).isJsonNull()) {
            String ser = jsonArr.get(1).getAsString();
            ByteBuffer newbb = ByteBuffer.wrap(Base64.getDecoder().decode(ser));
            ImmutableRoaringBitmap reachgrid = new ImmutableRoaringBitmap(newbb);
            int outside_count = 0;
            int level_pieces2 = this.split_pieces;
            while (level_pieces2 >= 1) {
                int lb_x = lb_x_hash.get(level_pieces2);
                int lb_y = lb_y_hash.get(level_pieces2);
                int rt_x = rt_x_hash.get(level_pieces2);
                int rt_y = rt_y_hash.get(level_pieces2);
                int offset = this.multi_offset.get(level_pieces2);
                Iterator iter = reachgrid.iterator();
                boolean flag = false;
                while (iter.hasNext()) {
                    int grid_id = (Integer)iter.next() - offset;
                    int row_index = grid_id / level_pieces2;
                    int col_index = grid_id - row_index * level_pieces2;
                    if (row_index < rt_x && row_index > lb_x && col_index < rt_y && col_index > lb_y) {
                        return true;
                    }
                    if (row_index > rt_x || row_index < lb_x || col_index > rt_y || col_index < lb_y) {
                        flag = false;
                        continue;
                    }
                    flag = true;
                    break;
                }
                if (flag) break;
                ++outside_count;
                level_pieces2 /= 2;
            }
            if (outside_count == this.level_count) {
                return false;
            }
            return this.TraverseQuery(start_id, rect, lb_x_hash, lb_y_hash, rt_x_hash, rt_y_hash);
        }
        if (!jsonArr.get(2).isJsonNull()) {
            MyRectangle RMBR = new MyRectangle(jsonArr.get(2).getAsDouble(), jsonArr.get(3).getAsDouble(), jsonArr.get(4).getAsDouble(), jsonArr.get(5).getAsDouble());
            if (RMBR.min_x > rect.max_x || RMBR.max_x < rect.min_x || RMBR.min_y > rect.max_y || RMBR.max_y < rect.min_y) {
                return false;
            }
            if (RMBR.min_x > rect.min_x && RMBR.max_x < rect.max_x && RMBR.min_y > rect.min_y && RMBR.max_y < rect.max_y) {
                return true;
            }
            return this.TraverseQuery(start_id, rect, lb_x_hash, lb_y_hash, rt_x_hash, rt_y_hash);
        }
        return this.TraverseQuery(start_id, rect, lb_x_hash, lb_y_hash, rt_x_hash, rt_y_hash);
    }

    public void Preprocess() {
    }
}