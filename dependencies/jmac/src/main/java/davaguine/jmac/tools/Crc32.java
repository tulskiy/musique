/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package davaguine.jmac.tools;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public final class Crc32 {
    private final static long CRC32_TABLE[] = {0L, 1996959894L, 0xEE0E612CL, 0x990951BAL, 124634137L, 1886057615L, 0xE963A535L, 0x9E6495A3L, 249268274L, 2044508324L, 0xE0D5E91EL, 0x97D2D988L, 162941995L, 2125561021L, 0xE7B82D07L, 0x90BF1D91L, 498536548L, 1789927666L, 0xF3B97148L, 0x84BE41DEL, 450548861L, 1843258603L, 0xF4D4B551L, 0x83D385C7L, 325883990L, 1684777152L, 0xFD62F97AL, 0x8A65C9ECL, 335633487L, 1661365465L, 0xFA0F3D63L, 0x8D080DF5L, 997073096L, 1281953886L, 0xD56041E4L, 0xA2677172L, 1006888145L, 1258607687L, 0xD20D85FDL, 0xA50AB56BL, 901097722L, 1119000684L, 0xDBBBC9D6L, 0xACBCF940L, 853044451L, 1172266101L, 0xDCD60DCFL, 0xABD13D59L, 651767980L, 1373503546L, 0xC8D75180L, 0xBFD06116L, 565507253L, 1454621731L, 0xCFBA9599L, 0xB8BDA50FL, 671266974L, 1594198024L, 0xC60CD9B2L, 0xB10BE924L, 795835527L, 1483230225L, 0xC1611DABL, 0xB6662D3DL, 1994146192L, 31158534L, 0x98D220BCL, 0xEFD5102AL, 1907459465L, 112637215L, 0x9FBFE4A5L, 0xE8B8D433L, 2013776290L, 251722036L, 0x9609A88EL, 0xE10E9818L, 2137656763L, 141376813L, 0x91646C97L, 0xE6635C01L, 1802195444L, 476864866L, 0x856530D8L,
                                               0xF262004EL, 1812370925L, 453092731L, 0x8208F4C1L, 0xF50FC457L, 1706088902L, 314042704L, 0x8BBEB8EAL, 0xFCB9887CL, 1658658271L, 366619977L, 0x8CD37CF3L, 0xFBD44C65L, 1303535960L, 984961486L, 0xA3BC0074L, 0xD4BB30E2L, 1256170817L, 1037604311L, 0xA4D1C46DL, 0xD3D6F4FBL, 1131014506L, 879679996L, 0xAD678846L, 0xDA60B8D0L, 1141124467L, 855842277L, 0xAA0A4C5FL, 0xDD0D7CC9L, 1342533948L, 654459306L, 0xBE0B1010L, 0xC90C2086L, 1466479909L, 544179635L, 0xB966D409L, 0xCE61E49FL, 1591671054L, 702138776L, 0xB0D09822L, 0xC7D7A8B4L, 1504918807L, 783551873L, 0xB7BD5C3BL, 0xC0BA6CADL, 0xEDB88320L, 0x9ABFB3B6L, 62317068L, 1957810842L, 0xEAD54739L, 0x9DD277AFL, 81470997L, 1943803523L, 0xE3630B12L, 0x94643B84L, 225274430L, 2053790376L, 0xE40ECF0BL, 0x9309FF9DL, 167816743L, 2097651377L, 0xF00F9344L, 0x8708A3D2L, 503444072L, 1762050814L, 0xF762575DL, 0x806567CBL, 426522225L, 1852507879L, 0xFED41B76L, 0x89D32BE0L, 282753626L, 1742555852L, 0xF9B9DF6FL, 0x8EBEEFF9L, 397917763L, 1622183637L, 0xD6D6A3E8L, 0xA1D1937EL, 953729732L, 1340076626L, 0xD1BB67F1L, 0xA6BC5767L, 1068828381L, 1219638859L, 0xD80D2BDAL,
                                               0xAF0A1B4CL, 906185462L, 1090812512L, 0xDF60EFC3L, 0xA867DF55L, 829329135L, 1181335161L, 0xCB61B38CL, 0xBC66831AL, 628085408L, 1382605366L, 0xCC0C7795L, 0xBB0B4703L, 570562233L, 1426400815L, 0xC5BA3BBEL, 0xB2BD0B28L, 733239954L, 1555261956L, 0xC2D7FFA7L, 0xB5D0CF31L, 752459403L, 1541320221L, 0x9B64C2B0L, 0xEC63F226L, 1969922972L, 40735498L, 0x9C0906A9L, 0xEB0E363FL, 1913087877L, 83908371L, 0x95BF4A82L, 0xE2B87A14L, 2075208622L, 213261112L, 0x92D28E9BL, 0xE5D5BE0DL, 2094854071L, 198958881L, 0x86D3D2D4L, 0xF1D4E242L, 1759359992L, 534414190L, 0x81BE16CDL, 0xF6B9265BL, 1873836001L, 414664567L, 0x88085AE6L, 0xFF0F6A70L, 1711684554L, 285281116L, 0x8F659EFFL, 0xF862AE69L, 1634467795L, 376229701L, 0xA00AE278L, 0xD70DD2EEL, 1308918612L, 956543938L, 0xA7672661L, 0xD06016F7L, 1231636301L, 1047427035L, 0xAED16A4AL, 0xD9D65ADCL, 1088359270L, 936918000L, 0xA9BCAE53L, 0xDEBB9EC5L, 1202900863L, 817233897L, 0xBDBDF21CL, 0xCABAC28AL, 1404277552L, 615818150L, 0xBAD03605L, 0xCDD70693L, 1423857449L, 601450431L, 0xB3667A2EL, 0xC4614AB8L, 1567103746L, 711928724L, 0xB40BBE37L, 0xC30C8EA1L, 1510334235L, 755167117};

    private long crc;

    /**
     * Dummy Constructor
     */
    public Crc32() {
        crc = 0xFFFFFFFFL;
    }

    public void init() {
        crc = 0xFFFFFFFFL;
    }

    /**
     * Feed a bitstring to the crc calculation.
     */
    public void append(byte bits) {
        long l;
        crc = ((l = crc) >> 8L) ^ CRC32_TABLE[(int) ((l & 0xFF) ^ (long) (bits & 0xff))];
    }

    public void append(byte bits1, byte bits2) {
        long l;
        long[] a1;
        l = ((l = crc) >> 8L) ^ (a1 = CRC32_TABLE)[(int) ((l & 0xFF) ^ (long) (bits1 & 0xFF))];
        crc = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) (bits2 & 0xFF))];
    }

    /**
     * Feed a bitstring to the crc calculation.
     */
    public void append(short bits) {
        long l;
        long[] a1;
        l = ((l = crc) >> 8L) ^ (a1 = CRC32_TABLE)[(int) ((l & 0xFF) ^ (long) (bits & 0xFF))];
        crc = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits & 0xffff) >> 8))];
    }

    public void append(short bits1, short bits2) {
        long l;
        long[] a1;
        l = ((l = crc) >> 8L) ^ (a1 = CRC32_TABLE)[(int) ((l & 0xFF) ^ (long) (bits1 & 0xFF))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits1 & 0xffff) >> 8))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) (bits2 & 0xFF))];
        crc = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits2 & 0xffff) >> 8))];
    }

    /**
     * Feed a bitstring to the crc calculation.
     */
    public void append24(int bits) {
        long l;
        long[] a1;
        l = ((l = crc) >> 8L) ^ (a1 = CRC32_TABLE)[(int) ((l & 0xFF) ^ (long) (bits & 0xFF))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits & 0xff00) >> 8))];
        crc = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits & 0xff0000) >> 16))];
    }

    public void append24(int bits1, int bits2) {
        long l;
        long[] a1;
        l = ((l = crc) >> 8L) ^ (a1 = CRC32_TABLE)[(int) ((l & 0xFF) ^ (long) (bits1 & 0xFF))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits1 & 0xff00) >> 8))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits1 & 0xff0000) >> 16))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) (bits2 & 0xFF))];
        l = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits2 & 0xff00) >> 8))];
        crc = (l >> 8L) ^ a1[(int) ((l & 0xFF) ^ (long) ((bits2 & 0xff0000) >> 16))];
    }

    public void prefinalizeCrc() {
        crc ^= 0xFFFFFFFFL;
    }

    public void finalizeCrc() {
        crc >>= 1;
    }

    public long getCrc() {
        return crc;
    }

    public void doSpecial() {
        crc |= 0x80000000L;
    }

    /**
     * Return the calculated checksum.
     * Erase it for next calls to add_bits().
     */
    public long checksum() {
        long sum = crc;
        crc = 0xFFFFFFFFL;
        sum ^= 0xFFFFFFFFL;
        sum >>= 1;
        return sum;
    }
}
