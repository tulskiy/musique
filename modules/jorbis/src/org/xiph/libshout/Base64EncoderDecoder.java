/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.xiph.libshout;

public class Base64EncoderDecoder extends java.util.prefs.AbstractPreferences {
    private java.util.Hashtable encodedStore = new java.util.Hashtable();

    public Base64EncoderDecoder(java.util.prefs.AbstractPreferences prefs, java.lang.String string) {
        super(prefs, string);
    }

    public java.lang.String encodeBase64(java.lang.String raw)
            throws java.io.UnsupportedEncodingException {
        byte[] rawUTF8 = raw.getBytes("UTF8");
        this.putByteArray(raw, rawUTF8);
        return (java.lang.String) this.encodedStore.get(raw);
    }

    public java.lang.String encodeBase64(java.lang.String key, java.lang.String raw)
            throws java.io.UnsupportedEncodingException {
        byte[] rawUTF8 = raw.getBytes("UTF8");
        this.putByteArray(key, rawUTF8);
        return (java.lang.String) this.encodedStore.get(key);
    }

    @SuppressWarnings("unchecked")
    public java.lang.String decodeBase64(java.lang.String key, java.lang.String base64String)
            throws java.io.UnsupportedEncodingException, java.io.IOException {
        byte[] def = {(byte) 'D', (byte) 'E', (byte) 'F'};//placeholder
        this.encodedStore.put(key, base64String);
        byte[] byteResults = this.getByteArray(key, def);
        return new java.lang.String(byteResults, "UTF8");
    }

    public String get(String key, String def) {
        return (java.lang.String) this.encodedStore.get(key);
    }


    @SuppressWarnings("unchecked")
    public void put(String key, String value) {
        this.encodedStore.put(key, value);
    }


    //	dummy implementation as AbstractPreferences is extended to access methods above
    protected java.util.prefs.AbstractPreferences childSpi(String name) {
        return null;
    }

    protected String[] childrenNamesSpi() throws java.util.prefs.BackingStoreException {
        return null;
    }

    protected void flushSpi() throws java.util.prefs.BackingStoreException {
    }

    protected String getSpi(String key) {
        return null;
    }

    protected String[] keysSpi() throws java.util.prefs.BackingStoreException {
        return null;
    }

    protected void putSpi(String key, String value) {
    }

    protected void removeNodeSpi() throws java.util.prefs.BackingStoreException {
    }

    protected void removeSpi(String key) {
    }

    protected void syncSpi() throws java.util.prefs.BackingStoreException {
    }
}
