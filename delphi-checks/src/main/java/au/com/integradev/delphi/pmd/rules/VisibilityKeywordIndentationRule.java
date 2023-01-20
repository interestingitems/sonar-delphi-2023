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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.node.VisibilityNode;
import au.com.integradev.delphi.utils.IndentationUtils;
import net.sourceforge.pmd.RuleContext;

public class VisibilityKeywordIndentationRule extends AbstractDelphiRule {
  private String getExpectedIndentation(DelphiNode node) {
    var visibilityNode = (VisibilityNode) node;
    // Class/Record/etc. -> VisibilitySection -> Visibility
    var parent = (DelphiNode) visibilityNode.jjtGetParent().jjtGetParent();
    return IndentationUtils.getLineIndentation(parent);
  }

  @Override
  public RuleContext visit(VisibilityNode visibilityNode, RuleContext data) {
    if (!IndentationUtils.getLineIndentation(visibilityNode)
        .equals(getExpectedIndentation(visibilityNode))) {
      addViolation(data, visibilityNode);
    }

    return super.visit(visibilityNode, data);
  }
}