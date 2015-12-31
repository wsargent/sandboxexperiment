package com.tersesystems.sandboxexperiment.security

import java.security._
import java.util.PropertyPermission

import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers._
import org.slf4j.MarkerFactory

import scala.util.control.NonFatal

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
class SandboxPolicy(scriptName: String) extends Policy {

  // cache a reference to this protection domain (per p123 of Java Security)
  // this give us AllPermission
  private val providerDomain = {
    val p = this
    AccessController.doPrivileged(DoPrivilegedAction(p.getClass.getProtectionDomain))
  }

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def getPermissions(domain: ProtectionDomain): PermissionCollection = {

    val policy = isPolicy(domain)
    val sandbox = isSandbox(domain)
    logger.info(markers(domain), s"getPermissions, policy = $policy, sandbox = $sandbox")

    val result: PermissionCollection = if (policy) {
      new AllPermission().newPermissionCollection()
    } else if (sandbox) {
      sandboxPermissions
    } else {
      appPermissions
    }
    if (isSandbox(domain)) {
      logger.info(s"getPermissions: domainPermissions = [${domain.getPermissions}], result = $result")
    }
    logger.info(markers(domain), s"getPermissions result = $result")
    result
  }

  override def implies(domain: ProtectionDomain, permission: Permission): Boolean = {
    logger.info(markers(domain, permission), "implies")

    val isImplied = if (isPolicy(domain) || isLogback(domain)) {
      true
    } else {
      super.implies(domain, permission)
    }

    if (isSandbox(domain)) {
      //import scala.collection.JavaConverters._
      logger.info(s"implies: domainPermissions = [${domain.getPermissions}], permission = [$permission], result = $isImplied")
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

  private val securityMarker = MarkerFactory.getMarker("SECURITY")

  private def markers(pd: ProtectionDomain, perm: Permission): LogstashMarker = {
    val markers: LogstashMarker = doMarkers(pd).and(doMarkers(perm))
    markers.and(securityMarker)
  }

  private def markers(perm: Permission): LogstashMarker = {
    doMarkers(perm).and(securityMarker)
  }

  private def markers(pd: ProtectionDomain): LogstashMarker = {
    // logstash markers don't have idempotent tags!
    doMarkers(pd).and(securityMarker)
  }

  private def doMarkers(perm: Permission): LogstashMarker = {
    var markers: LogstashMarker = append("permissionClass", perm.getClass.getName)
    markers = markers.and(append("permissionName", perm.getName))
    if (perm.getActions != null && !(perm.getActions == "")) {
      markers = markers.and(append("permissionActions", perm.getActions))
    }
    markers
  }

  private def location(pd: ProtectionDomain) = {
    val optCodeSource: Option[CodeSource] = Option(pd.getCodeSource)
    val location = optCodeSource.map(_.getLocation).getOrElse("null")
    location
  }

  private def classLoader(pd: ProtectionDomain) = {
    val cl = pd.getClassLoader
    Option(cl).getOrElse("null").getClass.getName
  }

  private def doMarkers(pd: ProtectionDomain): LogstashMarker = {
    // Start with something that can't blow up.
    var markers = append("protectionDomainHashCode", Integer.toHexString(pd.hashCode()))

    markers = markers.and(append("protectionDomainClassLoader", classLoader(pd)))
    markers = markers.and(append("protectionDomainPermissions", pd.getPermissions))
    markers = markers.and(append("codeSourceLocation", location(pd)))

    //val certs = optCodeSource.map(_.getCertificates).getOrElse(Array()).toSeq
    //markers = markers.and(append("codeSourceSigners", signers))
    // markers = markers.and(append("codeSourceCertificates", certs))

    //    try {
    //      // If you call pd.toString, it calls out to the security manager... which is us.
    //      // instant stack overflow.
    //      //val singleLinePD = pd.toString.replace("\n", " ")
    //      //markers = markers.and(append("protectionDomain", singleLinePD))
    //      //markers = markers.and(append("protectionDomainPrincipals", principals))
    //      //val principals = pd.getPrincipals.toSeq.toString
    //    } catch {
    //      case e:Exception =>
    //        // Well this is no fun:
    //        //
    //        //      [error] java.lang.NullPointerException
    //        //      [error] 	at java.security.ProtectionDomain.getPrincipals(ProtectionDomain.java:231)
    //        //      [error] 	at com.tersesystems.sandboxexperiment.security.SandboxPolicy.doMarkers(SandboxPolicy.scala:137)
    //        //      [error] 	at com.tersesystems.sandboxexperiment.security.SandboxPolicy.markers(SandboxPolicy.scala:101)
    //        //      [error] 	at com.tersesystems.sandboxexperiment.security.SandboxPolicy.implies(SandboxPolicy.scala:57)
    //        //      [error] 	at java.security.ProtectionDomain.implies(ProtectionDomain.java:281)
    //        //      [error] 	at java.security.AccessControlContext.checkPermission(AccessControlContext.java:450)
    //        //      [error] 	at java.security.AccessController.checkPermission(AccessController.java:884)
    //        //      [error] 	at java.lang.SecurityManager.checkPermission(SecurityManager.java:549)
    //        //      [error] 	at java.lang.SecurityManager.checkPropertyAccess(SecurityManager.java:1294)
    //        //      [error] 	at java.lang.System.getProperty(System.java:717)
    //        //      [error] 	at com.tersesystems.sandboxexperiment.sandbox.ScriptRunner.com$tersesystems$sandboxexperiment$sandbox$ScriptRunner$$executeScript(ScriptRunner.scala:24)
    //
    //        e.printStackTrace()
    //        import scala.compat.Platform.EOL
    //        markers = markers.and(append("exception", e.getStackTrace.mkString("", EOL, EOL)))
    //    }

    markers
  }

  def isLogback(domain: ProtectionDomain): Boolean = {
    false
    //Option(domain.getCodeSource).exists(_.getLocation.toString.endsWith("logback-classic-1.1.3.jar"))
  }

  def isPolicy(domain: ProtectionDomain) = providerDomain == domain

  private def p(block: => Unit): Unit = {
    AccessController.doPrivileged(new PrivilegedAction[Unit] {
      override def run() = block
    })
  }

}
