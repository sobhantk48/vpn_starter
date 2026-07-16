enum ConnectionStatus {
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
    this.activeCoreName,
  });

  const ConnectionStateModel.disconnected({
    this.message = 'Disconnected',
    this.activeProfileName,
    this.activeCoreName,
  }) : status = ConnectionStatus.disconnected;

  const ConnectionStateModel.connecting({
    this.message = 'Connecting...',
    this.activeProfileName,
    this.activeCoreName,
  }) : status = ConnectionStatus.connecting;

  const ConnectionStateModel.connected({
    this.message = 'Connected',
    this.activeProfileName,
    this.activeCoreName,
  }) : status = ConnectionStatus.connected;

  const ConnectionStateModel.disconnecting({
    this.message = 'Disconnecting...',
    this.activeProfileName,
    this.activeCoreName,
  }) : status = ConnectionStatus.disconnecting;

  const ConnectionStateModel.error({
    this.message = 'Connection error',
    this.activeProfileName,
    this.activeCoreName,
  }) : status = ConnectionStatus.error;

  final ConnectionStatus status;
  final String message;
  final String? activeProfileName;
  final String? activeCoreName;

  bool get isConnected => status == ConnectionStatus.connected;

  bool get isBusy =>
      status == ConnectionStatus.connecting ||
      status == ConnectionStatus.disconnecting;

  ConnectionStateModel copyWith({
    ConnectionStatus? status,
    String? message,
    String? activeProfileName,
    String? activeCoreName,
    bool clearProfile = false,
    bool clearCore = false,
  }) {
    return ConnectionStateModel(
      status: status ?? this.status,
      message: message ?? this.message,
      activeProfileName:
          clearProfile ? null : (activeProfileName ?? this.activeProfileName),
      activeCoreName:
          clearCore ? null : (activeCoreName ?? this.activeCoreName),
    );
  }
}
