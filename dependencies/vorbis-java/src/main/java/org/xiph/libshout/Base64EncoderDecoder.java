/*
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.
 *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2002
 * by the Xiph.Org Foundation http://www.xiph.org/
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
