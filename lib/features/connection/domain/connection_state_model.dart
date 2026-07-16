enum VpnConnectionStatus {
  disconnected,
  connecting,
  connected,
  disconnecting,
  error,
}

class ConnectionStateModel {
  const ConnectionStateModel({
    required this.status,
    required this.message,
    this.activeProfileName,
    this.activeCore,
  });

  const ConnectionStateModel.initial()
      : status = VpnConnectionStatus.disconnected,
        message = 'Disconnected',
        activeProfileName = null,
        activeCore = null;

  const ConnectionStateModel.disconnected({
    this.message = 'Disconnected',
    this.activeProfileName,
    this.activeCore,
  }) : status = VpnConnectionStatus.disconnected;

  const ConnectionStateModel.connecting({
    this.message = 'Connecting...',
    this.activeProfileName,
    this.activeCore,
  }) : status = VpnConnectionStatus.connecting;

  const ConnectionStateModel.connected({
    this.message = 'Connected',
    this.activeProfileName,
    this.activeCore,
  }) : status = VpnConnectionStatus.connected;

  const ConnectionStateModel.disconnecting({
    this.message = 'Disconnecting...',
    this.activeProfileName,
    this.activeCore,
  }) : status = VpnConnectionStatus.disconnecting;

  const ConnectionStateModel.error({
    this.message = 'Connection error',
    this.activeProfileName,
    this.activeCore,
  }) : status = VpnConnectionStatus.error;

  final VpnConnectionStatus status;
  final String message;
  final String? activeProfileName;
  final String? activeCore;

  String? get activeCoreName => activeCore;

  bool get isConnected => status == VpnConnectionStatus.connected;

  bool get isBusy =>
      status == VpnConnectionStatus.connecting ||
      status == VpnConnectionStatus.disconnecting;

  ConnectionStateModel copyWith({
    VpnConnectionStatus? status,
    String? message,
    String? activeProfileName,
    String? activeCore,
    String? activeCoreName,
    bool clearProfile = false,
    bool clearCore = false,
  }) {
    return ConnectionStateModel(
      status: status ?? this.status,
      message: message ?? this.message,
      activeProfileName:
          clearProfile ? null : (activeProfileName ?? this.activeProfileName),
      activeCore: clearCore
          ? null
          : (activeCore ?? activeCoreName ?? this.activeCore),
    );
  }
}
