package jp.a840.push.subscriber.swing.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jp.a840.push.subscriber.swing.util.comparator.ProductTimestampMethodComparator;


public class SwingClientDefaultTableModelUtil {
	
	public static Method[] convertStringsToMethods(Class clazz, String[] methodNames){
		Method[] methods = new Method[methodNames.length];
		for(int i = 0; i < methods.length; i++){
			Method m = getMethod(clazz, methodNames[i]);
			if(m == null){
				throw new IllegalArgumentException("Not found method. methodName: "+ methodNames[i]);
			}
			methods[i] = m;
		}
		return methods;
	}
	
	public static Method getMethod(Class clazz, String methodName){
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if(methods[i].getName().equalsIgnoreCase(methodName)
					|| methods[i].getName().equalsIgnoreCase("get" + methodName)){
				return methods[i];
			}
		}
		return null;
	}
	
    public static Vector createHeader(Object obj){
    	return createHeader(obj.getClass());
    }
	
    public static Vector createHeader(Class clazz){
    	return createHeader(clazz.getMethods());
    }
	
    public static Vector createHeader(Method[] methods){
        Arrays.sort(methods, new ProductTimestampMethodComparator());
        Vector itemNames = new Vector();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            String getStr = methodName.substring(0, 3);
            String getValue = methodName.substring(3);
            if (getStr.equals("get")) {
                if (filter(getValue)) {
                    continue;
                }
                Class[] param = methods[i].getParameterTypes();
                if (param.length == 0) {
                    try {
                        itemNames.add(getValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return itemNames;
    }
    
    /**
     * getterメソッドをかき集める
     * 
     * @param obj
     * @return
     */
    public static Method[] compileData(Object obj){
    	return compileData(obj.getClass());
    }
    
    public static Method[] compileData(Class clazz){
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, new ProductTimestampMethodComparator());
        
        List methodList = new ArrayList();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            String getStr = methodName.substring(0, 3);
            String getValue = methodName.substring(3);
            if (getStr.equals("get")) {
                if (filter(getValue)) {
                    continue;
                }
                Class[] param = methods[i].getParameterTypes();
                if (param.length == 0) {
                    try {
                    	   methodList.add(methods[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Method[] compiledMethods = new Method[methodList.size()];
        methodList.toArray(compiledMethods);
        return compiledMethods;
    }
    
    public static Vector createData(Set set){
        Class clazz = set.getClass();
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, new ProductTimestampMethodComparator());
        
        Vector items = new Vector();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            String getStr = methodName.substring(0, 3);
            String getValue = methodName.substring(3);
            if (getStr.equals("get")) {
                if (filter(getValue)) {
                    continue;
                }
                Class[] param = methods[i].getParameterTypes();
                if (param.length == 0) {
                    try {
                        Object obj = methods[i].invoke(set, null);
                        items.add(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return items;
    }
        
    public static boolean filter(String str) {
        if (str.equalsIgnoreCase("InputPerson")) {
            return true;
        } else if (str.equalsIgnoreCase("UpdatePerson")) {
            return true;
        } else if (str.equalsIgnoreCase("ActiveFlag")) {
            return true;
        } else if (str.equalsIgnoreCase("UpdateDate")) {
            return true;
        } else if (str.equalsIgnoreCase("InputDate")) {
            return true;
        } else if (str.equalsIgnoreCase("SubsystemName")) {
            return true;
        } else if (str.equalsIgnoreCase("Class")) {
            return true;
        } else if (str.equalsIgnoreCase("FeederCode")) {
            return true;
        } else if (str.equalsIgnoreCase("FeederCodeEx")) {
            return true;
        } else if (str.equalsIgnoreCase("DatasetName")) {
            return true;
        } else if (str.equalsIgnoreCase("DatasetType")) {
            return true;
        }
        return false;
    }
}
