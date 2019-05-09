/*
 * JBoss, Home of Professional Open Source Copyright 2006, JBoss Inc., and
 * individual contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package uy.gub.agesic.connector.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * A simple namespace context used for XPath expression evaluation.
 * 
 * @author <a href='mailto:kevin.conner@jboss.com'>Kevin Conner</a>
 */
public class XPathNamespaceContext implements NamespaceContext
{
    /**
     * The prefix to URI mapping.
     */
    private final HashMap<String, String> prefixToURI = new HashMap<String, String>() ;
    /**
     * The URI to prefix mapping.
     */
    private final HashMap<String, String> uriToPrefix = new HashMap<String, String>() ;
    
    /**
     * Get the namespace URI for the specified prefix.
     * @param The prefix.
     * @return The associated namespace URI.
     */
    public String getNamespaceURI(final String prefix)
    {
        return prefixToURI.get(prefix) ;
    }
    
    /**
     * Get the prefix for the specified namespace URI.
     * @param The namespace URI.
     * @return The associated prefix.
     */
    public String getPrefix(final String namespaceURI)
    {
        return uriToPrefix.get(namespaceURI) ;
    }
    
    /**
     * Get the prefixes for the specified namespace URI.
     * @param The namespace URI.
     * @return The iterator of the associated prefixes.
     */
    public Iterator<?> getPrefixes(String namespaceURI)
    {
        final String prefix = getPrefix(namespaceURI) ;
        if (prefix == null)
        {
            return Collections.EMPTY_SET.iterator() ;
        }
        else
        {
            return Arrays.asList(prefix).iterator() ;
        }
    }
    
    /**
     * Initialise the prefix/namespace URI mapping.
     * @param prefix The prefix.
     * @param uri The namespace URI.
     */
    public void setMapping(final String prefix, final String uri)
    {
        prefixToURI.put(prefix, uri) ;
        uriToPrefix.put(uri, prefix) ;
    }
}