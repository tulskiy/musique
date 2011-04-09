package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box sets up default values used by the movie fragments. By setting
 * defaults in this way, space and complexity can be saved in each Track
 * Fragment Box.
 *
 * @author in-somnia
 */
public class TrackExtendsBox extends FullBox {

	private long trackID, defaultSampleDescriptionIndex, defaultSampleDuration, defaultSampleSize;
	private int sampleDegradationPriority, samplePaddingValue;
	private int sampleDependsOn, sampleIsDependedOn, sampleHasRedundancy;
	private boolean differenceSample;

	public TrackExtendsBox() {
		super("Track Extends Box", "trex");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		trackID = in.readBytes(4);
		defaultSampleDescriptionIndex = in.readBytes(4);
		defaultSampleDuration = in.readBytes(4);
		defaultSampleSize = in.readBytes(4);
		long l = in.readBytes(4);
		/* 6 bits reserved
		 * 2 bits sampleDependsOn
		 * 2 bits sampleIsDependedOn
		 * 2 bits sampleHasRedundancy
		 * 3 bits samplePaddingValue
		 * 1 bit sampleIsDifferenceSample
		 * 16 bits sampleDegradationPriority
		 */
		sampleDegradationPriority = (int) (l&0xFFFF);
		l >>= 16;
		differenceSample = (l&1)==1;
		l >>= 1;
		samplePaddingValue = (int) (l&7);
		l >>= 3;
		sampleHasRedundancy = (int) (l&3);
		l >>= 2;
		sampleIsDependedOn = (int) (l&3);
		l >>= 2;
		sampleDependsOn = (int) (l&3);

		left -= 20;
	}

	/**
	 * The track ID identifies the track; this shall be the track ID of a track
	 * in the Movie Box.
	 *
	 * @return the track ID
	 */
	public long getTrackID() {
		return trackID;
	}

	/**
	 * The default sample description index used in the track fragments.
	 *
	 * @return the default sample description index
	 */
	public long getDefaultSampleDescriptionIndex() {
		return defaultSampleDescriptionIndex;
	}

	/**
	 * The default sample duration used in the track fragments.
	 *
	 * @return the default sample duration
	 */
	public long getDefaultSampleDuration() {
		return defaultSampleDuration;
	}

	/**
	 * The default sample size used in the track fragments.
	 *
	 * @return the default sample size
	 */
	public long getDefaultSampleSize() {
		return defaultSampleSize;
	}

	/**
	 * The default 'sample depends on' value as defined in the
	 * SampleDependencyTypeBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleDependsOn()
	 * @return the default 'sample depends on' value
	 */
	public int getSampleDependsOn() {
		return sampleDependsOn;
	}

	/**
	 * The default 'sample is depended on' value as defined in the
	 * SampleDependencyTypeBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleIsDependedOn()
	 * @return the default 'sample is depended on' value
	 */
	public int getSampleIsDependedOn() {
		return sampleIsDependedOn;
	}

	/**
	 * The default 'sample has redundancy' value as defined in the
	 * SampleDependencyBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleHasRedundancy()
	 * @return the default 'sample has redundancy' value
	 */
	public int getSampleHasRedundancy() {
		return sampleHasRedundancy;
	}

	/**
	 * The default padding value as defined in the PaddingBitBox.
	 *
	 * @see PaddingBitBox#getPad1()
	 * @return the default padding value
	 */
	public int getSamplePaddingValue() {
		return samplePaddingValue;
	}

	public boolean isSampleDifferenceSample() {
		return differenceSample;
	}

	/**
	 * The default degradation priority for the samples.
	 * @return
	 */
	public int getSampleDegradationPriority() {
		return sampleDegradationPriority;
	}
}
