/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2010 - 2023 TrackMate developers.
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
package fiji.plugin.trackmate.graph;

import static org.junit.Assert.assertArrayEquals;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.BeforeClass;
import org.junit.Test;

public class SortedDepthFirstIteratorTest
{

	private static final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static Random rnd = new Random();

	private static int N_CHILDREN = 50;

	private static int N_LEVELS = 5;

	private static Model model;

	private static Spot root;

	private static String[] names;

	private static Comparator< Spot > spotNameComparator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		/*
		 * The comparator
		 */
		spotNameComparator = new Comparator< Spot >()
		{
			@Override
			public int compare( final Spot o1, final Spot o2 )
			{
				return o1.getName().compareTo( o2.getName() );
			}
		};

		/*
		 * The graph
		 */

		model = new Model();
		model.beginUpdate();
		try
		{

			// Root
			root = new Spot( 0d, 0d, 0d, 1d, -1d, "Root" );
			model.addSpotTo( root, 0 );

			// First level
			names = new String[ N_CHILDREN ];
			final Spot[][] spots = new Spot[ N_LEVELS ][ N_CHILDREN ];
			for ( int i = 0; i < names.length; i++ )
			{

				names[ i ] = "A"; // randomString( 5 );
				final Spot spotChild = new Spot( 0d, 0d, 0d, 1d, -1d, names[ i ] );
				model.addSpotTo( spotChild, 1 );
				model.addEdge( root, spotChild, -1 );
				spots[ 0 ][ i ] = spotChild;

				spots[ 0 ][ i ] = spotChild;
				for ( int j = 1; j < spots.length; j++ )
				{
					final Spot spot = new Spot( 0d, 0d, 0d, 1d, -1d, "  " + j + "_" + randomString( 3 ) );
					spots[ j ][ i ] = spot;
					model.addSpotTo( spot, j + 1 );
					model.addEdge( spots[ j - 1 ][ i ], spots[ j ][ i ], -1 );
				}
			}
		}
		finally
		{
			model.endUpdate();
		}
	}

	@Test
	public final void testBehavior()
	{

		// Sort names
		final String[] expectedSortedNames = names.clone();
		final Comparator< String > alphabeticalOrder = new Comparator< String >()
		{
			@Override
			public int compare( final String o1, final String o2 )
			{
				return o1.compareTo( o2 );
			}
		};
		Arrays.sort( expectedSortedNames, 0, expectedSortedNames.length, alphabeticalOrder );

		// Collect names in the tree
		final SortedDepthFirstIterator< Spot, DefaultWeightedEdge > iterator = model.getTrackModel().getSortedDepthFirstIterator( root, spotNameComparator, true );
		final String[] actualNames = new String[ N_CHILDREN ];
		int index = 0;
		while ( iterator.hasNext() )
		{
			final Spot spot = iterator.next();
			if ( model.getTrackModel().getEdge( root, spot ) != null )
			{
				actualNames[ index++ ] = spot.getName();
			}
		}

		assertArrayEquals( expectedSortedNames, actualNames );
	}

	private final static String randomString( final int len )
	{
		final StringBuilder sb = new StringBuilder( len );
		for ( int i = 0; i < len; i++ )
			sb.append( AB.charAt( rnd.nextInt( AB.length() ) ) );
		return sb.toString();
	}

}
