/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ivory.entity.store;

import org.apache.ivory.IvoryException;
import org.apache.ivory.entity.store.ConfigurationStore;
import org.apache.ivory.entity.v0.Entity;
import org.apache.ivory.entity.v0.EntityType;
import org.apache.ivory.transaction.Action;

public class ConfigurationStoreAction extends Action{
    private static final String ENTITY_TYPE_KEY = "entityType";
    private static final String ENTITY_NAME_KEY = "entityName";
    private static final String ENTITY_KEY = "entity";
    
    public static enum Action{
        PUBLISH, REMOVE, UPDATE, UPDATEINIT
    }
    
    protected ConfigurationStoreAction() {
        super();
    }
    
    public ConfigurationStoreAction(ConfigurationStoreAction.Action action, Entity entity) {
        super(action.name());
        if(action == Action.UPDATEINIT)
            return;
        
        Payload payload = new Payload(ENTITY_TYPE_KEY, entity.getEntityType().name());
        switch(action) {
            case PUBLISH:
                payload.add(ENTITY_NAME_KEY, entity.getName());
                break;
                
            case REMOVE:
                payload.add(ENTITY_KEY, entity.toString());
                break;
                
            case UPDATE:
                payload.add(ENTITY_KEY, entity.toString());
                break;
        }
        setPayload(payload);
    }

    @Override
    public void rollback() throws IvoryException {
        Action action = Action.valueOf(getCategory());
        switch (action) {
            case PUBLISH:
                EntityType entityType = EntityType.valueOf(getPayload().get(ENTITY_TYPE_KEY));
                ConfigurationStore.get().remove(entityType, getPayload().get(ENTITY_NAME_KEY));
                break;
                
            case REMOVE:
                entityType = EntityType.valueOf(getPayload().get(ENTITY_TYPE_KEY));
                ConfigurationStore.get().publish(entityType, Entity.fromString(entityType, getPayload().get(ENTITY_KEY)));
                break;
                
            case UPDATE:
                entityType = EntityType.valueOf(getPayload().get(ENTITY_TYPE_KEY));
                ConfigurationStore.get().rollbackUpdate(entityType, Entity.fromString(entityType, getPayload().get(ENTITY_KEY)));
                break;
                
            case UPDATEINIT:
                ConfigurationStore.get().cleanupUpdateInit();
                break;
        }
    }

    @Override
    public void commit() {
        Action action = Action.valueOf(getCategory());
        switch(action) {
            case UPDATEINIT:
                ConfigurationStore.get().cleanupUpdateInit();
                break;
                
        }
    }
}