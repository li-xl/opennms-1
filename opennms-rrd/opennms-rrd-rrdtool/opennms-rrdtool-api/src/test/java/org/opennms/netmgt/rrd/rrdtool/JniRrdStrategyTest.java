/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Jun 23: Add comments about why the tests are disabled and eliminate
 *              warnings. - dj@opennms.org
 * 2007 Mar 19: Add a test for creating a graph.  Doesn't seem to work yet, though. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.rrd.rrdtool;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.util.StringUtils;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit tests for the JniRrdStrategy.  This requires that the shared object
 * for JNI rrdtool support can be found and linked (see findJrrdLibrary).
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@TestExecutionListeners({})
@RunWith(SpringJUnit4ClassRunner.class)
public class JniRrdStrategyTest {
    
    private JniRrdStrategy m_strategy;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        String rrdLib = System.getProperty("opennms.library.jrrd");
        if (rrdLib != null && !rrdLib.equals("${opennms.library.jrrd}")) {
            File libFile = new File(rrdLib);
            if (libFile.exists()) {
                m_strategy = new JniRrdStrategy();
                m_strategy.initialize();
            } else {
                throw new FileNotFoundException(rrdLib + " does not exist");
            }
        } else {
            System.err.println("System property 'opennms.library.jrrd' not set: skipping tests");
        }
    }

    @Test
    public void testInitialize() {
    }

    @Test
    public void testGraph() throws Exception {
        if (m_strategy != null) {
            String rrdtoolBin = System.getProperty("install.rrdtool.bin");
            if (rrdtoolBin != null) {
                File rrdtoolFile = new File(rrdtoolBin);
                if (!rrdtoolFile.exists()) {
                    System.err.println(rrdtoolBin + " does not exist");
                    return;
                }
            } else {
                System.err.println("System property 'install.rrdtool.bin' not set: skipping test");
                return;
            }

            long end = System.currentTimeMillis();
            long start = end - (24 * 60 * 60 * 1000);
            String[] command = new String[] {
                    rrdtoolBin,
                    "graph", 
                    "-",
                    "--start=" + start,
                    "--end=" + end,
                    "COMMENT:test"
            };
            
            m_strategy.createGraph(StringUtils.arrayToDelimitedString(command, " "), (new File(rrdtoolBin)).getParentFile());
        }
    }
}
