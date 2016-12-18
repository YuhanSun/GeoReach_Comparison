package org.datasyslab.GeoReach;

public interface ReachabilityQuerySolver {
	void Preprocess();
	boolean ReachabilityQuery(long start_id, MyRectangle rect);
}