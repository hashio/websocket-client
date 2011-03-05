package jp.a840.push.subscriber.swing.util.comparator;

import java.lang.reflect.Method;
import java.util.Comparator;

public class ProductTimestampMethodComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        if (((Method) o1).getName().equals("getCurrencyPair")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getCurrencyPair")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getAsk")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getAsk")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getBid")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getBid")) {
            return 1;
        }

        int c;

        return 0;
    }
    
    private int commonPriceCompareTo(String regexp, Object o1, Object o2){
        if (((Method) o1).getName().matches(regexp)
        		&&
        		((Method) o2).getName().matches(regexp)){
        		return suffixCompareTo(o1, o2);
        }
        if (((Method) o1).getName().matches(regexp)) {
        		return -1;
        }
        if (((Method) o2).getName().matches(regexp)) {
            return 1;
        }
        return 0;
    }
    
    private int suffixCompareTo(Object o1, Object o2){
        if (((Method) o1).getName().matches("get.*Ask")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Ask")) {
            return 1;
        }
        if (((Method) o1).getName().matches("get.*Bid")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Bid")) {
            return 1;
        }
        if (((Method) o1).getName().matches("get.*Time")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Time")) {
            return 1;
        }
        return 0;
    }
}
