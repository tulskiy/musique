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

import java.io.*;
import java.net.*;

public class ShoutClient {

    private Socket socket = null;
    private OutputStream socketOutputStream = null;

    private boolean connected = false;

    private String mount = null;
    private String host = null;
    private int port = 0;
    private String username = null;
    private String password = null;

    private String url = null;
    private String genre = null;
    private String title = null;
    private String desc = null;

    private char[] base64table = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
    };

    public ShoutClient() {

        connected = false;
    }

    public ShoutClient(String host, int port, String mount, String user, String password) {

        this.host = host;
        this.port = port;
        this.mount = mount;
        this.username = user;
        this.password = password;

        connected = false;
    }

    public boolean connect(String host, int port, String mount, String user, String password) {

        this.host = host;
        this.port = port;
        this.mount = mount;
        this.username = user;
        this.password = password;

        return connect();
    }

    public boolean connect() {

        if (!isConnected()) {
            try {
                socket = new Socket(host, port);
                // socket.connect( new InetSocketAddress( host, port ), 1000 ); // no timeout for now

                socketOutputStream = socket.getOutputStream();
                socket.setTcpNoDelay(true);
                writeHeader();
            }
            catch (UnknownHostException e) {
                connected = false;
                System.out.println("Unknown host: " + host);
            }
            catch (IOException e) {
                connected = false;
                System.out.println("Socket Connection Error: http://" + host + ":" + port);
                System.out.println(e + "\n");
            }
        }

        return connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMount() {
        return this.mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public String getUser() {
        return this.username;
    }

    public void setUser(String user) {
        this.username = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getURL() {
        return this.url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.desc;
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }

    public void writeHeader() throws IOException {

        socketOutputStream.write(("SOURCE " + mount + " HTTP/1.0" + "\r\n").getBytes());

        Base64EncoderDecoder base64Encode = new Base64EncoderDecoder(null, "");
        String rawAuth = username + ":" + password;
        String encodedAuth = base64Encode.encodeBase64(base64table.toString(), rawAuth);
        socketOutputStream.write(("Authorization: Basic " + encodedAuth + "\r\n").getBytes());

        socketOutputStream.write(("User-Agent: libshout/" + "\r\n").getBytes());
        socketOutputStream.write(("Content-Type: application/ogg" + "\r\n").getBytes());

        socketOutputStream.write(("ice-url: " + url + "\r\n").getBytes());
        socketOutputStream.write(("ice-public: 1" + "\r\n").getBytes());
        socketOutputStream.write(("ice-genre: " + genre + "\r\n").getBytes());
        socketOutputStream.write(("ice-name: " + title + "\r\n").getBytes());
        socketOutputStream.write(("ice-description: " + desc + "\r\n").getBytes());
        // socketOutputStream.write( new String("ice-audio-info: 1" + "\r\n").getBytes() );

        socketOutputStream.write(new String("\r\n").getBytes());
        socketOutputStream.flush();
        System.out.println("OggCast Header Sent");

        if (readHeaderResponse()) {
            connected = true;
        } else {
            connected = false;
        }
    }

    public boolean readHeaderResponse() throws IOException {

        InputStream socketInputStream = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(socketInputStream);
        BufferedReader br = new BufferedReader(isr);

        String responseHeader = br.readLine();
        System.out.println(responseHeader);

        int responseCodeIndex = responseHeader.indexOf(' ');
        int responseCode = Integer.parseInt(responseHeader.substring(responseCodeIndex + 1, responseCodeIndex + 4));

        if (responseCode >= 200 && responseCode < 300)
            return true;

        return false;
    }

    public void write(byte[] foo, int foostart, int foolength, byte[] bar, int barstart, int barlength) {

        try {
            socketOutputStream.write(foo, foostart, foolength);
            socketOutputStream.write(bar, barstart, barlength);
            socketOutputStream.flush();
        }
        catch (IOException e) {
            connected = false;
            System.out.println("Socket Connection Error: http://" + host + ":" + port);
            System.out.println(e);
        }
    }

    public void close() {
        try {
            connected = false;
            socket.shutdownOutput();
            socketOutputStream.close();
            socket.close();
            socketOutputStream = null;
            socket = null;
        }
        catch (Exception e) {
            System.out.println("Socket Connection Error: http://" + host + ":" + port);
            System.out.println(e);
        }
        System.out.println("Disconnected from: http://" + host + ":" + port);
    }
}
