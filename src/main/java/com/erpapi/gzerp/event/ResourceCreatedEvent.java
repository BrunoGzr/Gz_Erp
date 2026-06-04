package com.erpapi.gzerp.event;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEvent;

public class ResourceCreatedEvent extends ApplicationEvent {

    private final HttpServletResponse response;
    private final Long codigo;


    public ResourceCreatedEvent(Object source, HttpServletResponse response, Long codigo) {
        super(source);
        this.response = response;
        this.codigo = codigo;

    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Long getCodigo() {
        return codigo;
    }
}
