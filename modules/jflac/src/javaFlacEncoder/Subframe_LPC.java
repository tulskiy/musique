/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package javaFlacEncoder;

/**
 * Implements the Subframe abstract class, providing encoding support for the
 * FLAC LPC Subframe.
 * 
 * @author Preston Lacey
 */
public class Subframe_LPC extends Subframe {
    public static long totalTime = 0;
    private class PartialResult {
        int[] samples;
        int start;
        int increment;
        int count;
        int subframeSampleSize;

        int lpcOrder;
        int lowOrderBits;
        int totalBits;
        int precision;
        int lastCount;
    }
    
    /* Following values used frequently, let's calculate just once */
    private static final double LOGE_2 = Math.log(2);
    private static final double SQRT_2 = Math.sqrt(2);

    /** Maximum LPC order that is supported by this subframe */
    public static final int MAX_LPC_ORDER = 32;

    /** For debugging: Higher values equals greater output, generally in
     * increments of 10 */
    public static int DEBUG_LEV = 0;

/** Subframe type implemented by this subframe. */
    public static final EncodingConfiguration.SubframeType type =
            EncodingConfiguration.SubframeType.LPC;

    int sampleSize = 0;
    RiceEncoder rice = null;
    int _lpcOrder = 0;
    int _lowOrderBits = 0;
    long _totalBits = 0;
    int _precision = 15;
    int _lastCount = 0;
    int[] _errors = null;
    int[] _quantizedCoeffs = null;
    int _shift = 0;
    LPC[] lpcs = null;
    int[] _samples = null;
    int _offset = 0;
    int _frameSampleSize;
    int _start = 0;
    int _increment = 0;
    long[] correlations = null;
    int[] _windowedSamples = null;
    Subframe_LPC(StreamConfiguration sc) {
        super(sc);
        sampleSize = sc.getBitsPerSample();
        rice = new RiceEncoder();
        lpcs = new LPC[MAX_LPC_ORDER+1];
        for(int i = 0; i < MAX_LPC_ORDER+1; i++)
            lpcs[i] = new LPC(i);
        _lastCount = -1;
        _quantizedCoeffs = new int[MAX_LPC_ORDER+1];
    }

    /**
     * This method is used to set the encoding configuration.
     * @param ec    encoding configuration to use.
     * @return      true if configuration was changed, false otherwise
     */
    @Override
    public boolean registerConfiguration(EncodingConfiguration ec) {
        return super.registerConfiguration(ec);
    }

    public int encodeSamples(int[] samples, int count, int start, int skip,
        int offset, int unencSampleSize) {
         int encodedSamples = count;
        if(DEBUG_LEV > 0) {
            System.err.println("Subframe_LPC::encodeSamples(...) : Begin");
            if(DEBUG_LEV > 10) {
                System.err.println("--count : " +count);
                System.err.println("start:skip:offset:::"+start+":"+skip+":"+offset);
            }
        }
        int increment = skip+1;
        if(count != _lastCount) {
            _errors = new int[count];
            _lastCount = count;
            _windowedSamples = new int[count];

        }
        int minOrder = ec.getMinLPCOrder();
        int maxOrder = ec.getMaxLPCOrder();
        int frameSampleSize = unencSampleSize;
        int order = -1;
        long totalBits = 0;
        long[] R = null;
        if(correlations == null || correlations.length < maxOrder+1) {
           R = new long[maxOrder+1];
           correlations = R;
        }
        else
           R = correlations;
        LPC.window(samples, count, start, increment, _windowedSamples);
        //LPC.createAutoCorrelation(R, samples, count, start, increment, maxOrder);
        LPC.createAutoCorrelation(R, _windowedSamples, count, 0, 1, maxOrder);
        int[] coefficients = new int[MAX_LPC_ORDER+1];
        int[] errors = new int[count];
        int lowOrderBits = 0;
        int precision = 0;
        int shift = 0;
        int watchCount = 2;
        for(int i = maxOrder; i >= minOrder; i--) {
            LPC.calculate(lpcs[i], R);
            int tempTotalBits = partialEncodeLPC(samples, count, start, increment,
                    lpcs[i], this,frameSampleSize);
            //compare to current order: If last not set or size < last, replace
            if(tempTotalBits < totalBits || order == -1) {
                order = i;
                totalBits = tempTotalBits;
                lowOrderBits = _lowOrderBits;
                precision = _precision;
                shift = _shift;
                int[] temp = coefficients;
                coefficients = _quantizedCoeffs;
                _quantizedCoeffs = temp;
                temp = errors;
                errors = _errors;
                _errors = temp;
                //priorLPC = lpcs[i];
                watchCount = 2;
            }
            else {
               if(--watchCount == 0)
                break;
            }
        }
        _lowOrderBits = lowOrderBits;
        _precision = precision;
        _shift = shift;
        _quantizedCoeffs = coefficients;
        _errors = errors;
        _samples = samples;
        _offset = offset;
        _frameSampleSize = unencSampleSize;
        _start = start;
        _increment = increment;
        _totalBits = totalBits;
        _lpcOrder = order;
        return encodedSamples;
    }

