package com.tersesystems.sandboxexperiment.sandbox

import com.tersesystems.sandboxexperiment.privlib.Executor

class ReducedPrivilegeScriptRunner {

  def run() = {
    Executor.readPropertyWithNoPrivileges()
  }

}
