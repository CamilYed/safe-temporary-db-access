package pl.pw.cyber.dbaccess.adapters.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;


class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        var factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        var filename = Objects.requireNonNull(resource.getResource().getFilename());
        var properties = Objects.requireNonNull(factory.getObject());
        return new PropertiesPropertySource(filename, properties);
    }
}
