package org.apache.dubbo.rpc.cluster.configurator.absent;

import org.apache.dubbo.common.url;
import org.apache.dubbo.rpc.cluster.configurator;
import org.apache.dubbo.rpc.cluster.configuratorfactory;

public class AbsentConfiguratorFactory implements ConfiguratorFactory {

    public Configurator getConfigurator(Url url) {
        return new AbsentConfigurator(url);
    }
}
