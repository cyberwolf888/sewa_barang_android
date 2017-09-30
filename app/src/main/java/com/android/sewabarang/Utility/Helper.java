package com.android.sewabarang.Utility;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Helper {

    public String formatNumber(Integer number){
        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        kursIndonesia.setDecimalFormatSymbols(formatRp);
        kursIndonesia.setMaximumFractionDigits(0);

        return kursIndonesia.format(number);
    }
}
