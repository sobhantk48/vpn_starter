import 'package:equatable/equatable.dart';

enum VpnConnectionStatus {
  disconnected,
  connecting,
  connected,
  stopping,
  error,
}

class ConnectionStateModel extends Equatable {
  final VpnConnectionStatus status;
  final String? activeProfileName;
  final String? activeCore;
  final String? message;

  const ConnectionStateModel({
    required this.status,
    this.activeProfileName,
    this.activeCore,
    this.message,
  });

  const ConnectionStateModel.initial()
      : status = VpnConnectionStatus.disconnected,
        activeProfileName = null,
        activeCore = null,
        message = null;

  ConnectionStateModel copyWith({
    VpnConnectionStatus? status,
    String? activeProfileName,
    String? activeCore,
    String? message,
  }) {
    return ConnectionStateModel(
      status: status ?? this.status,
      activeProfileName: activeProfileName ?? this.activeProfileName,
      activeCore: activeCore ?? this.activeCore,
      message: message ?? this.message,
    );
  }

  @override
  List<Object?> get props => [status, activeProfileName, activeCore, message];
}
