package com.tts.app.tmaso.binding;

import org.osgi.service.component.ComponentContext;

public interface ComponentActivator {
    // Invoked automatically by Service Component Runtime
    void activate(ComponentContext context);

    // Invoked automatically by Service Component Runtime
    void deactivate(ComponentContext context);
}
