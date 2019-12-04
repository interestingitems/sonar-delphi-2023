package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createBoolean;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createDecimal;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createInteger;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createSet;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.createString;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionValues.unknownValue;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue.BinaryEvaluator;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue.UnaryEvaluator;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;

public class Expressions {
  private Expressions() {
    // Utility class
  }

  public static Expression binary(Expression left, TokenType operator, Expression right) {
    return new BinaryExpression(left, operator, right);
  }

  public static Expression unary(TokenType operator, Expression expression) {
    return new UnaryExpression(operator, expression);
  }

  public static Expression literal(TokenType type, String value) {
    return new LiteralExpression(type, value);
  }

  public static Expression set(Set<Expression> elements) {
    return new SetExpression(elements);
  }

  public static Expression emptySet() {
    return new SetExpression(Collections.emptySet());
  }

  public static Expression nameReference(String name) {
    return new NameReferenceExpression(name);
  }

  public static Expression invocation(String name, List<Expression> arguments) {
    return new InvocationExpression(name, arguments);
  }

  static class BinaryExpression implements Expression {
    private static final Map<TokenType, BinaryEvaluator> EVALUATORS;

    static {
      EVALUATORS = new EnumMap<>(TokenType.class);
      EVALUATORS.put(TokenType.PLUS, ExpressionValues::add);
      EVALUATORS.put(TokenType.MINUS, ExpressionValues::subtract);
      EVALUATORS.put(TokenType.MULTIPLY, ExpressionValues::multiply);
      EVALUATORS.put(TokenType.DIVIDE, ExpressionValues::divide);
      EVALUATORS.put(TokenType.DIV, ExpressionValues::div);
      EVALUATORS.put(TokenType.MOD, ExpressionValues::mod);
      EVALUATORS.put(TokenType.SHL, ExpressionValues::shl);
      EVALUATORS.put(TokenType.SHR, ExpressionValues::shr);
      EVALUATORS.put(TokenType.EQUALS, ExpressionValues::isEqual);
      EVALUATORS.put(TokenType.GREATER_THAN, ExpressionValues::greaterThan);
      EVALUATORS.put(TokenType.LESS_THAN, ExpressionValues::lessThan);
      EVALUATORS.put(TokenType.GREATER_THAN_EQUAL, ExpressionValues::greaterThanEqual);
      EVALUATORS.put(TokenType.LESS_THAN_EQUAL, ExpressionValues::lessThanEqual);
      EVALUATORS.put(TokenType.NOT_EQUALS, ExpressionValues::notEqual);
      EVALUATORS.put(TokenType.IN, ExpressionValues::in);
      EVALUATORS.put(TokenType.AND, ExpressionValues::and);
      EVALUATORS.put(TokenType.OR, ExpressionValues::or);
      EVALUATORS.put(TokenType.XOR, ExpressionValues::xor);
    }

    private final Expression leftExpression;
    private final TokenType operator;
    private final Expression rightExpression;

    BinaryExpression(Expression leftExpression, TokenType operator, Expression rightExpression) {
      this.leftExpression = leftExpression;
      this.operator = operator;
      this.rightExpression = rightExpression;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      BinaryEvaluator evaluator = checkNotNull(EVALUATORS.get(operator));
      checkNotNull(evaluator, "Unhandled binary operator '" + operator.name() + "'");
      ExpressionValue left = leftExpression.evaluate(preprocessor);
      ExpressionValue right = rightExpression.evaluate(preprocessor);

      return evaluator.apply(left, right);
    }
  }

  static class UnaryExpression implements Expression {
    private static final Map<TokenType, UnaryEvaluator> EVALUATORS =
        Map.of(
            TokenType.PLUS, ExpressionValues::plus,
            TokenType.MINUS, ExpressionValues::negate,
            TokenType.NOT, ExpressionValues::not);

    private final TokenType operator;
    private final Expression expression;

    private UnaryExpression(TokenType operator, Expression expression) {
      this.operator = operator;
      this.expression = expression;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      UnaryEvaluator evaluator = EVALUATORS.get(operator);
      checkNotNull(evaluator, "Unhandled unary operator '" + operator.name() + "'");
      ExpressionValue value = expression.evaluate(preprocessor);

      return evaluator.apply(value);
    }
  }

  static class LiteralExpression implements Expression {
    private final ExpressionValue value;

    private LiteralExpression(TokenType type, String text) {
      value = createValue(type, text);
    }

    private static ExpressionValue createValue(TokenType type, String text) {
      switch (type) {
        case INTEGER:
          return createInteger(Integer.parseInt(text));
        case DECIMAL:
          return createDecimal(Double.parseDouble(text));
        case STRING:
          return createString(text);
        default:
          throw new AssertionError("Unhandled literal expression type: " + type.name());
      }
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      return value;
    }
  }

  static class SetExpression implements Expression {
    private final Set<Expression> elements;

    private SetExpression(Set<Expression> elements) {
      this.elements = elements;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      Set<ExpressionValue> elementValues =
          elements.stream()
              .map(expression -> expression.evaluate(preprocessor))
              .collect(Collectors.toSet());

      return createSet(elementValues);
    }
  }

  static class NameReferenceExpression implements Expression {
    private final String name;

    private NameReferenceExpression(String name) {
      this.name = name;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      if (this.name.equalsIgnoreCase("True")) {
        return createBoolean(true);
      } else if (this.name.equalsIgnoreCase("False")) {
        return createBoolean(false);
      }
      return unknownValue();
    }
  }

  static class InvocationExpression implements Expression {
    private final String name;
    private final List<Expression> arguments;

    private InvocationExpression(String name, List<Expression> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      if (name.equalsIgnoreCase("Defined") && !arguments.isEmpty()) {
        Expression argument = arguments.get(0);
        if (argument instanceof NameReferenceExpression) {
          boolean isDefined = preprocessor.isDefined(((NameReferenceExpression) argument).name);
          return createBoolean(isDefined);
        }
      }
      return unknownValue();
    }
  }
}