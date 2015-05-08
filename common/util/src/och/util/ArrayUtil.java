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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class ArrayUtil {
	
	
	public static byte[] writeIntToArray(int v, byte[] target, int offset) {
		target[offset] = (byte)((v >>> 24) & 0xFF);
		target[offset+1] = (byte)((v >>> 16) & 0xFF);
		target[offset+2] = (byte)((v >>>  8) & 0xFF);
		target[offset+3] = (byte)((v >>>  0) & 0xFF);
		return target;
	}
	
	public static int getInt(byte[] bytes, int offset) {
	     return bytes[offset] << 24 
	    		 | (bytes[offset+1] & 0xFF) << 16 
	    		 | (bytes[offset+2] & 0xFF) << 8 
	    		 | (bytes[offset+3] & 0xFF);
	}
	
	public static byte[] getIntBytes(int val){
		return writeIntToArray(val, new byte[4], 0);
	}
	
	
	
	
	public static byte[] writeLongToArray(long v, byte[] target, int offset) {
		target[offset] = (byte)((v >>> 56));
		target[offset+1] = (byte)((v >>> 48));
		target[offset+2] = (byte)((v >>> 40));
		target[offset+3] = (byte)((v >>> 32));
		target[offset+4] = (byte)((v >>> 24));
		target[offset+5] = (byte)((v >>> 16));
		target[offset+6] = (byte)((v >>>  8));
		target[offset+7] = (byte)((v >>>  0));
		return target;
	}
	
	public static byte[] getLongBytes(long val){
		return writeLongToArray(val, new byte[8], 0);
	}
	
	public static long getLong(byte[] bytes) {
		return getLong(bytes, 0);
	}
	
	public static long getLong(byte[] bytes, int offset) {
        return (((long)bytes[offset] << 56) +
                ((long)(bytes[offset+1] & 255) << 48) +
                ((long)(bytes[offset+2] & 255) << 40) +
                ((long)(bytes[offset+3] & 255) << 32) +
                ((long)(bytes[offset+4] & 255) << 24) +
                ((bytes[offset+5] & 255) << 16) +
                ((bytes[offset+6] & 255) << 8) +
                ((bytes[offset+7] & 255) << 0));
	}
	
	
	
	public static void writeStrToArray(String str, byte[] target, int offset){
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		System.arraycopy(bytes, 0, target, offset, bytes.length);
	}
	
	public static String getStr(byte[] bytes, int offset, int strBytesCount){
		
		byte[] strBytes = new byte[strBytesCount];
		arrayCopy(bytes, offset, strBytes, 0, strBytesCount);
		
		String result = null;
		try {
			result = new String(strBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		int endIndex = result.indexOf('\0');
		if(endIndex > -1) result = result.substring(0, endIndex);
		return result;
	}
	
	
	
	public static byte[] arrayCopy(byte[] src, int srcOffset, byte[] target, int targetOffset, int length){
		System.arraycopy(src, srcOffset, target, targetOffset, length);
		return target;
	}
	
    public static byte[] copyFromSmallToBig(byte[] small, byte[] big, int bigOffset) {
        System.arraycopy(small, 0, big, bigOffset, small.length);
        return big;
    }
    
    public static <T> T[] copyFromSmallToBig(T[] small, T[] big, int bigOffset) {
        System.arraycopy(small, 0, big, bigOffset, small.length);
        return big;
    }
	
	
	
	
	public static byte[] convertStrToBytes(String str, int length){
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		byte[] result = Arrays.copyOf(bytes, length);
		return result;
	}
	
	
	public static Byte[] toObjectArray(byte[] bytes){
		Byte[] out = new Byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			out[i] = bytes[i];
		}
		return out;
	}
	
	public static byte[] toPrimitiveArray(Byte[] bytes){
		byte[] out = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			out[i] = bytes[i];
		}
		return out;
	}

}
