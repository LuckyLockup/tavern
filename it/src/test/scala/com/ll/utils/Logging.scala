package com.ll.utils

import org.slf4j.LoggerFactory

trait Logging { protected lazy val log = LoggerFactory.getLogger(this.getClass) }