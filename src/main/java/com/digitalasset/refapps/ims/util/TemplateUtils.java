/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.ims.util;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.Value;
import com.daml.ledger.rxjava.components.helpers.CreatedContract;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class TemplateUtils {

  /**
   * Creates a transform function which is aware of the template types. Useful to be passed to
   * Bot.wire.
   *
   * @param allowedTemplates the list of allowed template types
   * @return a transformation function returning a new contract typed as one of the allowed
   *     templates
   */
  public static Function<CreatedContract, Template> contractTransformator(
      Class<? extends Template>... allowedTemplates) {
    return createdContract -> {
      Record args = createdContract.getCreateArguments();
      for (Class template : allowedTemplates) {
        try {
          Identifier templateId = (Identifier) template.getField("TEMPLATE_ID").get(null);
          if (createdContract.getTemplateId().equals(templateId)) {
            return (Template)
                template.getMethod("fromValue", new Class[] {Value.class}).invoke(null, args);
          }
        } catch (IllegalAccessException
            | NoSuchFieldException
            | InvocationTargetException
            | NoSuchMethodException e) {
          throw new RuntimeException(
              "Argument class " + template + " should be derived from Template");
        }
      }
      throw new IllegalStateException(
          "Unknown contract of type " + createdContract.getTemplateId());
    };
  }
}