    /**
     * Return the estimated size of the previous encode attempt in bits. Since
     * returning the data from an encode is costly(due to the rice encoding and FLAC
     * compliant bit-packing), this allows us to estimate the size first, and
     * therefore choose another subframe type if this is larger.
     *
     * @return estimated size in bits of encoded subframe.
     */
    public long estimatedSize() {
        return _totalBits;
    }

    /**
     * Get the data from the last encode attempt. Data is returned in an
     * EncodedElement, properly packed at the bit-level to be added directly to
     * a FLAC stream.
     *
     * @return EncodedElement containing encoded subframe
     */
    public EncodedElement getData() {
        EncodedElement result = new EncodedElement();
        result.clear((int)_totalBits+1, _offset);
        writeLPC(_samples, _lastCount, _start,
            _increment, result, _frameSampleSize, _lowOrderBits,
            _precision, _shift, _quantizedCoeffs, _errors, _lpcOrder,rice);
        int totalBits = result.getTotalBits();
        this.lastEncodedSize = (int)totalBits;

        if(DEBUG_LEV > 0) {
            System.err.println("lastencodedSize set: "+this.lastEncodedSize);
            System.err.println("Subframe_LPC::getData(...): End");
        }
        return result;
    }

    public int encodeSamples(int[] samples, int count, int start, int skip,
        EncodedElement dataEle, int offset, int unencSampleSize ) {

        encodeSamples(samples, count, start, skip,
            offset, unencSampleSize);

        EncodedElement result = getData();
        int totalBits = result.getTotalBits();
        dataEle.data = result.data;
        dataEle.usableBits = result.usableBits;
        dataEle.offset = result.offset;
        dataEle.previous = result.previous;
        dataEle.next = result.next;
        this.lastEncodedSize = (int)totalBits;

        return count;

    }

    private static void writeHeadersAndData(EncodedElement dataEle, int order,
            int[] coeff, int precision, int shift, int[] samples,
            int sampleSize, int start, int skip) {
        //write headers
        int encodedType = 1<<5 | (order-1);
        dataEle.addInt(0, 1);
        dataEle.addInt(encodedType, 6);
        dataEle.addInt(0, 1);
        if(order > 0) {
            dataEle.packInt(samples, sampleSize, start, skip, order);
        }
        dataEle.addInt(precision-1, 4);
        dataEle.addInt(shift, 5);
        //System.err.println("shift:order:type::"+shift+":"+order+":"+encodedType);
        for(int i = 1; i <= order; i++) {
            int val = (int)-coeff[i];
            dataEle.addInt(val, precision);
        }
    }

