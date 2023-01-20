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
package au.com.integradev.delphi.utils;

import au.com.integradev.delphi.antlr.ast.node.ArgumentListNode;
import au.com.integradev.delphi.antlr.ast.node.ExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.ExpressionStatementNode;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNode;
import au.com.integradev.delphi.antlr.ast.node.PrimaryExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.StatementNode;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public class StatementUtils {
  private StatementUtils() {
    // Utility class
  }

  public static boolean isMethodInvocation(
      StatementNode statement,
      String fullyQualifiedName,
      Predicate<List<ExpressionNode>> argumentListPredicate) {
    if (!(statement instanceof ExpressionStatementNode)) {
      return false;
    }

    var expression = ((ExpressionStatementNode) statement).getExpression().skipParentheses();
    if (!(expression instanceof PrimaryExpressionNode) || expression.jjtGetNumChildren() > 2) {
      return false;
    }

    Node name = expression.jjtGetChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    List<ExpressionNode> arguments = extractArguments(expression.jjtGetChild(1));
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).fullyQualifiedName().equals(fullyQualifiedName)
        && argumentListPredicate.test(arguments);
  }

  private static List<ExpressionNode> extractArguments(Node argumentList) {
    if (argumentList instanceof ArgumentListNode) {
      return ((ArgumentListNode) argumentList).getArguments();
    }
    return Collections.emptyList();
  }
}