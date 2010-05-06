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
public class ProgressHelper {
    public ProgressHelper(int nTotalSteps, ProgressCallback ProgressCallback) {
        m_CallbackFunction = ProgressCallback;

        m_nTotalSteps = nTotalSteps;
        m_nCurrentStep = 0;
        m_nLastCallbackFiredPercentageDone = 0;

        UpdateProgress(0);
    }

    public void UpdateStatus(String msg) {
        m_CallbackFunction.updateStatus(msg);
    }

    public void UpdateProgress() {
        UpdateProgress(-1, false);
    }

    public void UpdateProgress(int nCurrentStep) {
        UpdateProgress(nCurrentStep, false);
    }

    public void UpdateProgress(int nCurrentStep, boolean bForceUpdate) {
        //update the step
        if (nCurrentStep == -1)
            m_nCurrentStep++;
        else
            m_nCurrentStep = nCurrentStep;

        //figure the percentage done
        float fPercentageDone = ((float) (m_nCurrentStep)) / ((float) (Math.max(m_nTotalSteps, 1)));
        int nPercentageDone = (int) (fPercentageDone * 1000 * 100);
        if (nPercentageDone > 100000) nPercentageDone = 100000;

        //fire the callback
        if (m_CallbackFunction != null) {
            m_CallbackFunction.pPercentageDone = nPercentageDone;
            if (bForceUpdate || (nPercentageDone - m_nLastCallbackFiredPercentageDone) >= 1000) {
                m_CallbackFunction.callback(nPercentageDone);
                m_nLastCallbackFiredPercentageDone = nPercentageDone;
            }
        }
    }

    public void UpdateProgressComplete() {
        UpdateProgress(m_nTotalSteps, true);
    }

    public boolean isKillFlag() {
        return m_CallbackFunction != null ? m_CallbackFunction.killFlag : false;
    }

    private ProgressCallback m_CallbackFunction = null;

    private int m_nTotalSteps;
    private int m_nCurrentStep;
    private int m_nLastCallbackFiredPercentageDone;
}
