package com.tersesystems.sandboxexperiment.security

import java.security._
import java.util.PropertyPermission

/**
 * Specifies a sandbox policy which grants AllPermission to the AppClassLoader, and a very
 * limited set of permissions to the SandboxClassLoader.
 *
 * The policy can only really distinguish sandbox code by CodeSource location (the path on the filesystem),
 * and the class loader.  It doesn't seem possible to sandbox code which is in the same classloader (you
 * can't distinguish individual classes through here, so if you're going to forbid individual classes, you
 * have to do it by filtering through the classloader directly.
 *
 * The root JVM really needs AllPermission or close to it, because even Logback will introspect like crazy.
 *
 * Likewise, this SandboxPolicy can't be packaged with the sandbox directly, because then the protection
 * domain's the same as the sandbox and you've got AllPermission.  So... you MUST keep your sandboxed code
 * in a different JAR file, with a different class loader.
 */
class SandboxPolicy(scriptName: String) extends Policy with SecurityMarkers {

  // cache a reference to this protection domain (per p123 of Java Security)
  // this give us AllPermission
  private val providerDomain = {
    val p = this
    DoPrivilegedAction() {
      p.getClass.getProtectionDomain
    }(AccessController.getContext)
  }

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def getPermissions(domain: ProtectionDomain): PermissionCollection = {
    logger.info(markers(domain), "getPermissions")

    val result: PermissionCollection = if (providerDomain == domain) {
      new AllPermission().newPermissionCollection()
    } else if (isSandbox(domain)) {
      sandboxPermissions
    } else {
      appPermissions
    }

    logger.info(markers(domain), s"getPermissions result = $result")
    result
  }

  override def implies(domain: ProtectionDomain, permission: Permission): Boolean = {
    logger.info(markers(domain, permission), "implies")
    val isImplied = if (providerDomain == domain) {
      true
    } else {
      super.implies(domain, permission)
    }
    logger.info(markers(domain, permission), s"implies result = $isImplied")
    isImplied
  }

  private def appPermissions: Permissions = {
    val permissions = new Permissions()
    permissions.add(new AllPermission())
    permissions
  }

  private def sandboxPermissions: Permissions = {
    val permissions = new Permissions()
    permissions.add(new PropertyPermission("*", "read"))

    // THIS IS THE LINE WHERE EVERYTHING HAPPENS!
    // DON'T COMMENT OUT PLZ KTHXBYE
    permissions.add(new java.io.FilePermission(scriptName, "execute"))

    permissions
  }

  private def isSandbox(domain: ProtectionDomain): Boolean = {
    domain.getClassLoader match {
      case cl: SandboxClassLoader =>
        true
      case other =>
        false
    }
  }

}
