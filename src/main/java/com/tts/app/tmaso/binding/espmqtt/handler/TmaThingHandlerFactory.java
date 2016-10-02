/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding.espmqtt.handler;

import static com.tts.app.tmaso.binding.BindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Locale;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.tts.app.tmaso.binding.BindingConstants;
import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.device.TmaDeviceManagerImpl;

/**
 * The {@link TmaThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Markus Rathgeb - Add locale provider support
 */
public class TmaThingHandlerFactory extends BaseThingHandlerFactory {

    private class LocaleProviderHolder implements LocaleProvider {
        private LocaleProvider localeProvider;

        @Override
        public Locale getLocale() {
            return localeProvider.getLocale();
        }
    }

    private final LocaleProviderHolder localeProviderHolder = new LocaleProviderHolder();
    private DiscoveryServiceRegistry discoveryServiceRegistry;

    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        localeProviderHolder.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        localeProviderHolder.localeProvider = null;
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String deviceUid = thing.getUID().getId();
        if (BindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            TmaDeviceManager deviceManager = TmaDeviceManagerImpl.getInstance();
            TmaThingHandler thingHandler = new TmaThingHandler(thing, localeProviderHolder, deviceManager,
                    discoveryServiceRegistry);
            deviceManager.manageThingHandler(deviceUid, thingHandler);
            return thingHandler;
        }

        return null;
    }
}
