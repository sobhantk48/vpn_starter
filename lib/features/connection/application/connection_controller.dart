import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../services/platform/core_platform_api.dart';
import '../domain/connection_state_model.dart';

final connectionControllerProvider =
    NotifierProvider<ConnectionController, ConnectionStateModel>(
  ConnectionController.new,
);

class ConnectionController extends Notifier<ConnectionStateModel> {
  late final CorePlatformApi _platformApi;

  @override
  ConnectionStateModel build() {
    _platformApi = ref.read(corePlatformApiProvider);
    return const ConnectionStateModel.initial();
  }

  Future<void> connect({
    required String profileName,
    required String coreName,
  }) async {
    state = state.copyWith(
      status: VpnConnectionStatus.connecting,
      activeProfileName: profileName,
      activeCore: coreName,
      message: 'Connecting...',
    );

    try {
      await _platformApi.startCore(
        profileName: profileName,
        coreName: coreName,
      );

      state = state.copyWith(
        status: VpnConnectionStatus.connected,
        message: 'Connected',
      );
    } catch (e) {
      state = state.copyWith(
        status: VpnConnectionStatus.error,
        message: e.toString(),
      );
    }
  }

  Future<void> disconnect() async {
    state = state.copyWith(
      status: VpnConnectionStatus.stopping,
      message: 'Stopping...',
    );

    try {
      await _platformApi.stopCore();
      state = const ConnectionStateModel.initial();
    } catch (e) {
      state = state.copyWith(
        status: VpnConnectionStatus.error,
        message: e.toString(),
      );
    }
  }
}
