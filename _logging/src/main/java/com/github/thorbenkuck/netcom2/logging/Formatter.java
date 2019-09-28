package com.github.thorbenkuck.netcom2.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {

    private static String toString(Object[] o, int index) {
        if(o == null) {
            return "null";
        }

        if(index >= o.length) {
            return "null";
        }

        return o[index].toString();
    }


    public static String format(String raw, Object[] array) {
        if (!raw.contains("{}") || array.length == 0) {
            return raw;
        }

        Pattern pattern = Pattern.compile("\\{}");
        Matcher matcher = pattern.matcher(raw);
        StringBuffer stringBuffer = new StringBuffer();
        int index = 0;

        while(matcher.find()) {
            matcher.appendReplacement(stringBuffer, toString(array, index++));
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

}
