package com.rew3.es.common

/**
 * Created by shishir on 9/17/15.
 */
sealed trait CommandResult
case object CommandAccepted extends CommandResult

//TODO check this violation
case class CommandRejected(violation: String) extends CommandResult