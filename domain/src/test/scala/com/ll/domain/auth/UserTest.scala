package com.ll.domain.auth

import com.ll.domain.json.JsonHelper

class UserTest extends JsonHelper {
  val examples = List(
    (User(UserId(42), "Akagi"),
      """
        |{"id":42,"nickname":"Akagi"}
      """.stripMargin)
  )

  "Encode json" in  {
    testEncoding(examples)
  }

  "Decode json" in {
    testDecoding(examples)
  }
}