    /**
     * Quantize coefficients to integer values of the given precision, and
     * calculate the shift needed.
     * @param coefficients values to quantize. These values will not be changed.
     * @param dest   destination for quantized values.
     * @param order number of values to quantize. First value skipped, coefficients
     *              array must be at least order+1 in length.
     * @param precision number of signed bits to use for coefficients
     * @return
     */
    private static int quantizeCoefficients(double[] coefficients, int[] dest,
            int order, int precision) {
        int shiftApplied = 0;
        int maxValAllowed = 1<<(precision-1)-1;
        double maxVal = 0;
        for(int i = 1; i <= order; i++) {
            double temp = coefficients[i];
            if(temp < 0) temp*= -1;
            if(temp > maxVal) 
                maxVal = temp;
        }
        //find shift to use(by max value)
        //for(shiftApplied = precision-1; shiftApplied > 0; shiftApplied--) {
        for(shiftApplied = 15; shiftApplied > 0; shiftApplied--) {
            int temp = (int)(maxVal * (1<<shiftApplied));
            if(temp <= maxValAllowed)
                break;
        }
        if(maxVal > maxValAllowed) {//no shift should have been applied
        //ensure max value is not too large, cap all necessary            //
            for(int i = 1; i <= order; i++) {
                double temp = coefficients[i];
                if(temp < 0)
                   temp = temp * -1;
                if(temp > maxValAllowed) {
                    //coefficients[i] = maxValAllowed;
                    dest[i] = maxValAllowed;
                }
                else
                    dest[i] = (int)coefficients[i];
                //System.err.println("Quantizing with new dest");
            }
        }
        else {
        //shift and quantize all values by found shift
            for(int i = 1; i <= order; i++) {
                //if(DEBUG_LEV > 20)
                //    System.err.print("i:old:new::"+i+":"+coefficients[i]);
                //coefficients[i] = (int)(coefficients[i]*(1<<shiftApplied));
                double temp = coefficients[i]*(1<<shiftApplied);
                temp = (temp > 0) ? temp+0.5:temp-0.5;
                //dest[i] = (int)(coefficients[i]*(1<<shiftApplied));
                dest[i] = (int)temp;
                //if(DEBUG_LEV > 20)
                //    System.err.println(":"+coefficients[i]);
            }
        }
        return shiftApplied;
    }

    private static void writeLPC(int[] samples, int count, int start,
            int increment, EncodedElement ele, int frameSampleSize, int riceParam,
            int precision, int shift, int[] coeffs, int[] errors, int order,
            RiceEncoder rice) {

        writeHeadersAndData(ele, order, coeffs, precision, shift,
            samples, frameSampleSize, start, increment-1);
        int paramSize = (riceParam > 14) ? 5:4;
        boolean fiveBitParam = (paramSize < 5) ? false:true;
        RiceEncoder.beginResidual(fiveBitParam, (byte)0, ele);
        EncodedElement_32 temp = new EncodedElement_32(ele.data.length/4+1, ele.getUsableBits()%8);
        rice.encodeRicePartition(errors, order,1, count-order, temp,
                riceParam, fiveBitParam);
        ele.attachEnd(temp.convertToEncodedElement());

    }

    private static int getParam(int[] vals, int end, int start, int max) {
        long sum = 0;
        for(int i = start; i < end; i++) {
            int temp = vals[i];
            temp = (temp < 0) ? -temp:temp;
            sum += temp;
        }
        float mean = (float)sum/(float)(end-start);
        double temp = LOGE_2*(mean);
        if(temp < 1)
           temp = 0;
        else
           temp = Math.ceil(Math.log(temp)/LOGE_2);
        int param = (int)temp;
        param++;
        if(param < 0) {
           System.err.println("end:start:sum:mean "+end+":"+start+":"+sum+":"+mean);
            param = 1;
            System.err.println("param negative?");
            System.exit(0);
        }
        else if(param > max)
            param = max;
        return param;
    }

