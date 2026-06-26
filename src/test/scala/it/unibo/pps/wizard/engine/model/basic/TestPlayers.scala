package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestPlayers extends AnyWordSpec with Matchers:
  "A Player" should:
    val id: PlayerId = PlayerId(1)
    val p = Player.human(id)
    "have the correct id" in:
      p.id shouldBe id
      p.isBot shouldBe false
  "A Bot" should :
    val id: PlayerId = PlayerId(2)
    val com = Player.computer(id)
    "have the correct id" in :
      com.id shouldBe id
      com.isBot shouldBe true
//  "Players" should:
//    "create" in:
//      ???