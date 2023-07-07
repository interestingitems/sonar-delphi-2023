/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.utils.NameConventionUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ConstructorCreateRule", repositoryKey = "delph")
@Rule(key = "ConstructorName")
public class ConstructorNameCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Rename this constructor to match the expected naming convention";
  private static final String PREFIX = "Create";

  @Override
  public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
    if (isViolation(method)) {
      reportIssue(context, method.getMethodNameNode(), MESSAGE);
    }
    return super.visit(method, context);
  }

  private static boolean isViolation(MethodDeclarationNode method) {
    if (!method.isConstructor() || method.isClassMethod()) {
      return false;
    }

    return !PREFIX.equals(method.simpleName())
        && !NameConventionUtils.compliesWithPrefix(method.simpleName(), PREFIX);
  }
}