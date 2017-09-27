/*
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *      contributor license agreements.  See the NOTICE file distributed with
 *      this work for additional information regarding copyright ownership.
 *      The ASF licenses this file to You under the Apache License, Version 2.0
 *      (the "License"); you may not use this file except in compliance with
 *      the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.jboss.budapest.connector;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

/**
 * Implementation of ConnectionHandler interface. getServerLocator() and getServerLocator(String ipAddress)
 * provides the caller with a ServerLocator to access JMS objects. There are no plans to implement the latter method
 * at this stage.
 *
 * @author <a href="mailto:tw.techlist@gmail.com">Tyronne Wickramarathne</a>
 */

public class ConnectionHandlerImpl implements ConnectionHandler {

    private ServerLocator serverLocator;

    /**
     * This method returns a ServerLocator from the local JMS broker
     * @return
     */
    @Override
    public ServerLocator getServerLocator() {
        serverLocator = ActiveMQClient.createServerLocatorWithoutHA(new TransportConfiguration("InVMConnectionFactory")); //TODO The connection factory needs to be picked from the properties file
        return serverLocator;
    }


    /**
     * For future releases
     * @param ipAddress
     * @return
     */
    @Override
    public ServerLocator getServerLocator(String ipAddress) {
        return null;
    }


    /**
     *
     * @param serverLocator
     * @return
     * @throws Exception
     */
    @Override
    public ClientSession createClientSession(ServerLocator serverLocator) throws Exception {
        ClientSessionFactory clientSessionFactory = getServerLocator().createSessionFactory();
        try (ClientSession clientSession = clientSessionFactory.createSession(true, true, true)){
            return clientSession;
        }
    }


}
