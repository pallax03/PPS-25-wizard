package it.unibo.pps.wizard.util

import it.unibo.tuprolog.core.{Struct, Term}
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesReader
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.solve.{Solution, Solver}

import java.io.StringReader
import scala.jdk.CollectionConverters.*

object PrologEngine:

  given Conversion[String, Term] = TermParser.withDefaultOperators().parseTerm(_)
  given Conversion[String, Struct] = TermParser.withDefaultOperators().parseStruct(_)
  given Conversion[String, Theory] = s =>
    ClausesReader.withDefaultOperators().readTheory(new StringReader(s))

  def buildEngine(theory: Theory): Struct => LazyList[Solution] =
    val solver = Solver.prolog().newBuilder().staticKb(theory).build()
    goal => solver.solve(goal).iterator().asScala.to(LazyList)

  def extractVars(solution: Solution): Map[String, Term] =
    if solution.isYes then solution.getSubstitution.asScala.map((v, t) => v.getName -> t).toMap
    else Map.empty
