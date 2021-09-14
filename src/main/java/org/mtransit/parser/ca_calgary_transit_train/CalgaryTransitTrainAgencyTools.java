package org.mtransit.parser.ca_calgary_transit_train;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/download/npk7-z3bj/application%2Fzip
// https://data.calgary.ca/OpenData/Pages/DatasetDetails.aspx?DatasetID=PDC0-99999-99999-00501-P(CITYonlineDefault)
// https://data.calgary.ca/_layouts/OpenData/DownloadDataset.ashx?Format=FILE&DatasetId=PDC0-99999-99999-00501-P(CITYonlineDefault)&VariantId=5(CITYonlineDefault)
// https://data.calgary.ca/_layouts/OpenData/DownloadDataset.ashx?Format=FILE&DatasetId=PDC0-99999-99999-00501-P(CITYonlineDefault)&VariantId=6(CITYonlineDefault)
public class CalgaryTransitTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CalgaryTransitTrainAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Calgary Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		return gRoute.getRouteType() != MAgency.ROUTE_TYPE_LIGHT_RAIL; // declared as light rail but we classify it as a train (not on the road)
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	private static final int RSN_RED = 201;
	private static final int RSN_BLUE = 202;

	private static final String SADDLE_TOWNE = "Saddletowne";
	private static final String SOMERSET_BRIDLE_WOOD = "Somerset-Bridlewood";
	private static final String _69_ST = "69 St";
	private static final String _69_ST_STATION = _69_ST + " Sta";
	private static final String TUSCANY = "Tuscany";

	private static final String SLASH = " / ";

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
			final int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case RSN_RED: return TUSCANY + SLASH + SOMERSET_BRIDLE_WOOD;
			case RSN_BLUE: return _69_ST_STATION + SLASH + SADDLE_TOWNE;
			// @formatter:on
			default:
				throw new MTLog.Fatal("Unexpected route long name for %s!", gRoute);
			}
		}
		return super.getRouteLongName(gRoute);
	}

	private static final Pattern CTRAIN_ = CleanUtils.cleanWord("ctrain");

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CTRAIN_.matcher(routeLongName).replaceAll(EMPTY);
		return super.cleanRouteLongName(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_RED_LINE = "EE2622"; // RED (from PDF map)
	private static final String COLOR_BLUE_LINE = "0F4076"; // BLUE (from PDF map)

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute, @NotNull MAgency agency) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			final int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case RSN_RED: return COLOR_RED_LINE;
			case RSN_BLUE: return COLOR_BLUE_LINE;
			// @formatter:on
			default:
				throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
			}
		}
		return super.getRouteColor(gRoute, agency);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CLEAN_AT_SPACE.matcher(tripHeadsign).replaceAll(CLEAN_AT_SPACE_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"AM", "PM",
				"EB", "WB", "NB", "SB",
				"SE", "SW", "NE", "NW",
				"LRT", "YYC", "TRW", "MRU", "SAIT", "JG", "EEEL",
				"AUArts", "CTrain",
		};
	}

	private static final Pattern ENDS_WITH_C_TRAIN_STATION = Pattern.compile("( (ctrain )?sta[t]?ion$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern CLEAN_AT_SPACE = Pattern.compile("(\\w)[\\s]*[@][\\s]*(\\w)");
	private static final String CLEAN_AT_SPACE_REPLACEMENT = "$1 @ $2";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = ENDS_WITH_C_TRAIN_STATION.matcher(gStopName).replaceAll(EMPTY);
		gStopName = CLEAN_AT_SPACE.matcher(gStopName).replaceAll(CLEAN_AT_SPACE_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
