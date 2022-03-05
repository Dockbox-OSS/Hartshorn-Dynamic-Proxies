package org.dockbox.hartshorn.proxy;

import org.dockbox.hartshorn.element.TypeContext;
import org.dockbox.hartshorn.proxy.javassist.JavassistProxyFactory;
import org.dockbox.hartshorn.util.Exceptional;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleApplicationProxier implements ApplicationProxier {

    private final Set<ProxyLookup> proxyLookups = ConcurrentHashMap.newKeySet();

    @Override
    public <T> Exceptional<TypeContext<T>> real(final T instance) {
        if (instance instanceof Proxy) {
            return Exceptional.of(TypeContext.of(((Proxy) instance).manager().targetClass()));
        }
        return Exceptional.empty();
    }

    @Override
    public <T> Exceptional<ProxyManager<T>> manager(final T instance) {
        if (instance instanceof Proxy) {
            return Exceptional.of(((Proxy) instance).manager());
        }
        return null;
    }

    @Override
    public <D, T extends D> Exceptional<D> delegate(final TypeContext<D> type, final T instance) {
        if (instance instanceof Proxy) {
            final ProxyManager manager = ((Proxy) instance).manager();
            return manager.delegate(type.type());
        }
        return Exceptional.empty();
    }

    @Override
    public <T> StateAwareProxyFactory<T, ?> factory(final TypeContext<T> type) {
        return this.factory(type.type());
    }

    @Override
    public <T> StateAwareProxyFactory<T, ?> factory(final Class<T> type) {
        return new JavassistProxyFactory<>(type, this);
    }

    @Override
    public <T> Class<T> unproxy(final T instance) {
        for (final ProxyLookup lookup : this.proxyLookups) {
            if (lookup.isProxy(instance)) return lookup.unproxy(instance);
        }
        return instance != null ? (Class<T>) instance.getClass() : null;
    }

    @Override
    public boolean isProxy(final Object instance) {
        return this.proxyLookups.stream().anyMatch(lookup -> lookup.isProxy(instance));
    }

    @Override
    public boolean isProxy(final Class<?> candidate) {
        return this.proxyLookups.stream().anyMatch(lookup -> lookup.isProxy(candidate));
    }

    @Override
    public void registerProxyLookup(final ProxyLookup proxyLookup) {
        this.proxyLookups.add(proxyLookup);
    }
}
