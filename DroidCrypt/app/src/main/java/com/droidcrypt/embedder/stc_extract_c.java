package com.droidcrypt.embedder;

import android.util.Log;
import java.util.BitSet;
import com.droidcrypt.common;

/**
 * Created by arudra on 24/02/15.
 */
public class stc_extract_c
{

    int stc_extract( BitSet vector, int vectorlength, BitSet message, int syndromelength, int matrixheight)
    {
        int i, j, k, index, index2, base, height;

        byte [][] binmat = null;
        int[] matrices, widths;

        height = matrixheight;

        if(matrixheight > 31) {
            Log.e("Error", "Submatrix height must not exceed 31.");
            return -1;
        }

        {
            double invalpha;
            int shorter, longer, worm;
            int [][] columns = null;

            matrices = new int[syndromelength]; //(syndromelength * sizeof(int));
            widths = new int[syndromelength];

            invalpha = (double)vectorlength / syndromelength;
            if(invalpha < 1) {
                Log.e("Error", "The message cannot be longer than the cover object.");
                return -1;
            }
            shorter = (int)Math.floor(invalpha);
            longer = (int)Math.ceil(invalpha);
            common c = new common();
            if((columns[0] = c.getMatrix(shorter, matrixheight)) == null ) {
                return -1;
            }
            if((columns[1] = c.getMatrix(longer, matrixheight)) == null ) {
                return -1;
            }
            worm = 0;
            for(i = 0; i < syndromelength; i++) {
                if(worm + longer <= (i + 1) * invalpha + 0.5) {
                    matrices[i] = 1;
                    widths[i] = longer;
                    worm += longer;
                } else {
                    matrices[i] = 0;
                    widths[i] = shorter;
                    worm += shorter;
                }
            }
            binmat[0] = new byte [shorter * matrixheight];
            binmat[1] = new byte [longer * matrixheight];
            for(i = 0, index = 0; i < shorter; i++) {
                for(j = 0; j < matrixheight; j++, index++) {
                    binmat[0][index] = (byte)(columns[0][i] & (1 << j)) ;
                }
            }
            for(i = 0, index = 0; i < longer; i++) {
                for(j = 0; j < matrixheight; j++, index++) {
                    binmat[1][index] = (byte)(columns[1][i] & (1 << j)) ;
                }
            }
        }

        for(i = 0; i < syndromelength; i++) {
            message.clear(i);
        }

        byte tmp = 0;
        for(index = 0, index2 = 0; index2 < syndromelength; index2++) {
            for(k = 0, base = 0; k < widths[index2]; k++, index++, base += matrixheight) {
                if(vector.get(index)) {
                    for(i = 0; i < height; i++)
                    {
                        tmp = (byte)(message.get(index2 + i) ? 1 : 0);
                        tmp ^= binmat[matrices[index2]][base + i];

                        if (tmp== 0)
                            message.clear(index2 + i);
                        else
                            message.set(index2 + i);
                    }
                }
            }
            if(syndromelength - index2 <= matrixheight)
                height--;
        }

        return 0;
    }


}
