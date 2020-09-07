package com.cisco.pxgrid.samples.ise;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Session {
	private OffsetDateTime timestamp;

	private State state;

	private String userName;
	private String callingStationId;
	private String calledStationId;
	private String auditSessionId;

	private List<String> ipAddresses = new ArrayList<String>();
	private String macAddress;

	private String nasIpAddress;
	private String nasPortId;
	private String nasIdentifier;
	private String nasPortType;

	private String ancPolicy;
	private String postureStatus;
	private String endpointProfile;
	private String endpointOperatingSystem;
	private String ctsSecurityGroup;

	private String adNormalizedUser;
	private String adUserDomainName;
	private String adHostDomainName;
	private String adUserNetBiosName;
	private String adHostNetBiosName;
	private String adUserResolvedIdentities;
	private String adUserResolvedDns;
	private String adHostResolvedIdentities;
	private String adHostResolvedDns;
	private String adUserQualifiedName;
	private String adHostQualifiedName;
	private String adUserSamAccountName;
	private String adHostSamAccountName;

	private List<String> providers = new ArrayList<String>();
	private String endpointCheckResult;
	private OffsetDateTime endpointCheckTime;
	private Long identitySourcePortStart;
	private Long identitySourcePortEnd;
	private Long identitySourcePortFirst;
	private String terminalServerAgentId;
	private String isMachineAuthentication;

	private String serviceType;
	private String tunnelPrivateGroupId;
	private String airespaceWlanId;
	private String networkDeviceProfileName;
	private String radiusFlowType;
	private String ssid;

	// MDM
	private String mdmMacAddress;
	private String mdmOsVersion;
	private boolean mdmRegistered;
	private boolean mdmCompliant;
	private boolean mdmDiskEncrypted;
	private boolean mdmJailBroken;
	private boolean mdmPinLocked;
	private String mdmModel;
	private String mdmManufacturer;
	private String mdmImei;
	private String mdmMeid;
	private String mdmUdid;
	private String mdmSerialNumber;
	private String mdmLocation;
	private String mdmDeviceManager;
	private String mdmLastSyncTime;
	private List<String> selectedAuthzProfiles = new ArrayList<String>();
	private String vn;

	public enum State {
		DISCONNECTED, AUTHENTICATING, AUTHENTICATED, POSTURED, STARTED
	}

	// Getters and Setters below...

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCallingStationId() {
		return callingStationId;
	}

	public void setCallingStationId(String callingStationId) {
		this.callingStationId = callingStationId;
	}

	public String getCalledStationId() {
		return calledStationId;
	}

	public void setCalledStationId(String calledStationId) {
		this.calledStationId = calledStationId;
	}

	public String getAuditSessionId() {
		return auditSessionId;
	}

	public void setAuditSessionId(String auditSessionId) {
		this.auditSessionId = auditSessionId;
	}

	public List<String> getIpAddresses() {
		return new ArrayList<>(ipAddresses);
	}

	public void setIpAddresses(List<String> ipAddresses) {
		this.ipAddresses = new ArrayList<>(ipAddresses);
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getNasIpAddress() {
		return nasIpAddress;
	}

	public void setNasIpAddress(String nasIpAddress) {
		this.nasIpAddress = nasIpAddress;
	}

	public String getNasPortId() {
		return nasPortId;
	}

	public void setNasPortId(String nasPortId) {
		this.nasPortId = nasPortId;
	}

	public String getNasIdentifier() {
		return nasIdentifier;
	}

	public void setNasIdentifier(String nasIdentifier) {
		this.nasIdentifier = nasIdentifier;
	}

	public String getNasPortType() {
		return nasPortType;
	}

	public void setNasPortType(String nasPortType) {
		this.nasPortType = nasPortType;
	}

	public String getAncPolicy() {
		return ancPolicy;
	}

	public void setAncPolicy(String ancPolicy) {
		this.ancPolicy = ancPolicy;
	}

	public String getPostureStatus() {
		return postureStatus;
	}

	public void setPostureStatus(String postureStatus) {
		this.postureStatus = postureStatus;
	}

	public String getEndpointProfile() {
		return endpointProfile;
	}

	public void setEndpointProfile(String endpointProfile) {
		this.endpointProfile = endpointProfile;
	}

	public String getEndpointOperatingSystem() {
		return endpointOperatingSystem;
	}

	public void setEndpointOperatingSystem(String endpointOperatingSystem) {
		this.endpointOperatingSystem = endpointOperatingSystem;
	}

	public String getCtsSecurityGroup() {
		return ctsSecurityGroup;
	}

	public void setCtsSecurityGroup(String ctsSecurityGroup) {
		this.ctsSecurityGroup = ctsSecurityGroup;
	}

	public String getAdNormalizedUser() {
		return adNormalizedUser;
	}

	public void setAdNormalizedUser(String adNormalizedUser) {
		this.adNormalizedUser = adNormalizedUser;
	}

	public String getAdUserDomainName() {
		return adUserDomainName;
	}

	public void setAdUserDomainName(String adUserDomainName) {
		this.adUserDomainName = adUserDomainName;
	}

	public String getAdHostDomainName() {
		return adHostDomainName;
	}

	public void setAdHostDomainName(String adHostDomainName) {
		this.adHostDomainName = adHostDomainName;
	}

	public String getAdUserNetBiosName() {
		return adUserNetBiosName;
	}

	public void setAdUserNetBiosName(String adUserNetBiosName) {
		this.adUserNetBiosName = adUserNetBiosName;
	}

	public String getAdHostNetBiosName() {
		return adHostNetBiosName;
	}

	public void setAdHostNetBiosName(String adHostNetBiosName) {
		this.adHostNetBiosName = adHostNetBiosName;
	}

	public String getAdUserResolvedIdentities() {
		return adUserResolvedIdentities;
	}

	public void setAdUserResolvedIdentities(String adUserResolvedIdentities) {
		this.adUserResolvedIdentities = adUserResolvedIdentities;
	}

	public String getAdUserResolvedDns() {
		return adUserResolvedDns;
	}

	public void setAdUserResolvedDns(String adUserResolvedDns) {
		this.adUserResolvedDns = adUserResolvedDns;
	}

	public String getAdHostResolvedIdentities() {
		return adHostResolvedIdentities;
	}

	public void setAdHostResolvedIdentities(String adHostResolvedIdentities) {
		this.adHostResolvedIdentities = adHostResolvedIdentities;
	}

	public String getAdHostResolvedDns() {
		return adHostResolvedDns;
	}

	public void setAdHostResolvedDns(String adHostResolvedDns) {
		this.adHostResolvedDns = adHostResolvedDns;
	}

	public void setAdUserQualifiedName(String adUserQualifiedName) {
		this.adUserQualifiedName = adUserQualifiedName;
	}

	public String getAdUserSamAccountName() {
		return adUserSamAccountName;
	}

	public void setAdUserSamAccountName(String adUserSamAccountName) {
		this.adUserSamAccountName = adUserSamAccountName;
	}

	public List<String> getProviders() {
		return new ArrayList<>(providers);
	}

	public void setProviders(List<String> providers) {
		this.providers = new ArrayList<>(providers);
	}

	public String getEndpointCheckResult() {
		return endpointCheckResult;
	}

	public void setEndpointCheckResult(String endpointCheckResult) {
		this.endpointCheckResult = endpointCheckResult;
	}

	public OffsetDateTime getEndpointCheckTime() {
		return endpointCheckTime;
	}

	public void setEndpointCheckTime(OffsetDateTime endpointCheckTime) {
		this.endpointCheckTime = endpointCheckTime;
	}

	public Long getIdentitySourcePortStart() {
		return identitySourcePortStart;
	}

	public void setIdentitySourcePortStart(Long identitySourcePortStart) {
		this.identitySourcePortStart = identitySourcePortStart;
	}

	public Long getIdentitySourcePortEnd() {
		return identitySourcePortEnd;
	}

	public void setIdentitySourcePortEnd(Long identitySourcePortEnd) {
		this.identitySourcePortEnd = identitySourcePortEnd;
	}

	public Long getIdentitySourcePortFirst() {
		return identitySourcePortFirst;
	}

	public void setIdentitySourcePortFirst(Long identitySourcePortFirst) {
		this.identitySourcePortFirst = identitySourcePortFirst;
	}

	public String getTerminalServerAgentId() {
		return terminalServerAgentId;
	}

	public void setTerminalServerAgentId(String terminalServerAgentId) {
		this.terminalServerAgentId = terminalServerAgentId;
	}

	public String getIsMachineAuthentication() {
		return isMachineAuthentication;
	}

	public void setIsMachineAuthentication(String isMachineAuthentication) {
		this.isMachineAuthentication = isMachineAuthentication;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getTunnelPrivateGroupId() {
		return tunnelPrivateGroupId;
	}

	public void setTunnelPrivateGroupId(String tunnelPrivateGroupId) {
		this.tunnelPrivateGroupId = tunnelPrivateGroupId;
	}

	public String getAirespaceWlanId() {
		return airespaceWlanId;
	}

	public void setAirespaceWlanId(String airespaceWlanId) {
		this.airespaceWlanId = airespaceWlanId;
	}

	public String getNetworkDeviceProfileName() {
		return networkDeviceProfileName;
	}

	public void setNetworkDeviceProfileName(String networkDeviceProfileName) {
		this.networkDeviceProfileName = networkDeviceProfileName;
	}

	public String getRadiusFlowType() {
		return radiusFlowType;
	}

	public void setRadiusFlowType(String radiusFlowType) {
		this.radiusFlowType = radiusFlowType;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getMdmMacAddress() {
		return mdmMacAddress;
	}

	public void setMdmMacAddress(String mdmMacAddress) {
		this.mdmMacAddress = mdmMacAddress;
	}

	public String getMdmOsVersion() {
		return mdmOsVersion;
	}

	public void setMdmOsVersion(String mdmOsVersion) {
		this.mdmOsVersion = mdmOsVersion;
	}

	public boolean isMdmRegistered() {
		return mdmRegistered;
	}

	public void setMdmRegistered(boolean mdmRegistrationStatus) {
		this.mdmRegistered = mdmRegistrationStatus;
	}

	public boolean isMdmCompliant() {
		return mdmCompliant;
	}

	public void setMdmCompliant(boolean mdmComplianceStatus) {
		this.mdmCompliant = mdmComplianceStatus;
	}

	public boolean isMdmDiskEncrypted() {
		return mdmDiskEncrypted;
	}

	public void setMdmDiskEncrypted(boolean mdmDiskEncryptionStatus) {
		this.mdmDiskEncrypted = mdmDiskEncryptionStatus;
	}

	public boolean isMdmJailBroken() {
		return mdmJailBroken;
	}

	public void setMdmJailBroken(boolean mdmJailBroken) {
		this.mdmJailBroken = mdmJailBroken;
	}

	public boolean isMdmPinLocked() {
		return mdmPinLocked;
	}

	public void setMdmPinLocked(boolean mdmPinLocked) {
		this.mdmPinLocked = mdmPinLocked;
	}

	public String getMdmModel() {
		return mdmModel;
	}

	public void setMdmModel(String mdmModel) {
		this.mdmModel = mdmModel;
	}

	public String getMdmManufacturer() {
		return mdmManufacturer;
	}

	public void setMdmManufacturer(String mdmManufacturer) {
		this.mdmManufacturer = mdmManufacturer;
	}

	public String getMdmImei() {
		return mdmImei;
	}

	public void setMdmImei(String mdmImei) {
		this.mdmImei = mdmImei;
	}

	public String getMdmMeid() {
		return mdmMeid;
	}

	public void setMdmMeid(String mdmMeid) {
		this.mdmMeid = mdmMeid;
	}

	public String getMdmUdid() {
		return mdmUdid;
	}

	public void setMdmUdid(String mdmUdid) {
		this.mdmUdid = mdmUdid;
	}

	public String getMdmSerialNumber() {
		return mdmSerialNumber;
	}

	public void setMdmSerialNumber(String mdmSerialNumber) {
		this.mdmSerialNumber = mdmSerialNumber;
	}

	public String getMdmLocation() {
		return mdmLocation;
	}

	public void setMdmLocation(String mdmLocation) {
		this.mdmLocation = mdmLocation;
	}

	public String getMdmDeviceManager() {
		return mdmDeviceManager;
	}

	public void setMdmDeviceManager(String mdmDeviceManager) {
		this.mdmDeviceManager = mdmDeviceManager;
	}

	public String getMdmLastSyncTime() {
		return mdmLastSyncTime;
	}

	public void setMdmLastSyncTime(String mdmLastSyncTime) {
		this.mdmLastSyncTime = mdmLastSyncTime;
	}

	public String getAdUserQualifiedName() {
		return adUserQualifiedName;
	}

	public String getAdHostQualifiedName() {
		return adHostQualifiedName;
	}

	public void setAdHostQualifiedName(String adHostQualifiedName) {
		this.adHostQualifiedName = adHostQualifiedName;
	}

	public String getAdHostSamAccountName() {
		return adHostSamAccountName;
	}

	public void setAdHostSamAccountName(String adHostSamAccountName) {
		this.adHostSamAccountName = adHostSamAccountName;
	}

	public List<String> getSelectedAuthzProfiles() {
		return new ArrayList<>(selectedAuthzProfiles);
	}

	public void setSelectedAuthzProfiles(List<String> selectedAuthzProfiles) {
		this.selectedAuthzProfiles = new ArrayList<>(selectedAuthzProfiles);
	}

	public String getVn() {
		return vn;
	}

	public void setVn(String vn) {
		this.vn = vn;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
