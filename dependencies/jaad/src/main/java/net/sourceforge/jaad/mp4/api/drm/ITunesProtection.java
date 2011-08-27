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
package net.sourceforge.jaad.mp4.api.drm;

import net.sourceforge.jaad.mp4.api.Protection;
import net.sourceforge.jaad.mp4.boxes.Box;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.impl.drm.FairPlayDataBox;

public class ITunesProtection extends Protection {

	private final String userID, userName, userKey;
	private final byte[] privateKey, initializationVector;

	public ITunesProtection(Box sinf) {
		super(sinf);

		final Box schi = sinf.getChild(BoxTypes.SCHEME_INFORMATION_BOX);
		userID = new String(((FairPlayDataBox) schi.getChild(BoxTypes.FAIRPLAY_USER_ID_BOX)).getData());
		
		//user name box is filled with 0
		final byte[] b = ((FairPlayDataBox) schi.getChild(BoxTypes.FAIRPLAY_USER_NAME_BOX)).getData();
		int i = 0;
		while(b[i]!=0) {
			i++;
		}
		userName = new String(b, 0, i-1);
		
		userKey = new String(((FairPlayDataBox) schi.getChild(BoxTypes.FAIRPLAY_USER_KEY_BOX)).getData());
		privateKey = ((FairPlayDataBox) schi.getChild(BoxTypes.FAIRPLAY_PRIVATE_KEY_BOX)).getData();
		initializationVector = ((FairPlayDataBox) schi.getChild(BoxTypes.FAIRPLAY_IV_BOX)).getData();
	}

	@Override
	public Scheme getScheme() {
		return Scheme.ITUNES_FAIR_PLAY;
	}

	public String getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserKey() {
		return userKey;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public byte[] getInitializationVector() {
		return initializationVector;
	}
}
