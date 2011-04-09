package net.sourceforge.jaad.impl.ps;

interface PSConstants {

	int MAX_ENVELOPES = 5;
	int MAX_IID_ICC_PARS = 34;
	int MAX_IPD_OPD_PARS = 17;
	int QMF_SLOTS = 32;
	int MAX_SSB = 91;
	int MAX_DELAY = 14;
	int MAX_AP_DELAY = 5;
	int MAX_AP_BANDS = 50;
	int AP_LINKS = 3;
	int[] BANDS = {71, 91}; //number of frequency bands that can be addressed by the sub subband index, k
	int[] PAR_BANDS = {20, 34}; //number of frequency bands that can be addressed by the parameter index, b(k)
	int[] ALLPASS_BANDS = {30, 50}; //number of all-pass filer bands
	int[] SHORT_DELAY_BAND = {42, 62}; //first stereo band using the short one sample delay
	//decorrelation
	float PEAK_DECAY_FACTOR = 0.76592833836465f;
	float TRANSIENT_IMPACT = 1.5f;
	float A_SMOOTH = 0.25f; //smoothing coefficient
	float DECAY_SLOPE = 0.05f;
	int[] DECAY_CUTOFF = {10, 32}; //start frequency band for the all-pass filter decay slope
	int[] LINK_DELAY = {3, 4, 5};
}
