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
 * 2007 Apr 05: Created this file. - dj@opennms.org
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
package org.opennms.netmgt.model;


/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class StringPropertyAttribute implements OnmsAttribute {
    private String m_name;
    private String m_value;
    private OnmsResource m_resource;

    /**
     * @param string
     * @param string2
     */
    public StringPropertyAttribute(String name, String value) {
        m_name = name;
        m_value = value;
    }

    /**
     * Get the name for this attribute.  This is the property name
     * from the properties file.
     * 
     * @see org.opennms.netmgt.model.OnmsAttribute#getName()
     */
    public String getName() {
        return m_name;
    }
    
    public String getValue() {
        return m_value;
    }

    /**
     * @see org.opennms.netmgt.model.OnmsAttribute#getResource()
     */
    public OnmsResource getResource() {
        return m_resource;
    }

    /**
     * @see org.opennms.netmgt.model.OnmsAttribute#setResource(org.opennms.netmgt.model.OnmsResource)
     */
    public void setResource(OnmsResource resource) {
        m_resource = resource;

    }

}
