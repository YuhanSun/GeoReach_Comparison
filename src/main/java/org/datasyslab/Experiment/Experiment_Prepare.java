package org.datasyslab.Experiment;

import java.util.ArrayList;

import org.datasyslab.GeoReach.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

public class Experiment_Prepare {
	
	static String dataset;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * generate query rectangle with spatial vertices count as selectivity
	 * rather than using size of rectangle
	 */
	public static void GenerateQueryRectangle() {
		int experiment_count = 500;
		String entity_path = String.format("/mnt/hgfs/Ubuntu_shared/GeoMinHop/data/%s/entity.txt", dataset);
		ArrayList<Entity> entities = OwnMethods.ReadEntity((String)entity_path);
		int spa_count = OwnMethods.GetSpatialEntityCount(entities);
		STRtree stRtree = OwnMethods.ConstructSTRee(entities);
		
		String center_id_path = String.format("/mnt/hgfs/Ubuntu_shared/GeoMinHop/query/spa_predicate/"
				+ "%s/%s_centerids.txt", dataset, dataset);
		ArrayList<Integer> center_ids = OwnMethods.ReadCenterID(center_id_path);
		ArrayList<Integer> final_center_ids = OwnMethods.GetRandom_NoDuplicate(center_ids, experiment_count);
		
		double selectivity = 0.000001;
		while (selectivity < 0.9)
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
				OwnMethods.Print(radius);
				double a = Math.sqrt(Math.PI) * radius;
				double minx = center.getX() - a / 2;
				double miny = center.getY() - a / 2;
				double maxx = center.getX() + a / 2;
				double maxy = center.getY() + a / 2;
				
				write_line += String.format("%f\t%f\t%f\t%f\n", minx, miny, maxx, maxy);
			}
			String output_path = String.format("/mnt/hgfs/Ubuntu_shared/GeoMinHop/"
					+ "query/spa_predicate/%s/queryrect_%d.txt", dataset, name_suffix);
			OwnMethods.WriteFile(output_path, true, write_line);
			selectivity *= 10;
		}
	}

}
