package com.adobe.acs.commons.wcm.impl;


import com.day.cq.commons.Externalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = PublishUrlServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_GET,
        extensions = PublishUrlServlet.TXT_EXTENSION
)
@Designate(ocd = PublishUrlServlet.PublishUrlServletConfig.class)
public class PublishUrlServlet extends SlingSafeMethodsServlet implements Serializable {

    private static final long serialVersionUID = 1L;
    protected static final String RESOURCE_TYPE = "acs-commons/components/utilities/publish-url";
    protected static final String TXT_EXTENSION = "txt";
    private static final String PATH = "path";
    private static final String JSON_TYPE = "application/json";
    private String[] externalizerKeys;

    @Activate
    protected void activate(final PublishUrlServletConfig config) {
        this.externalizerKeys = config.externalizerKeys();
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String path = request.getParameter(PATH);
        ResourceResolver resolver = request.getResourceResolver();
        Externalizer externalizer = resolver.adaptTo(Externalizer.class);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonResponse = mapper.createObjectNode();

        if (externalizer != null) {
            Arrays.asList(externalizerKeys).forEach(key -> {
                String capitalizedKey = StringUtils.capitalize(key);
                String externalLink = externalizer.externalLink(resolver, key, request.getScheme(), path);
                jsonResponse.put(capitalizedKey, externalLink);
            });
        }

        response.setContentType(JSON_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonResponse.toString());
    }

    @ObjectClassDefinition(name = "ACS AEM Commons - Publish URL Servlet Configuration",
            description = "Configuration for the Publish URL Servlet")
    public @interface PublishUrlServletConfig {

        @AttributeDefinition(name = "Externalizer Environment Keys", description = "Externalizer Environment Keys. They " +
                "need to match the environment keys configured in the Externalizer configuration.", type = AttributeType.STRING)
        String[] externalizerKeys() default {};
    }
}
