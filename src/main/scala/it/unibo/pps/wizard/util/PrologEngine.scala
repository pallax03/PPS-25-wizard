package it.unibo.pps.wizard.util

import alice.tuprolog.{Prolog, SolveInfo, Term, Theory}

import scala.jdk.CollectionConverters.*

object PrologEngine:

  given Conversion[String, Theory] = Theory.parseWithStandardOperators(_)

  def buildEngine(theory: Theory): String => LazyList[SolveInfo] =
    val solver = Prolog()
    solver.setTheory(theory)
    goal =>
      new Iterable[SolveInfo]:
        override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo]:
          private var solution: Option[SolveInfo] = Some(solver.solve(goal))

          override def hasNext: Boolean =
            solution.exists(current => current.isSuccess || current.hasOpenAlternatives)

          override def next(): SolveInfo =
            try solution.get
            finally
              solution =
                if solution.get.hasOpenAlternatives then Some(solver.solveNext())
                else None
      .to(LazyList)

  def extractVars(solution: SolveInfo): Map[String, Term] =
    if solution.isSuccess then
      solution.getBindingVars.asScala
        .map(variable => variable.getName -> solution.getTerm(variable.getName))
        .toMap
    else Map.empty
