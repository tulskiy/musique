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
 * This class is used to calculate LPC Coefficients for a FLAC stream.
 *
 * @author Preston Lacey
 */
public class LPC {
    /** The error calculated by the LPC algorithm */
    protected double rawError;
    /** The coefficients as calculated by the LPC algorithm */
    protected double[] rawCoefficients;
    /** The order of this LPC calculation */
    protected int order;

    /**
     * Constructor creates an LPC object of the given order.
     * @param order Order for this LPC calculation.
     */
    public LPC(int order) {
        this.order = order;
        rawError = 0;
        rawCoefficients = null;
    }
    /**
     * Get this LPC object's order
     * @return order used for this LPC calculation.
     */
    public int getOrder () { return order; }
    /**
     * Get the error for this LPC calculation
     * @return lpc error
     */
    public double getError() { return rawError; }

    /**
     * Get the calculated LPC Coefficients as an array.
     * @return lpc coefficients in an array.
     */
    public double[] getCoefficients() { return rawCoefficients; }

    /**
     * Calculate an LPC using the given Auto-correlation data. Static method
     * used since this is slightly faster than a more strictly object-oriented
     * approach.
     * 
     * @param lpc LPC to calculate
     * @param R Autocorrelation data to use
     */
    public static void calculate(LPC lpc, long[] R) {
        //calculate R(autocorrelation coefficients)
        int coeffCount = lpc.order;

        //calculate first iteration directly
        double[] A = new double[coeffCount+1];
        A[0] = 1;
        double E = R[0];

        //calculate remaining iterations

        if(R[0] == 0) {
            for(int i = 0; i < coeffCount+1; i++)
                A[i] = 0.0;
        }
        else {
            double[] ATemp = new double[coeffCount+1];
            for(int k = 0; k < coeffCount; k++) {
                double lambda = 0.0;
                double temp = 0;
                for(int j = 0; j <= k; j++) {
                    temp += A[j]*R[k+1-j];
                }
                lambda = -temp/E;
                //lambda = temp;

                for(int i = 0; i <= k+1; i++) {//verified good?
                    ATemp[i] = A[i]+lambda*A[k+1-i];
                }
                System.arraycopy(ATemp, 0, A, 0, coeffCount+1);
                E = (1-lambda*lambda)*E;
            }
        }
        lpc.rawCoefficients = A;
        lpc.rawError = E;
    }

    /**
     * Calculate an LPC using a prior order LPC's values to save calculations.
     * 
     * @param lpc LPC to calculate
     * @param R Auto-correlation data to use.
     * @param priorLPC Prior order LPC to use(may be any order lower than our
     * target LPC)
     * 
     */
    public static void calculateFromPrior(LPC lpc, long[] R, LPC priorLPC) {
        //calculate R(autocorrelation coefficients)
        int coeffCount = lpc.order;

        //calculate first iteration directly
        double[] A = new double[coeffCount+1];
        A[0] = 1;
        //A[1] = -R[1]/R[0];
        //double E = R[0]+R[1]*A[1];
        double E = R[0];
        int startIter = 0;
        if(priorLPC != null && priorLPC.order < lpc.order) {
            startIter = priorLPC.order;
            E = priorLPC.rawError;
            System.arraycopy(priorLPC.rawCoefficients, 0, A, 0, startIter+1);
        }
        //calculate remaining iterations
        if(R[0] == 0) {
            for(int i = 0; i < coeffCount+1; i++)
                A[i] = 0.0;
        }
        else {
            double[] ATemp = new double[coeffCount+1];
            for(int k = startIter; k < coeffCount; k++) {
                double lambda = 0.0;
                double temp = 0.0;
                for(int j = 0; j <= k; j++) {
                    temp -= A[j]*R[k-j+1];
                }
                lambda = temp/E;

                for(int i = 0; i <= k+1; i++) {
                    ATemp[i] = A[i]+lambda*A[k+1-i];
                }
                System.arraycopy(ATemp, 0, A, 0, coeffCount+1);
                E = (1-lambda*lambda)*E;
            }
        }
        lpc.rawCoefficients = A;
        lpc.rawError = E;
    }

    /**
     * Create auto-correlation coefficients(up to a maxOrder of 32).
     * @param R Array to put results in.
     * @param samples Samples to calculate the auto-correlation for.
     * @param count number of samples to use
     * @param start index of samples array to start at
     * @param increment number of indices to increment between valid samples(for
     * interleaved arrays)
     * @param maxOrder maximum order to calculate.
     */
    public static void createAutoCorrelation(long[] R, int []samples, int count,
            int start, int increment, int maxOrder) {
        //samples = window(samples,count,start,increment);
        //start = 0;
        //increment = 1;
        if(increment == 1) {
           for(int i = 0; i <= maxOrder; i++) {
              R[i] = 0;
              long temp = 0;
              int baseIndex = i;
              for(int j = 0; j < count-i; j++) {
                 temp += samples[j]*samples[j+baseIndex];
              }
              R[i] += temp;
           }
        }
        else {
           for(int i = 0; i <= maxOrder; i++) {
               R[i] = 0;
               int baseIndex = increment*i;
               long temp = 0;
               int innerLimit = (count-i)*increment;
               for(int j = start; j < innerLimit; j+=increment) {
                  temp += samples[j]*samples[j+baseIndex];
               }
               R[i] += temp;
           }
        }
    }

    /**
     * Apply a window function to sample data
     * @param samples Samples to apply window to. Values in this array are left
     * unaltered.
     * @param count number of samples to use
     * @param start index of samples array to start at
     * @param increment number of indices to increment between valid samples(for
     * interleaved arrays)
     * @param windowedSamples array containing windowed values. Return values
     * are packed(increment of one).
     * 
     */
    public static void window(int[] samples, int count, int start, int increment,
            int[] windowedSamples) {
       //int[] values = new int[samples.length];
       //int[] values = new int[count];
       int[] values = windowedSamples;
       int loopCount = 0;
       float halfway = count/2.0f;
       float windowCount = -halfway;
       int limit = count*increment+start;
       for(int i = start; i < limit; i+=increment) {
          float innerCount = (windowCount < 0) ? -windowCount:windowCount;
          windowCount++;
          //double val = 1.0-(double)(innerCount/halfway);
          float val = 1.0f-( (innerCount*innerCount)/(halfway*halfway) );
          double temp = ((double)samples[i])*val;
          temp = (temp >0) ? temp+0.5:temp-0.5;
          values[loopCount++] = (int)temp;
          //values[i] = (int)(((double)samples[i])*val);
       }
    }
}
