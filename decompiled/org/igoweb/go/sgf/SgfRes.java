/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import org.igoweb.go.GoRes;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class SgfRes
extends Resource {
    public static final int BASE = 720103995;
    public static final int CUT_SHORT = 720103995;
    public static final int BAD_COLOR = 720103996;
    public static final int BAD_INTEGER = 720103997;
    public static final int BAD_NUMBER = 720103998;
    public static final int BAD_ENCODING = 720103999;
    public static final int BAD_PROP_NAME = 720104000;
    public static final int BAD_BOARD_SIZE = 720104001;
    public static final int BAD_TOKEN = 720104002;
    public static final int CONFLICT_WARN = 720104003;
    public static final int CORRUPTED_TEXT = 720104004;
    public static final int BAD_BRANCH = 720104005;
    public static final int BAD_FILE_FORMAT = 720104006;
    public static final int BAD_GAME_TYPE = 720104007;
    public static final int FORFEIT = 720104008;
    public static final int BAD_LOC = 720104009;
    public static final int BAD_HANDICAP = 720104010;
    public static final int UNTERMINATED_ARG = 720104011;
    public static final int BAD_SGF_FILE = 720104012;
    public static final int BAD_TIME = 720104013;
    public static final int DEFAULT_SGF_NAME = 720104014;
    public static final int BAD_LABEL = 720104015;
    public static final int MISSING_ARGUMENT = 720104016;
    public static final int MISSING_TYPE = 720104017;
    public static final int REDUNDANT_RULE = 720104018;
    public static final int SECURITY_ERROR = 720104019;
    public static final int AMBIGUOUS_BOARD_SIZE = 720104020;
    public static final int UNEXPECTED_EOF_TOKEN = 720104021;
    public static final int BAD_RANK = 720104023;
    public static final int BAD_RESULT = 720104024;
    public static final int DONT_CLOSE = 720104027;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("{0}...", 720103995, "{0}...", "A message that is used when we cut short something.", new Object[][]{{"This is a very long messag"}, {"Moonbeam [25k?] (Moonbeam [25k?] vs. Lar"}}), new ResEntry("\"{0}\" at line {1} is not a valid color", 720103996, "\"{0}\" at line {1} is not a valid color. The color should be \"B\" or \"W\"", "An error from reading a bad SGF file.", new Object[]{"zzz", new Integer(5)}), new ResEntry("Argument \"{0}\" on line {1} is not a valid integer", 720103997, "Argument \"{0}\" on line {1} is not a valid integer.", "An error from reading a bad SGF file.", new Object[]{"1z", new Integer(12)}), new ResEntry("Argument \"{0}\" on line {1} is not a valid number", 720103998, "Argument \"{0}\" on line {1} is not a valid number.", "An error from reading a bad SGF file.", new Object[]{"1z", new Integer(12)}), new ResEntry("badEncoding", 720103999, "The character encoding \"{0}\" specified on line {1} cannot be read.", "An error while reading an SGF file.", new Object[]{"ISO-1231232-58", new Integer(1)}), new ResEntry("badParamName", 720104000, "Invalid property code at line {0}", "An error from reading a bad SGF file.", new Object[][]{{new Integer(5)}, {new Integer(11)}}), new ResEntry("Bogus board size \"{0}\" at line {1}", 720104001, "Bogus board size \"{0}\" at line {1}", "An error from reading a bad SGF file.", new Object[]{"103", new Integer(10)}), new ResEntry("Bogus token. Last good token at line {0}", 720104002, "Bogus token. Last good token at line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(18)}), new ResEntry("conflictWarn", 720104003, "A property with type \"{0}\" on line {1} conflicts with another property. The previous property will be removed.", "A warning of bad SGF code.", new Object[][]{{"B", new Integer(12)}, {"AE", new Integer(15)}}), new ResEntry("corruptedText", 720104004, "An illegal character combination for character set \"{0}\" was detected on line {1}", "An error when there is corrupted text in the SGF file.", new Object[][]{{"UTF-8", new Integer(10)}, {"Latin-1", new Integer(15)}}), new ResEntry("Error reading branch; \"(\" without \";\" at line {0}", 720104005, "Error reading branch; \"(\" without \";\" at line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(25)}), new ResEntry("File format is {0}; should be 1 through 4", 720104006, "File format is {0}; should be 1 through 4", "An error from reading a bad SGF file.", new Object[]{new Integer(8)}), new ResEntry("File's game type is {0}, should be 1", 720104007, "File''s game type is {0}, should be 1", "An error from reading a bad SGF file.", new Object[]{new Integer(2)}), new ResEntry("Forfeit", 720104008, "Forfeit", "As in \"White won by forfeit\"."), new ResEntry("Invalid board location \"{0}\" on line {1}", 720104009, "Invalid board location \"{0}\" on line {1}", "An error from reading a bad SGF file.", new Object[]{"zz", new Integer(158)}), new ResEntry("Invalid handicap \"{0}\" at line {1}", 720104010, "Invalid handicap \"{0}\" at line {1}", "An error from reading a bad SGF file.", new Object[]{"h-3", new Integer(30)}), new ResEntry("Invalid SGF file; \"(;\" not found", 720104012, "Invalid SGF file; \"(;\" not found", "An error from reading a bad SGF file."), new ResEntry("Invalid time specification \"{0}\" at line {1}", 720104013, "Invalid time specification \"{0}\" at line {1}", "An error from reading a bad SGF file.", new Object[]{"5:00", new Integer(2)}), new ResEntry("Label \"{0}\" on line {1} is invalid", 720104015, "Label \"{0}\" on line {1} is invalid", "An error from reading a bad SGF file.", new Object[]{"ac:", new Integer(15)}), new ResEntry("No argument for parameter on line {0}", 720104016, "No argument for property on line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(93)}), new ResEntry("Parameter missing type at line {0}", 720104017, "Property missing type at line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(80)}), new ResEntry("Redundant rule specification at line {0}", 720104018, "Redundant rule specification at line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(12)}), new ResEntry("Security error opening file.", 720104019, "Security error opening file."), new ResEntry("The board size follows a location definition in an ambiguous manner", 720104020, "The board size follows a location definition in an ambiguous manner", "This is an SGF error. Explaining it is hard; just do your best to translate it."), new ResEntry("Unexpected end of file. Last good token at line {0}", 720104021, "Unexpected end of file. Last good token at line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(15)}), new ResEntry("Unknown rank \"{0}\" on line {1}", 720104023, "Unknown rank \"{0}\" on line {1}", "An error from reading a bad SGF file.", new Object[]{"5 dan", new Integer(2)}), new ResEntry("Unreadable result \"{0}\" on line {1}", 720104024, "Unreadable result \"{0}\" on line {1}", "An error from reading a bad SGF file.", new Object[]{"Black wins", new Integer(2)}), new ResEntry("Unterminated parameter argument on line {0}", 720104011, "Unterminated property argument on line {0}", "An error from reading a bad SGF file.", new Object[]{new Integer(35)}), new ResEntry("untitled.sgf", 720104014, "untitled.sgf", "This is the default name for SGF files the first time that you try to save them."), new ResEntry("dontClose", 720104027, "Don't Close", "A button in a dialogue that means \"don't close this game\".")};

    @Override
    public String propFilePath() {
        return "org/igoweb/go/sgf/res/Res";
    }

    public String toString() {
        return "Go Resources";
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[]{new GoRes()};
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }
}
