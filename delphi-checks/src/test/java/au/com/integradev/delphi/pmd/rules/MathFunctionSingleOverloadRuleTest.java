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

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;
import static au.com.integradev.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import org.junit.jupiter.api.Test;

class MathFunctionSingleOverloadRuleTest extends BasePmdRuleTest {

  @Test
  void testExtendedOverloadShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  Power(1.0, Integer(1));")
            .appendImpl("  IntPower(1.0, 1);")
            .appendImpl("  IntPower(Extended(1.0), 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MathFunctionSingleOverloadRule"));
  }

  @Test
  void testDoubleOverloadShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  IntPower(Double(1.0), 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("MathFunctionSingleOverloadRule"));
  }

  @Test
  void testSingleOverloadShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("uses")
            .appendImpl("  System.Math;")
            .appendImpl("procedure Test;")
            .appendImpl("begin")
            .appendImpl("  IntPower(Single(1.0), 1);")
            .appendImpl("  IntPower(1, 1);")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .areExactly(1, ruleKeyAtLine("MathFunctionSingleOverloadRule", builder.getOffset() + 5))
        .areExactly(1, ruleKeyAtLine("MathFunctionSingleOverloadRule", builder.getOffset() + 6));
  }
}