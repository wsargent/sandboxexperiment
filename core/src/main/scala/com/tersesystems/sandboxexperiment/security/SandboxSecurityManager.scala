package com.tersesystems.sandboxexperiment.security

import java.security.Policy

/**
 * A passthrough to the security policy.  SecurityManager itself doesn't do much.
 */
class SandboxSecurityManager(scriptName:String) extends SecurityManager {
  Policy.setPolicy(new SandboxPolicy(scriptName))
}
