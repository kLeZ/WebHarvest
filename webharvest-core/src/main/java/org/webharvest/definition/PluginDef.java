package org.webharvest.definition;

import org.webharvest.runtime.processors.Processor;

public interface PluginDef {

    // FIXME rbala Can we use an interface type instead?
    Processor createPlugin();

}
