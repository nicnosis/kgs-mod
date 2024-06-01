/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.io.FileNotFoundException;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class UtilRes
extends Resource {
    public static final int BASE = -24406070;
    public static final int ERROR_READING_PREFS = -24406070;
    public static final int ERROR_WRITING_PREFS = -24406069;
    public static final int NN_NN_NN = -24406068;
    public static final int PARSE_HOUR_MIN_SEC = -24406067;
    public static final int PARSE_MIN_SEC = -24406066;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("errorReadingPrefs", -24406070, "Your preference file \"{0}\" could not be read. Error \"{1}\" occurred. This program will run as normal, but your preferences will not be available.", "", new Object[]{".cgobanrc", new FileNotFoundException()}), new ResEntry("errorWritingPrefs", -24406069, "Your preference file \"{0}\" could not be written. Error \"{1}\" occurred. Your preferences will not be saved for next time.", "", new Object[]{".cgobanrc", new FileNotFoundException()}), new ResEntry("nn:nn:nn", -24406068, "{0,choice,0#{1}|1#{0}:{1,number,00}}:{2,number,00}", "This is for a time period. It should show hours if the time is over an hour (ie if the first argument is nonzero), or minutes and seconds if the time period is under an hour.", new Object[][]{{0, 5, 8}, {2, 5, 8}, {0, 45, 12}}), new ResEntry("ParseHourMinSec", -24406067, "{0,number,integer}:{1,number,integer}:{2,number,integer}", "This is a string that decodes user input into hours, minutes, and seconds."), new ResEntry("ParseMinSec", -24406066, "{0,number,integer}:{1,number,integer}", "This is a string that decodes user input into minutes and seconds.")};

    @Override
    public String propFilePath() {
        return "org/igoweb/util/res/Res";
    }

    public String toString() {
        return "org.igoweb utility Resources";
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }
}
