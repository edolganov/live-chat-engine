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
package och.service.props;

import static och.util.Util.*;

import java.io.File;
import java.util.Set;

import och.api.exception.PropertyNotFoundException;
import och.service.props.impl.FileProps;
import och.service.props.impl.MapProps;
import och.service.props.impl.MultiProps;

import org.junit.Test;

import test.BaseTest;

@SuppressWarnings("rawtypes")
public class MultiPropsTest extends BaseTest {
	
	
	
	@Test
	public void test_update(){
		
		
		FileProps props1 = new FileProps(new File(TEST_DIR, "test1.props"));
		FileProps props2 = new FileProps(new File(TEST_DIR, "test2.props"));
		
		props1.putVal("key1", "1");
		props2.putVal("key2", "2");
		
		MultiProps multi = new MultiProps(props1);
		multi.addSource(props2);
		
		Set[] keys = {null};
		String[] curVal1 = {null};
		String[] curVal2 = {null};
		String[] curVal3 = {null};
		multi.addChangedListener((k)->{
			keys[0] = k;
			curVal1[0] = multi.getVal("key1");
			curVal2[0] = multi.getVal("key2");
			curVal3[0] = multi.getVal("key3");
		});
		
		//change in 1
		props1.putVal("key1", "11");
		assertEquals(set("key1"), keys[0]);
		assertEquals("11", curVal1[0]);
		assertEquals("2", curVal2[0]);
		
		
		//change in 2
		props2.putVal("key2", "22");
		assertEquals(set("key2"), keys[0]);
		assertEquals("11", curVal1[0]);
		assertEquals("22", curVal2[0]);
		
		
		//chagne in 2 but not set
		props2.putVal("key1", "1");
		assertEquals(set("key1"), keys[0]);
		assertEquals("11", curVal1[0]);
		assertEquals("22", curVal2[0]);
		
		props2.removeVal("key2");
		assertEquals(set("key2"), keys[0]);
		assertEquals("11", curVal1[0]);
		assertEquals(null, curVal2[0]);
		
		
		//reset
		MapProps props3 = new MapProps(map("key3", "3"));
		multi.resetSources(props3);
		assertEquals(set("key1", "key3"), keys[0]);
		assertEquals(null, curVal1[0]);
		assertEquals(null, curVal2[0]);
		assertEquals("3", curVal3[0]);
		
		//update after reset
		props3.putVal("key3", "33");
		assertEquals(set("key3"), keys[0]);
		assertEquals(null, curVal1[0]);
		assertEquals(null, curVal2[0]);
		assertEquals("33", curVal3[0]);
	}
	
	
	
	@Test
	public void test_simpe(){
		
		FileProps props1 = new FileProps(new File(TEST_DIR, "test1.props"));
		FileProps props2 = new FileProps(new File(TEST_DIR, "test2.props"));
		
		props1.putVal("key1", "1");
		props1.putVal("key2", "2");
		
		props2.putVal("key3", "3");
		props2.putVal("key4", "4");
		
		MultiProps multi = new MultiProps(props1, props2);
		
		//read
		{
			assertEquals("1", multi.findVal("key1"));
			assertEquals("2", multi.findVal("key2"));
			assertEquals("3", multi.findVal("key3"));
			assertEquals("4", multi.findVal("key4"));
			
			assertEquals(new Integer(1), multi.getVal("key1", -1));
			assertEquals(new Integer(2), multi.getVal("key2", -1));
			assertEquals(new Integer(3), multi.getVal("key3", -1));
			assertEquals(new Integer(4), multi.getVal("key4", -1));
			
			assertEquals(new Long(1), multi.getVal("key1", -1L));
			assertEquals(new Long(2), multi.getVal("key2", -1L));
			assertEquals(new Long(3), multi.getVal("key3", -1L));
			assertEquals(new Long(4), multi.getVal("key4", -1L));
		}
		
		//unknown
		{
			assertEquals(null, multi.getVal("key-unknown", (String)null));
			assertEquals("@", multi.getVal("key-unknown", "@"));
			
			try {
				multi.findVal("key-unknown");
				fail_exception_expected();
			}catch(PropertyNotFoundException e){
				//ok
			}
		}
		
		//source update
		{
			props1.putVal("key1", "11");
			assertEquals("11", multi.findVal("key1"));
		}
		
		//rewrite by order
		{
			props1.putVal("key3", 1);
			assertEquals("1", multi.findVal("key3"));
			assertEquals("1", multi.toMap().get("key3"));
			
			props1.removeVal("key3");
			assertEquals("3", multi.findVal("key3"));
		}
		
	}

}
