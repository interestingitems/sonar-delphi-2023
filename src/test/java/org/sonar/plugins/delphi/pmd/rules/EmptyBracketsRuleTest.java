package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

public class EmptyBracketsRuleTest extends BasePmdRuleTest {

  @Test
  public void testMethodParametersEmptyBracketsShouldAddIssue() {
    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  TMyForm = class(TObject)");
    builder.appendDecl("    procedure MyProcedure();");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffsetDecl() + 3));
  }

  @Test
  public void testInvocationOfUnknownMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffset() + 3));
  }

  @Test
  public void testInvocationOfKnownMethodShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  MyProcedure();")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("EmptyBracketsRule", builder.getOffset() + 3));
  }

  @Test
  public void testExplicitArrayConstructorShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("type")
            .appendDecl("  TIntArray = array of Integer;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  Foo: TIntArray;")
            .appendImpl("begin")
            .appendImpl("  Foo := TIntArray.Create();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  public void testInvocationOfProcVarShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendDecl("procedure MyProcedure;")
            .appendImpl("procedure Test;")
            .appendImpl("var")
            .appendImpl("  ProcVar: procedure;")
            .appendImpl("begin")
            .appendImpl("  ProcVar();")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }
}
