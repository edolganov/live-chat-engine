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
package heap;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import och.junit.AssertExt;

import org.junit.Test;

public class InetAddressTest extends AssertExt {
	
	@Test
	public void test_ip4_ip6() throws UnknownHostException{
		
		InetAddress ip4 = InetAddress.getByName("151.38.39.114");
		InetAddress ip4FromIp6 = InetAddress.getByName("::ffff:151.38.39.114");
		InetAddress ip6 = InetAddress.getByName("2001:4860:0:1001::68");
		
		assertTrue(ip4 instanceof Inet4Address);
		assertTrue(ip4FromIp6 instanceof Inet4Address);
		assertTrue(ip6 instanceof Inet6Address);
		
		assertEquals("151.38.39.114", ip4.getHostAddress());
		assertEquals("151.38.39.114", ip4FromIp6.getHostAddress());
		assertEquals("2001:4860:0:1001:0:0:0:68", ip6.getHostAddress());
		
	}

}
