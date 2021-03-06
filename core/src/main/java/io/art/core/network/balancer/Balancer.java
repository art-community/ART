package io.art.core.network.balancer;

import java.util.Collection;

public interface Balancer<T> {

    T select();

    Collection<T> getEndpoints();

    void updateEndpoints(Collection<T> endpoints);

    enum BalancerMethod{
        ROUND_ROBIN
    }
}
