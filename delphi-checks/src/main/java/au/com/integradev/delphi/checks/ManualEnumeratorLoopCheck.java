/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

@Rule(key = "ManualEnumeratorLoop")
public class ManualEnumeratorLoopCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(ForToStatementNode forNode, DelphiCheckContext context) {
    doVisit(forNode, context);
    return super.visit(forNode, context);
  }

  private void doVisit(ForToStatementNode forNode, DelphiCheckContext context) {
    if (forNode.getChildren().size() < 7) {
      return;
    }

    var forVar = forNode.getChild(0);
    var assign = forNode.getChild(1);
    var startValue = forNode.getChild(2);
    var to = forNode.getChild(3);
    var endValue = forNode.getChild(4);
    var loopBody = forNode.getChild(6);

    if ((forVar instanceof ForLoopVarReferenceNode || forVar instanceof ForLoopVarDeclarationNode)
        && assign instanceof CommonDelphiNode
        && assign.getToken().getType() == DelphiTokenType.ASSIGN
        && startValue instanceof PrimaryExpressionNode
        && startValue.getChildren().size() == 1
        && startValue.getChild(0) instanceof IntegerLiteralNode
        && ((IntegerLiteralNode) startValue.getChild(0)).getValueAsInt() == 0
        && to instanceof CommonDelphiNode
        && to.getToken().getType() == DelphiTokenType.TO
        && endValue instanceof BinaryExpressionNode) {

      BinaryExpressionNode binaryEndValue = (BinaryExpressionNode) endValue;
      ExpressionNode left = binaryEndValue.getLeft();
      if (binaryEndValue.getOperator() == BinaryOperator.SUBTRACT
          && isLiteralOne(binaryEndValue.getRight())
          && left.getChildren().size() == 1
          && left.getChild(0) instanceof NameReferenceNode) {

        NameReferenceNode upperLoopTargetNameReference = (NameReferenceNode) left.getChild(0);

        var decl = upperLoopTargetNameReference.getNameDeclaration();

        NameDeclaration forVarDecl =
            (forVar instanceof ForLoopVarReferenceNode)
                ? ((ForLoopVarReferenceNode) forVar).getNameReference().getNameDeclaration()
                : ((ForLoopVarDeclarationNode) forVar)
                    .getNameDeclarationNode()
                    .getNameDeclaration();

        if (decl instanceof VariableNameDeclaration
            && isEnumerableDotCount(upperLoopTargetNameReference)
            && loopBody instanceof CompoundStatementNode
            && loopBody.getFirstChildOfType(StatementListNode.class) != null
            && loopBody.getFirstChildOfType(StatementListNode.class).getChildren().size() > 0
            && isIndexedAssignment(
                loopBody.getFirstChildOfType(StatementListNode.class).getChild(0), decl, forVarDecl)
            && loopBody.findDescendantsOfType(NameReferenceNode.class).stream()
                    .filter(n -> n.getNameDeclaration().equals(forVarDecl))
                    .count()
                == 1) {
          reportIssue(context, forVar, "Enumerate this collection using a for-in loop.");
        }
      }
    }
  }

  private static boolean isLiteralOne(ExpressionNode right) {
    return right.isIntegerLiteral() && right.extractLiteral().getValueAsInt() == 1;
  }

  private static boolean isEnumerableDotCount(NameReferenceNode node) {
    NameReferenceNode lastName = node.getLastName();
    return lastName.getIdentifier().getImage().equalsIgnoreCase("Count")
        && isReferenceToEnumerableType(lastName.prevName());
  }

  private static boolean isReferenceToEnumerableType(NameReferenceNode child) {
    NameDeclaration nameDeclaration = child.getNameDeclaration();
    return nameDeclaration instanceof VariableNameDeclaration
        && isEnumerableType(((VariableNameDeclaration) nameDeclaration).getType());
  }

  private static boolean isEnumerableType(Type type) {
    return type.isSubTypeOf("System.IEnumerable") || isImplicitEnumerableType(type);
  }

  private static boolean isImplicitEnumerableType(Type type) {
    if (!(type instanceof StructType)) {
      return false;
    }
    var decls = ((StructType) type).typeScope().getMethodDeclarations();
    return decls.stream()
        .filter(d -> d.getName().equalsIgnoreCase("GetEnumerator"))
        .findFirst()
        .map(Invocable::getReturnType)
        .filter(ManualEnumeratorLoopCheck::isImplicitEnumeratorType)
        .isPresent();
  }

  private static boolean isImplicitEnumeratorType(Type type) {
    if (type.isSubTypeOf("System.IEnumerator")) {
      return true;
    }

    if (!(type instanceof StructType)) {
      return false;
    }
    var decls = ((StructType) type).typeScope().getMethodDeclarations();
    var propDecls = ((StructType) type).typeScope().getPropertyDeclarations();

    return hasCurrentProperty(propDecls) && hasMoveNextMethod(decls) && hasResetMethod(decls);
  }

  private static boolean hasResetMethod(Set<MethodNameDeclaration> decls) {
    return decls.stream()
        .anyMatch(d -> d.getName().equalsIgnoreCase("Reset") && d.getReturnType().isVoid());
  }

  private static boolean hasMoveNextMethod(Set<MethodNameDeclaration> decls) {
    return decls.stream()
        .anyMatch(d -> d.getName().equalsIgnoreCase("MoveNext") && d.getReturnType().isBoolean());
  }

  private static boolean hasCurrentProperty(Set<PropertyNameDeclaration> decls) {
    return decls.stream()
        .anyMatch(d -> d.getName().equalsIgnoreCase("Current") && !d.getReturnType().isVoid());
  }

  private static boolean isIndexedAssignment(
      Node node, NameDeclaration enumerable, NameDeclaration index) {
    DelphiNode assignmentSource;

    if (node instanceof AssignmentStatementNode) {
      AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode) node;

      if (assignmentStatementNode.getChildren().size() != 2) {
        return false;
      }
      DelphiNode assignmentTarget = assignmentStatementNode.getChild(0);

      if (!(assignmentTarget instanceof PrimaryExpressionNode)
          || assignmentTarget.getChildren().size() != 1
          || !(assignmentTarget.getChild(0) instanceof NameReferenceNode)) {
        return false;
      }

      // make sure it's a simple local variable
      NameReferenceNode assignmentReference = (NameReferenceNode) assignmentTarget.getChild(0);
      if (!(assignmentReference.getNameDeclaration().getScope() instanceof MethodScope)
          || assignmentReference.getChildren().size() != 1) {
        return false;
      }
      assignmentSource = assignmentStatementNode.getChild(1);
    } else if (node instanceof VarStatementNode) {
      assignmentSource = ((VarStatementNode) node).getExpression();
      if (assignmentSource == null) {
        return false;
      }
    } else {
      return false;
    }

    if (assignmentSource.getChildren().size() != 2
        || !(assignmentSource instanceof PrimaryExpressionNode)) {
      return false;
    }
    DelphiNode enumerableReference = assignmentSource.getChild(0);
    if (!(enumerableReference instanceof NameReferenceNode)
        || !((NameReferenceNode) enumerableReference).getNameDeclaration().equals(enumerable)) {
      return false;
    }
    DelphiNode arrayAccessor = assignmentSource.getChild(1);
    if (!(arrayAccessor instanceof ArrayAccessorNode) || arrayAccessor.getChildren().size() != 1) {
      return false;
    }
    DelphiNode arrayAccessExpr = arrayAccessor.getChild(0);
    if (!(arrayAccessExpr instanceof PrimaryExpressionNode)
        || arrayAccessExpr.getChildren().size() != 1) {
      return false;
    }
    DelphiNode indexReference = arrayAccessExpr.getChild(0);
    return indexReference instanceof NameReferenceNode
        && ((NameReferenceNode) indexReference).getNameDeclaration().equals(index);
  }
}
