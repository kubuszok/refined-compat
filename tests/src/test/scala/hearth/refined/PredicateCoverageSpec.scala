package hearth
package refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import hearth.refined.auto.*

final class PredicateCoverageSpec extends munit.FunSuite {

  // --------------- Numeric predicates ---------------

  test("numeric: Positive") {
    val x: Int Refined Positive = 5
    assertEquals(x.value, 5)
  }

  test("numeric: Positive rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.numeric.Positive
      import hearth.refined.auto.*
      val x: Int Refined Positive = 0
    """).nonEmpty)
  }

  test("numeric: NonNegative (0)") {
    val x: Int Refined NonNegative = 0
    assertEquals(x.value, 0)
  }

  test("numeric: Negative") {
    val x: Int Refined Negative = -3
    assertEquals(x.value, -3)
  }

  test("numeric: NonPositive") {
    val x: Int Refined NonPositive = 0
    assertEquals(x.value, 0)
  }

  test("numeric: Greater[5]") {
    val x: Int Refined Greater[5] = 10
    assertEquals(x.value, 10)
  }

  test("numeric: Greater[5] rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.numeric.Greater
      import hearth.refined.auto.*
      val x: Int Refined Greater[5] = 3
    """).nonEmpty)
  }

  test("numeric: Less[10]") {
    val x: Int Refined Less[10] = 5
    assertEquals(x.value, 5)
  }

  test("numeric: Less[10] rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.numeric.Less
      import hearth.refined.auto.*
      val x: Int Refined Less[10] = 15
    """).nonEmpty)
  }

  test("numeric: Long Refined Positive") {
    val x: Long Refined Positive = 100L
    assertEquals(x.value, 100L)
  }

  test("numeric: Double Refined Positive") {
    val x: Double Refined Positive = 3.14
    assertEquals(x.value, 3.14)
  }

  test("numeric: Interval.Closed[1, 10]") {
    val x: Int Refined Interval.Closed[1, 10] = 5
    assertEquals(x.value, 5)
  }

  test("numeric: Interval.Closed boundaries") {
    val low: Int Refined Interval.Closed[1, 10] = 1
    val high: Int Refined Interval.Closed[1, 10] = 10
    assertEquals(low.value, 1)
    assertEquals(high.value, 10)
  }

  test("numeric: Interval.Closed rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.numeric.Interval
      import hearth.refined.auto.*
      val x: Int Refined Interval.Closed[1, 10] = 0
    """).nonEmpty)
  }

  test("numeric: Interval.Open[0, 100]") {
    val x: Int Refined Interval.Open[0, 100] = 50
    assertEquals(x.value, 50)
  }

  test("numeric: Modulo[3, 0] (divisible by 3)") {
    val x: Int Refined Modulo[3, 0] = 9
    assertEquals(x.value, 9)
  }

  test("numeric: Greater[-5] (negative literal type)") {
    val x: Int Refined Greater[-5] = -1
    assertEquals(x.value, -1)
  }

  // --------------- String predicates ---------------

  test("string: MatchesRegex") {
    val x: String Refined MatchesRegex["[a-z]+"] = "hello"
    assertEquals(x.value, "hello")
  }

  test("string: MatchesRegex rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.string.MatchesRegex
      import hearth.refined.auto.*
      val x: String Refined MatchesRegex["[a-z]+"] = "Hello123"
    """).nonEmpty)
  }

  test("string: StartsWith") {
    val x: String Refined StartsWith["http"] = "http://example.com"
    assertEquals(x.value, "http://example.com")
  }

  test("string: EndsWith") {
    val x: String Refined EndsWith[".scala"] = "Main.scala"
    assertEquals(x.value, "Main.scala")
  }

  test("string: Uuid") {
    val x: String Refined Uuid = "550e8400-e29b-41d4-a716-446655440000"
    assertEquals(x.value, "550e8400-e29b-41d4-a716-446655440000")
  }

  test("string: Trimmed") {
    val x: String Refined Trimmed = "no spaces around"
    assertEquals(x.value, "no spaces around")
  }

  test("string: ValidInt") {
    val x: String Refined ValidInt = "42"
    assertEquals(x.value, "42")
  }

  test("string: ValidLong") {
    val x: String Refined ValidLong = "9999999999"
    assertEquals(x.value, "9999999999")
  }

  test("string: ValidDouble") {
    val x: String Refined ValidDouble = "3.14"
    assertEquals(x.value, "3.14")
  }

  // --------------- Collection predicates ---------------

  test("collection: NonEmpty String") {
    val x: String Refined NonEmpty = "hello"
    assertEquals(x.value, "hello")
  }

  test("collection: NonEmpty rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.collection.NonEmpty
      import hearth.refined.auto.*
      val x: String Refined NonEmpty = ""
    """).nonEmpty)
  }

  test("collection: Size[Greater[2]]") {
    val x: String Refined Size[Greater[2]] = "abc"
    assertEquals(x.value, "abc")
  }

  test("collection: MinSize[3]") {
    val x: String Refined MinSize[3] = "abc"
    assertEquals(x.value, "abc")
  }

  test("collection: MaxSize[5]") {
    val x: String Refined MaxSize[5] = "hi"
    assertEquals(x.value, "hi")
  }

  // --------------- Generic predicates ---------------

  test("generic: Equal[42]") {
    val x: Int Refined Equal[42] = 42
    assertEquals(x.value, 42)
  }

  test("generic: Equal[42] rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.generic.Equal
      import hearth.refined.auto.*
      val x: Int Refined Equal[42] = 43
    """).nonEmpty)
  }

  test("generic: Equal[\"hello\"]") {
    val x: String Refined Equal["hello"] = "hello"
    assertEquals(x.value, "hello")
  }

  test("generic: Equal[true]") {
    val x: Boolean Refined Equal[true] = true
    assertEquals(x.value, true)
  }

  // --------------- Boolean composition ---------------

  test("composition: And[Positive, Less[100]]") {
    val x: Int Refined And[Positive, Less[100]] = 50
    assertEquals(x.value, 50)
  }

  test("composition: And rejection (fails first)") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.boolean.And
      import eu.timepit.refined.numeric.{Positive, Less}
      import hearth.refined.auto.*
      val x: Int Refined And[Positive, Less[100]] = -5
    """).nonEmpty)
  }

  test("composition: And rejection (fails second)") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.boolean.And
      import eu.timepit.refined.numeric.{Positive, Less}
      import hearth.refined.auto.*
      val x: Int Refined And[Positive, Less[100]] = 200
    """).nonEmpty)
  }

  test("composition: Or[Negative, Greater[100]]") {
    val neg: Int Refined Or[Negative, Greater[100]] = -5
    val big: Int Refined Or[Negative, Greater[100]] = 200
    assertEquals(neg.value, -5)
    assertEquals(big.value, 200)
  }

  test("composition: Not[Negative]") {
    val x: Int Refined Not[Negative] = 0
    assertEquals(x.value, 0)
  }

  test("composition: triple And") {
    val x: Int Refined And[Positive, And[Less[100], Not[Equal[50]]]] = 42
    assertEquals(x.value, 42)
  }

  test("composition: triple And rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.boolean.{And, Not}
      import eu.timepit.refined.numeric.{Positive, Less}
      import eu.timepit.refined.generic.Equal
      import hearth.refined.auto.*
      val x: Int Refined And[Positive, And[Less[100], Not[Equal[50]]]] = 50
    """).nonEmpty)
  }

  // --------------- Type alias dealiasing ---------------

  type PosInt = Int Refined Positive
  type NonNegInt = Int Refined NonNegative
  type BoundedInt = Int Refined Interval.Closed[1, 100]
  type NonEmptyString = String Refined NonEmpty
  type RegexString = String Refined MatchesRegex["^[a-z]+$"]
  type PortNumber = Int Refined And[Greater[0], Less[65536]]

  test("alias: PosInt") {
    val x: PosInt = 5
    assertEquals(x.value, 5)
  }

  test("alias: NonNegInt") {
    val x: NonNegInt = 0
    assertEquals(x.value, 0)
  }

  test("alias: BoundedInt (Interval)") {
    val x: BoundedInt = 50
    assertEquals(x.value, 50)
  }

  test("alias: NonEmptyString") {
    val x: NonEmptyString = "hello"
    assertEquals(x.value, "hello")
  }

  test("alias: RegexString") {
    val x: RegexString = "hello"
    assertEquals(x.value, "hello")
  }

  test("alias: PortNumber (composed)") {
    val x: PortNumber = 8080
    assertEquals(x.value, 8080)
  }

  test("alias: PortNumber rejection") {
    assert(compileErrors("""
      import eu.timepit.refined.api.Refined
      import eu.timepit.refined.boolean.And
      import eu.timepit.refined.numeric.{Greater, Less}
      import hearth.refined.auto.*
      type PortNumber = Int Refined And[Greater[0], Less[65536]]
      val x: PortNumber = 0
    """).nonEmpty)
  }
}
