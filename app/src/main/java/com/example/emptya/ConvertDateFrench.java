package com.example.emptya;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConvertDateFrench {

    public static String convertDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);

            Date date = inputFormat.parse(dateString);
            String formattedDate = outputFormat.format(date);

            return formattedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        String date = "2012-12-30T00:00:00.000Z";
        String formattedDate = convertDate(date);
        System.out.println("Formatted Date: " + formattedDate);
    }
}
