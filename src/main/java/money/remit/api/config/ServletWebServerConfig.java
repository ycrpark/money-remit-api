package money.remit.api.config;

import java.net.UnknownHostException;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ServletWebServerConfig {
    @Bean
    public ConnectorCustomizer connectorCustomizer() {
        return new ConnectorCustomizer();
    }
    
    @Bean
    public ServletWebServerFactory servletWebServerFactory(ConnectorCustomizer connectorCustomizer)
            throws UnknownHostException {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connectorCustomizer);
        
        return factory;
    }
}
