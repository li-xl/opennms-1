//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Add the graph offets to the model object. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.graph.Graph;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.graph.GraphResults.GraphResultSet;
import org.opennms.web.svclayer.GraphResultsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultGraphResultsService implements GraphResultsService, InitializingBean {

    private ResourceDao m_resourceDao;
    
    private GraphDao m_graphDao;

    private NodeDao m_nodeDao;
    
    private RrdDao m_rrdDao;

    private RelativeTimePeriod[] m_periods;

    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    public GraphResults findResults(String[] resourceIds,
            String[] reports, long start, long end, String relativeTime) {
        if (resourceIds == null) {
            throw new IllegalArgumentException("resourceIds argument cannot be null");
        }
        if (reports == null) {
            throw new IllegalArgumentException("reports argument cannot be null");
        }
        if (end < start) {
            throw new IllegalArgumentException("end time cannot be before start time");
        }

        GraphResults graphResults = new GraphResults();
        graphResults.setStart(new Date(start));
        graphResults.setEnd(new Date(end));
        graphResults.setRelativeTime(relativeTime);
        graphResults.setRelativeTimePeriods(m_periods);
        graphResults.setReports(reports);

        for (String resourceId : resourceIds) {
            graphResults.addGraphResultSet(createGraphResultSet(resourceId, reports, graphResults));
        }
        
        graphResults.setGraphTopOffsetWithText(m_rrdDao.getGraphTopOffsetWithText());
        graphResults.setGraphRightOffset(m_rrdDao.setGraphRightOffset());
        
        return graphResults;
    }
    
    public GraphResultSet createGraphResultSet(String resourceId, String[] reports, GraphResults graphResults) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        GraphResultSet rs = graphResults.new GraphResultSet();
        rs.setResource(resource);
        
        if (reports.length == 1 && "all".equals(reports[0])) {
            PrefabGraph[] queries = m_graphDao.getPrefabGraphsForResource(resource);
            List<String> queryNames = new ArrayList<String>(queries.length);
            for (PrefabGraph query : queries) {
                queryNames.add(query.getName());
            }

            reports = queryNames.toArray(new String[queryNames.size()]);
        }

        List<Graph> graphs = new ArrayList<Graph>(reports.length);

        for (String report : reports) {
            PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
            graphs.add(new Graph(prefabGraph, resource, graphResults.getStart(), graphResults.getEnd()));
        }

        /*
         * Sort the graphs by their order in the properties file.
         * PrefabGraph implements the Comparable interface.
         */
        Collections.sort(graphs);

        rs.setGraphs(graphs);
        
        return rs;
    }

    public void afterPropertiesSet() {
        Assert.state(m_nodeDao != null, "nodeDao property has not been set");
        Assert.state(m_resourceDao != null, "resourceDao property has not been set");
        Assert.state(m_graphDao != null, "graphDao property has not been set");
        Assert.state(m_rrdDao != null, "rrdDao property has not been set");
    }

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }
}
