package org.mtransit.parser.ca_calgary_transit_train;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://www.calgarytransit.com/developer-resources
// https://data.calgary.ca/OpenData/Pages/DatasetDetails.aspx?DatasetID=PDC0-99999-99999-00501-P(CITYonlineDefault)
// https://data.calgary.ca/_layouts/OpenData/DownloadDataset.ashx?Format=FILE&DatasetId=PDC0-99999-99999-00501-P(CITYonlineDefault)&VariantId=5(CITYonlineDefault)
public class CalgaryTransitTrainAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-calgary-transit-train-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CalgaryTransitTrainAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating Calgary Transit train data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Calgary Transit train data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return gRoute.route_type != MAgency.ROUTE_TYPE_LIGHT_RAIL; // declared as light rail but we classify it as a train (not on the road)
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.route_short_name); // using route short name as route ID
	}

	private static final int RSN_RED = 201;
	private static final int RSN_BLUE = 202;

	private static final String SADDLETOWNE = "Saddletowne";
	private static final String SOMERSET_BRIDLEWOOD = "Somerset-Bridlewood";
	private static final String _69TH_ST = "69th St";
	private static final String TUSCANY = "Tuscany";

	private static final String SLASH = " / ";

	private static final String RLN_RED = TUSCANY + SLASH + SOMERSET_BRIDLEWOOD;
	private static final String RLN_BLUE = _69TH_ST + SLASH + SADDLETOWNE;

	@Override
	public String getRouteLongName(GRoute gRoute) {
		int rsn = Integer.parseInt(gRoute.route_short_name);
		switch (rsn) {
		// @formatter:off
		case RSN_RED: return RLN_RED;
		case RSN_BLUE: return RLN_BLUE;
		// @formatter:on
		default:
			System.out.println("Unexpected route long name " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String AGENCY_COLOR_RED = "B83A3F"; // LIGHT RED (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_RED;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_RED_LINE = "EE2622"; // RED (from PDF map)
	private static final String COLOR_BLUE_LINE = "0F4076"; // BLUE (from PDF map)

	@Override
	public String getRouteColor(GRoute gRoute) {
		int rsn = Integer.parseInt(gRoute.route_short_name);
		switch (rsn) {
		// @formatter:off
		case RSN_RED: return COLOR_RED_LINE;
		case RSN_BLUE: return COLOR_BLUE_LINE;
		// @formatter:on
		default:
			System.out.println("Unexpected route color " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final long TRIP_ID_BLUE_SADDLETOWNE = 20200l;
	private static final String STOP_CODE_69TH_ST = "3627"; // first station 202, 0

	private static final long TRIP_ID_BLUE_69TH_ST = 20201l;
	private static final String STOP_CODE_SADDLETOWNE = "9781"; // first station 202, 1

	@Override
	public int compare(MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ts1.getTripId() == TRIP_ID_BLUE_SADDLETOWNE) {
			if (STOP_CODE_69TH_ST.equals(ts1GStop.stop_code)) {
				return +1;
			} else if (STOP_CODE_69TH_ST.equals(ts2GStop.stop_code)) {
				return -1;
			}
		} else if (ts1.getTripId() == TRIP_ID_BLUE_69TH_ST) {
			if (STOP_CODE_SADDLETOWNE.equals(ts1GStop.stop_code)) {
				return +1;
			} else if (STOP_CODE_SADDLETOWNE.equals(ts2GStop.stop_code)) {
				return -1;
			}
		}
		return super.compare(ts1, ts2, ts1GStop, ts2GStop);
	}

	private static final long RID_RED = 201l;
	private static final long RID_BLUE = 202l;

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip) {
		if (mRoute.id == RID_RED) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(TUSCANY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SOMERSET_BRIDLEWOOD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == RID_BLUE) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SADDLETOWNE, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(_69TH_ST, gTrip.direction_id);
				return;
			}
		}
		System.out.println("Unexpected trip " + gTrip);
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*[s|e|w|n]b[\\s]$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^[\\s]*[s|e|w|n]b[\\s]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_CTRAIN_STATION = Pattern.compile("( (ctrain )?sta[t]?ion$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AT_SIGN = Pattern.compile("([\\s]*@[\\s]*)", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = ENDS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = ENDS_WITH_CTRAIN_STATION.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT_SIGN.matcher(gStopName).replaceAll(AT_SIGN_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		gStopName = MSpec.cleanNumbers(gStopName);
		return MSpec.cleanLabel(gStopName);
	}
}
