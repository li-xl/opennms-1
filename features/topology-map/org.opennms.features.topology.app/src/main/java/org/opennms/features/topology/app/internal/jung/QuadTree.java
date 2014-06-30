/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.jung;

import java.awt.geom.Point2D;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Point;

public class QuadTree<Value> {
    private Node<Value> m_root;
    
    public static interface Visitor<Value> {
        public boolean visitNode(Node<Value> n);
    }


    // helper node data type
    public static class Node<Value> {
        private static final int EQ = -1;
        private static final int NW = 0;
        private static final int NE = 1;
        private static final int SW = 2;
        private static final int SE = 3;

        private Point2D m_location;
        private Value m_value;
        private BoundingBox m_bounds;

        private Node<Value>[] m_nodes;
        
        private Point2D m_centerOfMass;
        private int m_charge;

        
        public Node(BoundingBox bounds) {
            this(null, null, bounds);
        }

        public Node(Point2D location, Value value, BoundingBox bounds) {
            setLocation(location);
            setValue(value);
            setBounds(bounds);
        }
        
        private void setBounds(BoundingBox bounds) {
            m_bounds = new BoundingBox(bounds);
        }
        
        private static Point2D clonePoint(Point2D pt) {
            return (Point2D)pt.clone();
        }
        
        public Point2D getLocation() {
            return clonePoint(m_location);
        }

        public void setLocation(Point2D location) {
            m_location = clonePoint(location);
        }
        
        public void setLocation(double x, double y) {
            m_centerOfMass = new Point2D.Double(x, y);
        }
        
        public Point2D getCenterOfMass() {
            return clonePoint(m_centerOfMass);
        }
        
        public void setCenterOfMass(Point2D location) {
            m_centerOfMass = clonePoint(location);
        }
        
        public void setCenterOfMass(double x, double y) {
            m_centerOfMass = new Point2D.Double(x, y);
        }
        
        public void setCharge(int charge) {
            m_charge = charge;
        }
        
        public int getCharge() {
            return m_charge;
        }
        
        public Value getValue() {
            return m_value;
        }

        public void setValue(Value value) {
            m_value = value;
        }
        
        
        public int getWidth() {
            return m_bounds.getWidth();
        }
        
        public boolean isLeaf() {
            return m_nodes == null;
        }
        
        public double getX() { return m_location.getX(); }
        public double getY() { return m_location.getY(); }
        
        private int getQuadrant(Point2D pt) {
            Point center = m_bounds.getCenter();
            if ( less(pt.getX(), center.getX()) &&  less(pt.getY(), center.getY())) return NW;
            if ( less(pt.getX(), center.getX()) && !less(pt.getY(), center.getY())) return SW;
            if (!less(pt.getX(), center.getX()) &&  less(pt.getY(), center.getY())) return NE;
            // !less && !less
            return SE;
        }
        
        private BoundingBox getChildBounds(int quadrant) {
            int x = m_bounds.getX();
            int y = m_bounds.getY();
            int halfW = m_bounds.getWidth()/2;
            int halfH = m_bounds.getHeight()/2;
            switch(quadrant) {
            case NW: return new BoundingBox(x, y, halfW, halfH);
            case SW: return new BoundingBox(x, y + halfH, halfW, halfH);
            case NE: return new BoundingBox(x + halfW, y, halfW, halfH);
            default: return new BoundingBox(x + halfW, y+halfH, halfW, halfH);
            }
        }
        
        private Node<Value> getChild(int quadrant) {
            if (m_nodes == null) {
                m_nodes = new Node[4];
            }
            if (m_nodes[quadrant] == null) {
                m_nodes[quadrant] = new Node<Value>(getChildBounds(quadrant));
            }
            return m_nodes[quadrant];
        }
        
        private Node<Value> getChild(Point2D pt) {
            return getChild(getQuadrant(pt));
        }
        
        private boolean less(double k1, double k2) { return k1 < k2; }
        
        void insert(Point2D pt, int charge, Value v) {
            if (m_value == null) {
                // set location and 
                setLocation(pt);
                setValue(v);
                setCenterOfMass(pt);
                setCharge(charge);
                
            } else {
                if (this.isLeaf()) {
                    // move current data into a child node
                    insertChild(getLocation(), m_charge, m_value);
                }
                
                // insert new child data and update charge and center of mass
                insertChild(pt, charge, v);
                int newCharge = m_charge+charge;
                double cx = (getX()*m_charge + pt.getX()) / newCharge;
                double cy = (getY()*m_charge + pt.getY()) / newCharge;
                setCenterOfMass(cx, cy);
                setLocation(cx, cy);
                m_charge = newCharge;

            }
        }

        private void insertChild(Point2D pt, int charge, Value v) {
            Node<Value> child = getChild(pt);
            child.insert(pt, charge, v);
        }

        public void visit(Visitor<Value> visitor) {
            if (!visitor.visitNode(this)) {
                if (!isLeaf()) {
                    for(int i = 0; i < m_nodes.length; i++) {
                        Node<Value> n = m_nodes[i];
                        if (n != null) {
                            n.visit(visitor);
                        }
                    }
                }
                
            }
        }

    }
    
    public QuadTree(BoundingBox bounds) {
        m_root = new Node<Value>(bounds);
    }


    /***********************************************************************
     *  Insert (x, y) into appropriate quadrant
     ***********************************************************************/
    public void insert(Point2D location, int charge, Value value) {
        m_root.insert(location, charge, value);
    }


    public void visit(Visitor<Value> visitor) {
        m_root.visit(visitor);
    }

}
