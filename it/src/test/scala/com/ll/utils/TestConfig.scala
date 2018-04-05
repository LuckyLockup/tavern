package com.ll.utils

import scala.concurrent.duration._

case class TestConfig(
  wsUrl: String,
  soloUrl: String,
  defaultTimeout: Duration
)