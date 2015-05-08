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
package och.util.geoip;

import static och.util.FileUtil.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import test.BaseTest;

public class GeoIpTest extends BaseTest {
	
	@Before
	public void before() throws IOException{
		File from = new File("./components/geo-ip/extra/GeoIP-db.zip");
		File to = new File(TEST_DIR, "GeoIP-db.zip");
		copyFile(from, to);
	}
	
	@Test
	public void test_getCountry(){
		
		GeoIp geoIp = new GeoIp(TEST_PATH);
		
		//ip4
		assertEquals("Italy", geoIp.getCountry("151.38.39.114"));
		assertEquals("United States", geoIp.getCountry("12.25.205.51"));
		assertEquals("United States", geoIp.getCountry("64.81.104.131"));
		assertEquals("Colombia", geoIp.getCountry("200.21.225.82"));
		
		//ip6
		assertEquals("Italy", geoIp.getCountry("::151.38.39.114"));
		assertEquals("Italy", geoIp.getCountry("::ffff:151.38.39.114"));
		assertEquals("United States", geoIp.getCountry("2001:4860:0:1001::68"));
		
		//locals
		assertEquals(null, geoIp.getCountry("127.0.0.1"));
		assertEquals(null, geoIp.getCountry("192.168.21.1"));
		assertEquals(null, geoIp.getCountry("::127.0.0.1"));
		
	}

}
