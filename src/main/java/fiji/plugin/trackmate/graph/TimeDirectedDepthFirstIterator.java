/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2010 - 2022 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package fiji.plugin.trackmate.graph;

import fiji.plugin.trackmate.Spot;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class TimeDirectedDepthFirstIterator extends SortedDepthFirstIterator< Spot, DefaultWeightedEdge >
{

	public TimeDirectedDepthFirstIterator( Graph< Spot, DefaultWeightedEdge > g, Spot startVertex )
	{
		super( g, startVertex, null );
	}

	@Override
	protected void addUnseenChildrenOf( Spot vertex )
	{

		int ts = vertex.getFeature( Spot.FRAME ).intValue();
		for ( DefaultWeightedEdge edge : specifics.edgesOf( vertex ) )
		{
			if ( nListeners != 0 )
			{
				fireEdgeTraversed( createEdgeTraversalEvent( edge ) );
			}

			Spot oppositeV = Graphs.getOppositeVertex( graph, edge, vertex );
			int tt = oppositeV.getFeature( Spot.FRAME ).intValue();
			if ( tt <= ts )
			{
				continue;
			}

			if ( seen.containsKey( oppositeV ) )
			{
				encounterVertexAgain( oppositeV, edge );
			}
			else
			{
				encounterVertex( oppositeV, edge );
			}
		}
	}

}