    private static int partialEncodeLPC(int[] samples, int count, int start,
            int increment, LPC lpc, Subframe_LPC lpcSubframe,
            int frameSampleSize) {
        //System.err.println("encodeLPC begin");
        int order = lpc.order;
        //double error = (lpc.rawError < 0) ? -lpc.rawError:lpc.rawError;
        double tempLowOrderBits = 0;

        //following commented out because the getParam() method appears to be
        //more accurate for high-order lpc's, causing the search to end sooner
        //and resulting in smaller files. win-win. On second thought, that can't
        //be why it's quicker. The profile is showing *more* invocatiosn of this
        //function rather than fewer(by 3000!), which means it's something else
        //that's causing it to be quicker....strange.
        /*double deviation = Math.sqrt((int)error/count);
        double tempBits = LOGE_2*deviation/SQRT_2;
        tempLowOrderBits = (Math.ceil(Math.log(tempBits)/LOGE_2));
        if(java.lang.Double.isNaN(tempLowOrderBits)) {
            System.err.println("tempLowOrderBits is NaN");
            if(Double.isNaN(deviation))
                System.err.println("deviation is NaN");
            System.err.println("Error: "+(int)error/count);
            System.exit(0);
        }
        if(tempLowOrderBits < 1)
            tempLowOrderBits = 1;
        else if (tempLowOrderBits > frameSampleSize) {
            tempLowOrderBits = frameSampleSize;
        }*/
        int precision = 15;
        //calculate total estimated size of frame
        int headerSize = order*frameSampleSize+precision*order+9+8;
        int[] coeffs = lpcSubframe._quantizedCoeffs;
        int shift = quantizeCoefficients(lpc.rawCoefficients, coeffs, order, precision);
        //use integer coefficients to predict samples        
        //compare prediction to original, storing error.

        /** We save ~7% by accessing local vars instead of array in next loop */
        int coeff1 = coeffs[1];
        int coeff2 = coeffs[2];
        int coeff3 = coeffs[3];
        int coeff4 = coeffs[4];
        int coeff5 = coeffs[5];
        int coeff6 = coeffs[6];
        int coeff7 = coeffs[7];
        int coeff8 = coeffs[8];
        int coeff9 = coeffs[9];
        int coeff10 = coeffs[10];
        int coeff11 = coeffs[11];
        int coeff12 = coeffs[12];

        int baseIndex = start;
        int targetSampleBase = start+order*increment-increment;
        int tempOrder = order;
        for(int i = order; i < count; i++) {
            int temp = 0;            
            targetSampleBase += increment;
            int sampleIndex = baseIndex;
            baseIndex += increment;
          if(order > 12) {
            switch(order) {
                case 32: temp -= coeffs[32]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 31: temp -= coeffs[31]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 30: temp -= coeffs[30]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 29: temp -= coeffs[29]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 28: temp -= coeffs[28]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 27: temp -= coeffs[27]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 26: temp -= coeffs[26]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 25: temp -= coeffs[25]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 24: temp -= coeffs[24]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 23: temp -= coeffs[23]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 22: temp -= coeffs[22]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 21: temp -= coeffs[21]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 20: temp -= coeffs[20]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 19: temp -= coeffs[19]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 18: temp -= coeffs[18]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 17: temp -= coeffs[17]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 16: temp -= coeffs[16]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 15: temp -= coeffs[15]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 14: temp -= coeffs[14]*samples[sampleIndex];
                         sampleIndex+=increment;
                case 13: temp -= coeffs[13]*samples[sampleIndex];
                         sampleIndex+=increment;
            }
            tempOrder = 12;
          } 
          switch(tempOrder) {
                case 12: temp -= coeff12*samples[sampleIndex];
                         sampleIndex+=increment;
                case 11: temp -= coeff11*samples[sampleIndex];
                         sampleIndex+=increment;
                case 10: temp -= coeff10*samples[sampleIndex];
                         sampleIndex+=increment;
                case 9: temp -= coeff9*samples[sampleIndex];
                         sampleIndex+=increment;
                case 8: temp -= coeff8*samples[sampleIndex];
                         sampleIndex+=increment;
                case 7: temp -= coeff7*samples[sampleIndex];
                         sampleIndex+=increment;
                case 6: temp -= coeff6*samples[sampleIndex];
                         sampleIndex+=increment;
                case 5: temp -= coeff5*samples[sampleIndex];
                         sampleIndex+=increment;
                case 4: temp -= coeff4*samples[sampleIndex];
                         sampleIndex+=increment;
                case 3: temp -= coeff3*samples[sampleIndex];
                         sampleIndex+=increment;
                case 2: temp -= coeff2*samples[sampleIndex];
                         sampleIndex+=increment;
                case 1: temp -= coeff1*samples[sampleIndex];
                         sampleIndex+=increment;break;
                default:
            }
            temp = temp >> shift;
            lpcSubframe._errors[i] = samples[targetSampleBase]-temp;
        }
        tempLowOrderBits = getParam(lpcSubframe._errors, count, order,frameSampleSize);
        int riceSize = RiceEncoder.calculateEncodeSize(lpcSubframe._errors,
                order, 1, count-order, (int)tempLowOrderBits);
        int totalSize = headerSize + riceSize;

        lpcSubframe._precision = precision;
        lpcSubframe._lowOrderBits = (int)tempLowOrderBits;
        lpcSubframe._shift = shift;
        lpcSubframe._totalBits = totalSize;

        return totalSize;
    }

}
