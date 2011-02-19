/*
 * Created on 2005/08/10
 */
package jp.a840.push.subscriber.swing.util.comparator;

import java.lang.reflect.Method;
import java.util.Comparator;

public class ProductTimestampMethodComparator implements Comparator {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        if (((Method) o1).getName().equals("getTimestamp")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getTimestamp")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getRangeFrom")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getRangeFrom")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getRangeTo")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getRangeTo")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getWeeklyDate")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getWeeklyDate")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getMonthlyDate")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getMonthlyDate")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getCreateDate")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getCreateDate")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getSequence")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getSequence")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getProductCode")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getProductCode")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getMaturity")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getMaturity")) {
            return 1;
        }
        if (((Method) o1).getName().equals("getBasetsu")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getBasetsu")) {
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
        if (((Method) o1).getName().equals("getPrice")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getPrice")) {
            return 1;
        }

        int c;

        c = commonPriceCompareTo("getTemporary.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getContract.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getOpeningAsk1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getOpeningBid1.*", o1, o2);
        if(c != 0) return c;
        
        c = commonPriceCompareTo("getOpeningQutation1.*", o1, o2);
        if(c != 0) return c;
        
        c = commonPriceCompareTo("getOpeningPrice1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getOpeningAsk2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getOpeningBid2.*", o1, o2);
        if(c != 0) return c;
        
        c = commonPriceCompareTo("getOpeningQutation2.*", o1, o2);
        if(c != 0) return c;
        
        c = commonPriceCompareTo("getOpeningPrice2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getOpening.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getHigh.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getLow.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingAsk1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingBid1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingQuotation1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingPrice1.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingAsk2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingBid2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingQuotation2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosingPrice2.*", o1, o2);
        if(c != 0) return c;

        c = commonPriceCompareTo("getClosing.*", o1, o2);
        if(c != 0) return c;

        if (((Method) o1).getName().equals("getCrop")) {
            return -1;
        }
        if (((Method) o2).getName().equals("getCrop")) {
            return 1;
        }

        if (((Method) o1).getName().equals("getInterfaceVersion")) {
            return 1;
        }
        if (((Method) o2).getName().equals("getInterfaceVersion")) {
            return -1;
        }

        if (((Method) o1).getName().equals("getImplementVersion")) {
            return 1;
        }
        if (((Method) o2).getName().equals("getImplementVersion")) {
            return -1;
        }
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
        if (((Method) o1).getName().matches("get.*Quotation")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Quotation")) {
            return 1;
        }
        if (((Method) o1).getName().matches("get.*Price")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Price")) {
            return 1;
        }
        if (((Method) o1).getName().matches("get.*Price1")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Price1")) {
            return 1;
        }
        if (((Method) o1).getName().matches("get.*Price2")) {
            return -1;
        }
        if (((Method) o2).getName().matches("get.*Price2")) {
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
