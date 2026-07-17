import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/services/core_platform_api.dart';

enum VpnUiStatus {
  disconnected,
  connecting,
  connected,
  disconnecting,
  error,
}

class ConnectionUiState {
  const ConnectionUiState({
    required this.status,
    required this.message,
    required this.activeProfileName,
    required this.activeCoreName,
  });

  final VpnUiStatus status;
  final String message;
  final String? activeProfileName;
  final String? activeCoreName;

  bool get isConnected => status == VpnUiStatus.connected;

  bool get isBusy =>
      status == VpnUiStatus.connecting || status == VpnUiStatus.disconnecting;

  ConnectionUiState copyWith({
    VpnUiStatus? status,
    String? message,
    String? activeProfileName,
    String? activeCoreName,
  }) {
    return ConnectionUiState(
      status: status ?? this.status,
      message: message ?? this.message,
      activeProfileName: activeProfileName ?? this.activeProfileName,
      activeCoreName: activeCoreName ?? this.activeCoreName,
    );
  }

  static const initial = ConnectionUiState(
    status: VpnUiStatus.disconnected,
    message: 'Disconnected',
    activeProfileName: null,
    activeCoreName: null,
  );
}

final connectionControllerProvider =
    StateNotifierProvider<ConnectionController, ConnectionUiState>((ref) {
  return ConnectionController();
});

class ConnectionController extends StateNotifier<ConnectionUiState> {
  ConnectionController() : super(ConnectionUiState.initial);

  Future<void> connect({
    required String profileName,
    required String coreName,
  }) async {
    if (state.isBusy) return;

    state = state.copyWith(
      status: VpnUiStatus.connecting,
      message: 'Requesting VPN permission...',
      activeProfileName: profileName,
      activeCoreName: coreName,
    );

    try {
      final granted = await CorePlatformApi.requestVpnPermission();
      if (!granted) {
        state = state.copyWith(
          status: VpnUiStatus.disconnected,
          message: 'VPN permission not granted',
        );
        return;
      }

      state = state.copyWith(
        status: VpnUiStatus.connecting,
        message: 'Starting VPN service...',
      );

      final started = await CorePlatformApi.startCore(
        profileName: profileName,
        coreName: coreName,
      );

      if (!started) {
        state = state.copyWith(
          status: VpnUiStatus.error,
          message: 'Failed to start VPN service',
        );
        return;
      }

      await Future<void>.delayed(const Duration(milliseconds: 500));

      final running = await CorePlatformApi.vpnStatus();
      if (!running) {
        state = state.copyWith(
          status: VpnUiStatus.error,
          message: 'VPN service did not reach running state',
        );
        return;
      }

      state = state.copyWith(
        status: VpnUiStatus.connected,
        message: 'Connected',
      );
    } on PlatformException catch (e) {
      state = state.copyWith(
        status: VpnUiStatus.error,
        message: e.message ?? e.code,
      );
    } catch (e) {
      state = state.copyWith(
        status: VpnUiStatus.error,
        message: e.toString(),
      );
    }
  }

  Future<void> disconnect() async {
    if (state.isBusy) return;

    state = state.copyWith(
      status: VpnUiStatus.disconnecting,
      message: 'Stopping VPN...',
    );

    try {
      final stopped = await CorePlatformApi.stopCore();
      if (!stopped) {
        state = state.copyWith(
          status: VpnUiStatus.error,
          message: 'Failed to stop VPN service',
        );
        return;
      }

      await Future<void>.delayed(const Duration(milliseconds: 300));

      final running = await CorePlatformApi.vpnStatus();
      if (running) {
        state = state.copyWith(
          status: VpnUiStatus.error,
          message: 'VPN service is still running',
        );
        return;
      }

      state = ConnectionUiState.initial;
    } on PlatformException catch (e) {
      state = state.copyWith(
        status: VpnUiStatus.error,
        message: e.message ?? e.code,
      );
    } catch (e) {
      state = state.copyWith(
        status: VpnUiStatus.error,
        message: e.toString(),
      );
    }
  }
}
