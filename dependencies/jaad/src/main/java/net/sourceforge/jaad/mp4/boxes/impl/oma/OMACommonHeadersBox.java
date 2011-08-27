/*
 *  Copyright (C) 2011 in-somnia
 * 
 *  This file is part of JAAD.
 * 
 *  JAAD is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  JAAD is distributed in the hope that it will be useful, but WITHOUT 
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

//TODO: add remaining javadoc
public class OMACommonHeadersBox extends FullBox {

	private int encryptionMethod, paddingScheme;
	private long plaintextLength;
	private byte[] contentID, rightsIssuerURL;
	private Map<String, String> textualHeaders;

	public OMACommonHeadersBox() {
		super("OMA DRM Common Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		encryptionMethod = in.read();
		paddingScheme = in.read();
		plaintextLength = in.readBytes(8);
		final int contentIDLength = (int) in.readBytes(2);
		final int rightsIssuerURLLength = (int) in.readBytes(2);
		int textualHeadersLength = (int) in.readBytes(2);

		contentID = new byte[contentIDLength];
		in.readBytes(contentID);
		rightsIssuerURL = new byte[rightsIssuerURLLength];
		in.readBytes(rightsIssuerURL);

		textualHeaders = new HashMap<String, String>();
		String key, value;
		while(textualHeadersLength>0) {
			key = new String(in.readTerminated((int) getLeft(in), ':'));
			value = new String(in.readTerminated((int) getLeft(in), 0));
			textualHeaders.put(key, value);

			textualHeadersLength -= key.length()+value.length()+2;
		}

		readChildren(in);
	}

	/**
	 * The encryption method defines how the encrypted content can be decrypted.
	 * Values for the field are defined in the following table:
	 * 
	 * <table>
	 * <tr><th>Value</th><th>Algorithm</th></tr>
	 * <tr><td>0</td><td>no encryption used</td></tr>
	 * <tr><td>1</td><td>AES_128_CBC:<br />AES symmetric encryption as defined 
	 * by NIST. 128 bit keys, Cipher block chaining mode (CBC). For the first 
	 * block a 128-bit initialisation vector (IV) is used. For DCF files, the IV
	 * is included in the OMADRMData as a prefix of the encrypted data. For 
	 * non-streamable PDCF files, the IV is included in the IV field of the 
	 * OMAAUHeader and the IVLength field in the OMAAUFormatBox MUST be set to
	 * 16. Padding according to RFC 2630</td></tr>
	 * <tr><td>2</td><td>AES_128_CTR:<br />AES symmetric encryption as defined 
	 * by NIST. 128 bit keys, Counter mode (CTR). The counter block has a length
	 * of 128 bits. For DCF files, the initial counter value is included in the 
	 * OMADRMData as a prefix of the encrypted data. For non-streamable PDCF 
	 * files, the initial counter value is included in the IV field of the 
	 * OMAAUHeader  and the IVLength field in the OMAAUFormatBox MUST be set to 
	 * 16. For each cipherblock the counter is incremented by 1 (modulo 2128). 
	 * No padding.</td></tr>
	 * </table>
	 * 
	 * @return the encryption method
	 */
	public int getEncryptionMethod() {
		return encryptionMethod;
	}

	/**
	 * The padding scheme defines how the last block of ciphertext is padded. 
	 * Values of the padding scheme field are defined in the following table:
	 * 
	 * <table>
	 * <tr><th>Value</th><th>Padding scheme</th></tr>
	 * <tr><td>0</td><td>No padding (e.g. when using NULL or CTR algorithm)</td></tr>
	 * <tr><td>1</td><td>Padding according to RFC 2630</td></tr>
	 * </table>
	 * 
	 * @return the padding scheme
	 */
	public int getPaddingScheme() {
		return paddingScheme;
	}

	public long getPlaintextLength() {
		return plaintextLength;
	}

	public byte[] getContentID() {
		return contentID;
	}

	public byte[] getRightsIssuerURL() {
		return rightsIssuerURL;
	}

	public Map<String, String> getTextualHeaders() {
		return textualHeaders;
	}
}
