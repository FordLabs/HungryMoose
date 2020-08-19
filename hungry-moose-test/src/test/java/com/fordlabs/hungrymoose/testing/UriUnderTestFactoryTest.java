/*
 * Copyright (c) 2020 Ford Motor Company
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fordlabs.hungrymoose.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class UriUnderTestFactoryTest {

    @Before
    @After
    public void cleanUp() {
        System.clearProperty("target.host");
        System.clearProperty("target.port");
        System.clearProperty("target.protocol");
    }

    @Test
    public void protocol_host_and_port_are_set_from_properties() {
        System.setProperty("target.host", "propertyHost");
        System.setProperty("target.port", "99");
        System.setProperty("target.protocol", "ssh");

        UriUnderTest uri = UriUnderTestFactory.getUriUnderTest();

        assertThat(uri.getHost(), equalTo("propertyHost"));
        assertThat(uri.getPort(), equalTo(99));
        assertThat(uri.getTransferProtocol(), equalTo("ssh"));
    }

    @Test
    public void gets_defaults() {
        UriUnderTest uri = UriUnderTestFactory.getUriUnderTest();

        assertThat(uri.getHost(), equalTo("localhost"));
        assertThat(uri.getPort(), not(0));
        assertThat(uri.getTransferProtocol(), equalTo("http"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_port_out_of_upper_range() {
        System.setProperty("target.port", "65536");
        UriUnderTestFactory.getUriUnderTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_port_out_of_lower_range() {
        System.setProperty("target.port", "-1");
        UriUnderTestFactory.getUriUnderTest();
    }
}
