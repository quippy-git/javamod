package de.quippy.sidplay.resid_builder;

public class sid_filter_t {
	public int /* sid_fc_t */cutoff[][] = new int[0x800][2];

	public int /* uint_least16_t */points;

	public int /* int32_t */Lthreshold, Lsteepness, Llp, Lbp, Lhp;
	public int /* int32_t */Hthreshold, Hsteepness, Hlp, Hbp, Hhp;

}