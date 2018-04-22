package com.ll.domain.auth

import com.ll.domain.json.JsonHelper
import org.scalatest.FunSuite

class UserIdTest extends JsonHelper {
  val examples = List(
    (UserId(42),
      """42""".stripMargin)
  )

  "Encode json" in  {
    testEncoding(examples)
  }

  "Decode json" in {
    testDecoding(examples)
  }
}
