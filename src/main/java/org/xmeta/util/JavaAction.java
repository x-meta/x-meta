package org.xmeta.util;

import org.xmeta.ActionContext;

public interface JavaAction {
    Object run(ActionContext actionContext) throws Throwable;
}
