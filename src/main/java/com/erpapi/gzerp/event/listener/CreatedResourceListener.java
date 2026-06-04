package com.erpapi.gzerp.event.listener;

import com.erpapi.gzerp.event.ResourceCreatedEvent;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Component
public class CreatedResourceListener implements ApplicationListener<ResourceCreatedEvent> {

    @Override
    public void onApplicationEvent(ResourceCreatedEvent event) {
        HttpServletResponse response = event.getResponse();
        Long codigo = event.getCodigo();

        addHeaderLocation(codigo, response);
    }

    private static void addHeaderLocation(Long codigo, HttpServletResponse response) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{code}").
                buildAndExpand(codigo).toUri();
        response.setHeader("Location", uri.toASCIIString());
    }
}
