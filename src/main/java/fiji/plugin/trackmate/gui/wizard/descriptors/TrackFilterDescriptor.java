package fiji.plugin.trackmate.gui.wizard.descriptors;

import java.util.List;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.features.track.TrackBranchingAnalyzer;
import fiji.plugin.trackmate.gui.FeatureDisplaySelector;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.gui.panels.components.FilterGuiPanel;
import fiji.plugin.trackmate.gui.wizard.WizardPanelDescriptor2;

public class TrackFilterDescriptor extends WizardPanelDescriptor2
{

	private static final String KEY = "TrackFilter";

	private final TrackMate trackmate;

	public TrackFilterDescriptor(
			final TrackMate trackmate,
			final List< FeatureFilter > filters,
			final FeatureDisplaySelector featureSelector )
	{
		super( KEY );
		this.trackmate = trackmate;
		final FilterGuiPanel component = new FilterGuiPanel(
				trackmate.getModel(),
				trackmate.getSettings(),
				TrackMateObject.TRACKS,
				filters,
				TrackBranchingAnalyzer.NUMBER_SPOTS,
				featureSelector );

		component.addChangeListener( e -> filterTracks() );
		this.targetPanel = component;
	}

	private void filterTracks()
	{
		final FilterGuiPanel component = ( FilterGuiPanel ) targetPanel;
		trackmate.getSettings().setTrackFilters( component.getFeatureFilters() );
		trackmate.execTrackFiltering( false );
	}

	@Override
	public Runnable getForwardRunnable()
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				final Model model = trackmate.getModel();
				final Logger logger = model.getLogger();

				/*
				 * We have some tracks so we need to compute spot features will
				 * we render them.
				 */
				logger.log( "Calculating track features...\n", Logger.BLUE_COLOR );
				// Calculate features
				final long start = System.currentTimeMillis();
				trackmate.computeEdgeFeatures( true );
				trackmate.computeTrackFeatures( true );
				final long end = System.currentTimeMillis();
				logger.log( String.format( "Calculating features done in %.1f s.\n", ( end - start ) / 1e3f ), Logger.BLUE_COLOR );

				// Refresh component.
				final FilterGuiPanel component = ( FilterGuiPanel ) targetPanel;
				component.refreshValues();
				filterTracks();
			}
		};
	}

	@Override
	public void displayingPanel()
	{
		filterTracks();
	}

	@Override
	public void aboutToHidePanel()
	{
		final Logger logger = trackmate.getModel().getLogger();
		logger.log( "Performing track filtering on the following features:\n", Logger.BLUE_COLOR );
		final Model model = trackmate.getModel();
		final FilterGuiPanel component = ( FilterGuiPanel ) targetPanel;
		final List< FeatureFilter > featureFilters = component.getFeatureFilters();
		trackmate.getSettings().setTrackFilters( featureFilters );
		trackmate.execTrackFiltering( false );

		final int ntotal = model.getTrackModel().nTracks( false );
		if ( featureFilters == null || featureFilters.isEmpty() )
		{
			logger.log( "No feature threshold set, kept the " + ntotal + " tracks.\n" );
		}
		else
		{
			for ( final FeatureFilter ft : featureFilters )
			{
				String str = "  - on " + trackmate.getModel().getFeatureModel().getTrackFeatureNames().get( ft.feature );
				if ( ft.isAbove )
					str += " above ";
				else
					str += " below ";
				str += String.format( "%.1f", ft.value );
				str += '\n';
				logger.log( str );
			}
			final int nselected = model.getTrackModel().nTracks( true );
			logger.log( "Kept " + nselected + " spots out of " + ntotal + ".\n" );
		}
	}
}
