/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.util;

import static java.util.Collections.*;
import static och.util.json.GsonUtil.*;
import static och.util.log.LogUtil.*;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import och.util.exception.BreakLoopException;
import och.util.exception.ContinueLoopException;
import och.util.model.CallableVoid;
import och.util.model.Pair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Util {
	
	public static final String UTF8 = "UTF8";
	
	public static Log getLog(Class<?> clazz){
		return getAsyncLogIfNeed(LogFactory.getLog(clazz));
	}
	
	public static Log getLog(String name){
		return getAsyncLogIfNeed(LogFactory.getLog(name));
	}
	
	public static boolean equalsOrNull(Object a, Object b){
		if(a == null && b == null) return true;
		if(a != null && a.equals(b)) return true;
		return false;
	}
	
	public static boolean isEmpty(Object o) {
		return o == null;
	}

	public static boolean isEmpty(Collection<?> col) {
		return col == null || col.size() == 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.size() == 0;
	}

	public static boolean isEmpty(Object[] arr) {
		return arr == null || arr.length == 0;
	}
	
	public static boolean hasText(String str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
	
    public static boolean hasLength(String str) {
        return (str != null && str.length() > 0);
    }
	
	@SafeVarargs
	public static <T> ArrayList<T> list(T... elems) {

		if(elems == null){
			return null;
		}
		
		ArrayList<T> list = new ArrayList<T>(elems.length);
		for (T elem : elems) {
			list.add(elem);
		}
		return list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T[] array(Collection collection, Class<T> type){
		if(collection == null){
			return null;
		}
		T[] array = (T[])Array.newInstance(type, collection.size());
		return (T[])collection.toArray(array);
	}

	public static <T> ArrayList<T> toList(Collection<T> collection) {

		if (collection == null) {
			return null;
		}

		ArrayList<T> out = null;
		if (collection instanceof ArrayList<?>) {
			out = (ArrayList<T>) collection;
		} else {
			out = new ArrayList<T>(collection);
		}
		return out;
	}
	
	public static <T> ArrayList<T> toList(Enumeration<T> enumeration) {
		if (enumeration == null) {
			return null;
		}
		ArrayList<T> out = new ArrayList<>();
		while(enumeration.hasMoreElements()){
			out.add(enumeration.nextElement());
		}
		return out;
	}
	
	
	public static <T> List<T> subList(List<T> list, int fromIndex){
		if(isEmpty(list)) return list;
		if(fromIndex < 0) fromIndex = 0;
		int size = list.size();
		if(fromIndex >= size) return emptyList();
		return list.subList(fromIndex, size);
	}
	
	public static <K, V> HashMap<K, V> map(){
		return new HashMap<K, V>();
	}
	
	public static <K, V> HashMap<K, V> map(K key, V val){
		HashMap<K, V> out = new HashMap<>();
		out.put(key, val);
		return out;
	}
	
	public static <K, V> HashMap<K, V> map(K key1, V val1, K key2, V val2){
		HashMap<K, V> out = new HashMap<>();
		out.put(key1, val1);
		out.put(key2, val2);
		return out;
	}
	
	public static <K, V> HashMap<K, V> map(K key1, V val1, K key2, V val2, K key3, V val3){
		HashMap<K, V> out = new HashMap<>();
		out.put(key1, val1);
		out.put(key2, val2);
		out.put(key3, val3);
		return out;
	}
	
	@SafeVarargs
	public static <V> HashSet<V> set(V... vals){
		
		HashSet<V> out = new HashSet<>();
		for (V val : vals) {
			out.add(val);
		}
		return out;
	}
	
	
	public static Integer tryParseInt(Object obj, Integer defaultVal){
		if(obj == null) return defaultVal;
		if(obj instanceof Integer) return (Integer) obj;
		try {
			String val = obj.toString();
			return Integer.parseInt(val);
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	
	public static Long tryParseLong(Object obj, Long defaultVal){
		if(obj == null) return defaultVal;
		if(obj instanceof Long) return (Long) obj;
		try {
			String val = obj.toString();
			return Long.parseLong(val);
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static Double tryParseDouble(Object obj, Double defaultVal){
		if(obj == null) return defaultVal;
		if(obj instanceof Double) return (Double) obj;
		try {
			String val = obj.toString();
			return Double.parseDouble(val);
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static Boolean tryParseBool(Object obj, Boolean defaultVal){
		if(obj == null) return defaultVal;
		if(obj instanceof Boolean) return (Boolean) obj;
		try {
			String val = obj.toString();
			return Boolean.parseBoolean(val);
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static <T extends Enum<T>> T tryParseEnum(Object ob, Class<T> enumType){
		return tryParseEnum(ob, enumType, null);
	}
	
	public static <T extends Enum<T>> T tryParseEnum(Object ob, Class<T> enumType, T defaultVal){
		if(ob == null) return defaultVal;
		try {
			return (T)Enum.valueOf(enumType, String.valueOf(ob));
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static <T extends Enum<T>> T tryGetEnumByCode(Integer code, Class<T> enumType, T defaultVal){
		if(code == null) return defaultVal;
		try {
			for (T c : enumType.getEnumConstants()) {
				int curCode = ((och.util.model.HasIntCode)c).getCode();
				if(curCode == code)return c;
			}
			return defaultVal;
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static Date tryParseDate(String date, String format, Date defaultDate){
		return DateUtil.tryParseDate(date, format, defaultDate);
	}
	
	
	public static BigDecimal tryParseBigDecimal(String val, BigDecimal defaultVal){
		if(val == null) return defaultVal;
		try {
			return new BigDecimal(val);
		}catch (Exception e) {
			return defaultVal;
		}
	}
	
	public static String toJson(Object ob){
		return toJson(ob, false);
	}
	
	public static String toJson(Object ob, boolean prettyPrinting){
		return ! prettyPrinting ? defaultGson.toJson(ob) : defaultGsonPrettyPrinting.toJson(ob);
	}
	
	public static <T> T tryParseJson(String val, Class<T> type){
		return tryParseJson(val, type, null);
	}
	
	public static <T> T tryParseJson(String val, Class<T> type, T defObj){
		if(val == null) return defObj;
		try {
			return defaultGson.fromJson(val, type);
		}catch (Exception e) {
			return defObj;
		}
	}
	
	
    public static String toObjectString(Object ob) {
        return ob.getClass().getName() + "@" + Integer.toHexString(ob.hashCode());
    }
    
	public static void checkArgumentForEmpty(Object ob, String argMsg) throws IllegalArgumentException {
		if (isEmpty(ob)) 
			throw new IllegalArgumentException("arg is empty: "+argMsg);
	}

	public static void checkArgument(boolean state, String errorMsg)
			throws IllegalStateException {
		if (!state) 
			throw new IllegalArgumentException(errorMsg);
	}

	public static void checkState(boolean state, String errorMsg)
			throws IllegalStateException {
		if (!state) 
			throw new IllegalStateException(errorMsg);
	}
	
	public static void checkStateForEmpty(Object ob, String argMsg) throws IllegalStateException {
		if (isEmpty(ob)) 
			throw new IllegalStateException("arg is empty: "+argMsg);
	}
	
	
	public static String randomUUID() {
		UUID uuid = UUID.randomUUID();
		String out = uuid.toString();
		return out.toString();
	}
	
	public static String randomSimpleId(){
		return System.currentTimeMillis()+"-"+System.nanoTime();
	}
	
	public static boolean[] array(boolean... elems) {
		return elems;
	}
	
    public static int compareTo(long a, long b) {
		return (a<b ? -1 : (a==b ? 0 : 1));
    }
    
    public static int compareTo(int a, int b) {
		return (a<b ? -1 : (a==b ? 0 : 1));
    }
    
	public static String getDeltaTime(long start) {
		long stop = System.currentTimeMillis();
		long delta = Math.abs(stop - start);
		String deltaStr = delta>1000 ? ""+(delta/1000.)+"sec" : ""+delta+"ms";
		return deltaStr;
	}
	
	public static void assertHasText(String str){
		if(!hasText(str)) throw new IllegalArgumentException("no text in str");
	}
	
	
	public static interface ItemConverter<I, O> {
		O covert(I item) throws ContinueLoopException, BreakLoopException;
	}
	
	public static <I, O> List<O> convert(Collection<I> coll, ItemConverter<I, O> itemConverter){
		
		if(isEmpty(coll)) return list();
		
		ArrayList<O> out = new ArrayList<>(coll.size());
		for (I obj : coll){
			try {
				O item = itemConverter.covert(obj);
				out.add(item);
			}catch(ContinueLoopException e){
				continue;
			}catch (BreakLoopException e) {
				break;
			}
			
		}
		return out;
	}
	
	public static interface FindPredicat<I> {
		boolean isValid(I item) throws BreakLoopException;
	}
	
	public static <I> I find(Collection<I> coll, FindPredicat<I> predicat){
		
		if(isEmpty(coll)) return null;
		
		for (I item : coll){
			try {
				if(predicat.isValid(item)) return item;
			}catch (BreakLoopException e) {
				break;
			}
		}
		return null;
	}
	
	
	public static <I, K, V> Map<K, V> toMap(Collection<I> coll, ItemConverter<I, Pair<K, V>> converter){
		if(isEmpty(coll)) return map();
		HashMap<K, V> out = new HashMap<>();
		for (I val : coll){
			Pair<K, V> pair = converter.covert(val);
			out.put(pair.first, pair.second);
		}
		return out;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void putToListMap(Map mapOfLists, Object key, Object val) {
		List list = (List<?>)mapOfLists.get(key);
		if(list == null){
			list = new ArrayList<>();
			mapOfLists.put(key, list);
		}
		list.add(val);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void putToSetMap(Map mapOfSets, Object key, Object val) {
		Set set = (Set<?>)mapOfSets.get(key);
		if(set == null){
			set = new HashSet<>();
			mapOfSets.put(key, set);
		}
		set.add(val);
	}
	
	public static <T> T lastFrom(List<T> list){
		return list.get(list.size()-1);
	}
	
	public static <T> T firstFrom(Collection<T> coll){
		if(isEmpty(coll)) return null;
		return coll.iterator().next();
	}

	
	public static <T> T inLock(Lock lock, Callable<T> body) throws Exception{
		lock.lock();
		try {
			return body.call();
		}finally {
			lock.unlock();
		}
	}
	
	public static void inLock(Lock lock, CallableVoid body) throws Exception{
		lock.lock();
		try {
			body.call();
		}finally {
			lock.unlock();
		}
	}
	
	
	public static interface DuplicateProvider<T> {
		
		boolean isDuplicates(T a, T b);
		
		int findBestFrom(List<T> duplicates);
		
	}
	
	/** 
	 * пример: 
	 * <br>
	 * дубликаты по равенству без учета заглавной буквы, лучше те, что заглавной:
	 * <br> 
	 * ["a", "a", "b", "A", "c", "B"] -> ["A", "B", "c"]
	 */
	public static <T> List<T> filterByBestFromDuplicates(List<T> list, DuplicateProvider<T> provider){
		
		if(isEmpty(list)) return list;
        
        ArrayList<T> filtered = new ArrayList<>();
        
        HashSet<Integer> processedIndexes = new HashSet<>();
        
        int size = list.size();
		for (int i = 0; i < size; i++) {
			
			//уже был обработан ранее
			if(processedIndexes.contains(i)) continue;
			
        	T cur = list.get(i);
            ArrayList<T> duplicates = null;
            
            //�?щем дубликаты
            for (int j = i+1; j < size; j++) {
            	T other = list.get(j);
            	//нашли дубликат - заносим его в список дубликтов, помечаем глобально обработанным
                if (provider.isDuplicates(cur, other)) {
                	if(duplicates == null) duplicates = new ArrayList<>();
                	duplicates.add(other);
                	processedIndexes.add(j);
                }
            }
            
            //похожих не найдено - точно валидный
            if(duplicates == null){
            	filtered.add(cur);
            	continue;
            } 
            
            //получаем финальный список дубликатов
            duplicates.add(0, cur);
            
            
            //ищем среди них лучший
            int bestIndex = provider.findBestFrom(new ArrayList<>(duplicates));
            if(bestIndex == -1 || bestIndex > duplicates.size() - 1) continue;
            
            T best = duplicates.get(bestIndex);
			filtered.add(best);
		}
        
        
        return filtered;
	}
	
	
	
	public static boolean isUpdateNotEmptyVal(String oldVal, String newVal){
		if( oldVal == null) return newVal != null;
		return hasText(newVal) && ! oldVal.equals(newVal);
	}

}
