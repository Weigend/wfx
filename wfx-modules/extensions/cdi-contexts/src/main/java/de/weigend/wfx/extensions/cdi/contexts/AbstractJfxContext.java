/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
 * %%
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
 * #L%
 */
package de.weigend.wfx.extensions.cdi.contexts;

import de.weigend.wfx.extensions.cdi.contexts.api.JfxContext;
import de.weigend.wfx.lookup.Lookup;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.contexts.AbstractBoundContext;
import org.jboss.weld.contexts.beanstore.BoundBeanStore;
import org.jboss.weld.contexts.beanstore.MapBeanStore;
import org.jboss.weld.contexts.beanstore.NamingScheme;
import org.jboss.weld.contexts.beanstore.SimpleNamingScheme;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.weld.bean.BeanIdentifiers.forManagedBean;

/**
 * Abstract implementation for the {@link JfxContext}.
 * <p/>
 * A minimal functional example for a {@link JfxContext} is the {@link de.weigend.wfx.extensions.cdi.contexts.api.ViewContext}
 * with the implementation {@link de.weigend.wfx.extensions.cdi.contexts.view.ViewContextImpl}.
 *
 * @param <T> The type of the associated storage
 * @see de.weigend.wfx.extensions.cdi.contexts.api.JfxContext
 * @see de.weigend.wfx.extensions.cdi.contexts.api.ViewContext
 * @see de.weigend.wfx.extensions.cdi.contexts.view.ViewContextImpl
 */
public abstract class AbstractJfxContext<T> extends AbstractBoundContext<T> implements JfxContext<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJfxContext.class);
    private final NamingScheme namingScheme;

    private final Map<T, BoundBeanStore> beanStores = new ConcurrentHashMap<>();
    private final ThreadLocal<T> associatedStorage = new ThreadLocal<>();
    private final Map<Object, T> beanStorageIdentifier = new WeakHashMap<>();

    /**
     * Initialize a new jfx context.
     *
     * @param contextId the context id.
     */
    protected AbstractJfxContext(String contextId, String namingSchemePrefix) {
        super(contextId, false);
        Objects.requireNonNull(namingSchemePrefix, "namingSchemePrefix must not be null");
        this.namingScheme = new SimpleNamingScheme(namingSchemePrefix);
    }

    /**
     * Associate the context with the storage (for this thread). If {@code dissociateExisting} is true, a possible
     * associated context will be dissociated. Otherwise the context won't be associated and it returns false.
     *
     * @param storage            the external storage
     * @param dissociateExisting dissociate a possible pre associated context
     * @return true if the storage was attached, otherwise false
     */
    @Override
    public boolean associate(T storage, boolean dissociateExisting) {
        if (getBeanStore() != null && dissociateExisting) {
            dissociate(storage);
        }
        return associate(storage);
    }

    @Override
    public boolean associate(T storage) {
        if (getBeanStore() == null) {
            BoundBeanStore beanStore;
            if (!beanStores.containsKey(storage)) {
                beanStore = new MapBeanStore(namingScheme, new HashMap<>());
                beanStores.put(storage, beanStore);
            }
            else {
                beanStore = beanStores.get(storage);
            }
            associatedStorage.set(storage);
            setBeanStore(beanStore);
            activate();
            LOGGER.trace("Jfx Context id:{} associated. active:{},valid:{}", storage, isActive(), isValid());
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean dissociate(T storage) {
        associatedStorage.remove();
        boolean dissociate = super.dissociate(storage);
        LOGGER.trace("Jfx Context id:{} dissociated. active:{},valid:{}", storage, isActive(), isValid());
        return dissociate;
    }

    @Override
    protected void destroy() {
        if (Objects.equals(beanStores.get(associatedStorage.get()), getBeanStore())) {
            beanStores.remove(associatedStorage.get());
            LOGGER.trace("Jfx Context id:{} destroyed. active:{},valid:{}", associatedStorage.get(), isActive(), isValid());
            associatedStorage.remove();
        }
        super.destroy();
    }

    @Override
    public Set<T> getStorageIdentifier() {
        return Collections.unmodifiableSet(beanStores.keySet());
    }

    @Override
    public T getAssociatedStorage() {
        return associatedStorage.get();
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public T getStorageIdentifierFor(Object obj) {
        if (beanStorageIdentifier.containsKey(obj)) {
            return beanStorageIdentifier.get(obj);
        }
        BeanIdentifier beanIdentifier = getBeanIdentifier(obj.getClass());

        for (Map.Entry<T, BoundBeanStore> entry : beanStores.entrySet()) {
            ContextualInstance<Object> instance = entry.getValue().get(beanIdentifier);
            if (instance != null && instance.getInstance() == obj) {
                T key = entry.getKey();
                beanStorageIdentifier.put(obj, key);
                return key;
            }
        }
        return null;
    }

    /**
     * Get the bean identifier for the given class.
     *
     * @param clazz The class to get the bean identifier.
     * @return The bean identifier of the given class.
     */
    @SuppressWarnings("unchecked")
    private static BeanIdentifier getBeanIdentifier(Class clazz) {
        BeanManager beanManager = Lookup.lookup(BeanManager.class);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(clazz);
        if (annotatedType instanceof SlimAnnotatedType) {
            SlimAnnotatedType<AnnotatedTypeIdentifier> slimAnnotatedType = (SlimAnnotatedType<AnnotatedTypeIdentifier>) annotatedType;
            return new StringBeanIdentifier(forManagedBean(slimAnnotatedType.getIdentifier()));
        }
        return null;
    }
}
