package com.tersesystems.sandboxexperiment.security

import java.security.{Permission, ProtectionDomain}

import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers._
import org.slf4j.MarkerFactory

/**
  * Logs security markers.  SLF4J markers are great when you want free form fields.
  */
trait SecurityMarkers {

  private val securityMarker = MarkerFactory.getMarker("SECURITY")

  def markers(pd: ProtectionDomain, perm: Permission): LogstashMarker = {
    val markers: LogstashMarker = doMarkers(pd).and(doMarkers(perm))
    markers.and(securityMarker)
  }

  def markers(perm: Permission): LogstashMarker = {
    doMarkers(perm).and(securityMarker)
  }

  def markers(pd: ProtectionDomain): LogstashMarker = {
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

  private def doMarkers(pd: ProtectionDomain): LogstashMarker = {
    var markers = append("protectionDomainClassLoader", pd.getClassLoader.getClass.getName)
    markers = markers.and(append("protectionDomainPermissions", pd.getPermissions))
    markers = markers.and(append("protectionDomainPrincipals", pd.getPrincipals))
    markers = markers.and(append("codeSourceLocation", pd.getCodeSource.getLocation))
    markers = markers.and(append("codeSourceSigners", pd.getCodeSource.getCodeSigners))
    markers = markers.and(append("codeSourceCertificates", pd.getCodeSource.getCertificates))
    if (! markers.contains("SECURITY")) {
      markers = markers.and(securityMarker)
    }
    markers
  }

}
