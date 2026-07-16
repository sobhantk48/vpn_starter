import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../features/core_manager/domain/core_info.dart';

final corePlatformApiProvider = Provider<CorePlatformApi>((ref) {
  return CorePlatformApi();
});

class CorePlatformApi {
  static const _methodChannel = MethodChannel('vpn_starter/core');
  static const _eventChannel = EventChannel('vpn_starter/logs');

  Stream<dynamic> get logsStream => _eventChannel.receiveBroadcastStream();

  Future<List<CoreInfo>> getInstalledCores() async {
    try {
      final result = await _methodChannel.invokeListMethod<dynamic>('getCores');

      if (result == null || result.isEmpty) {
        return const [
          CoreInfo(
            name: 'sing-box',
            version: null,
            installed: false,
            updateAvailable: false,
            downloading: false,
          ),
          CoreInfo(
            name: 'xray',
            version: null,
            installed: false,
            updateAvailable: false,
            downloading: false,
          ),
        ];
      }

      return result.map((item) {
        final map = Map<String, dynamic>.from(item as Map);
        return CoreInfo(
          name: map['name'] as String,
          version: map['version'] as String?,
          installed: map['installed'] as bool? ?? false,
          updateAvailable: map['updateAvailable'] as bool? ?? false,
          downloading: map['downloading'] as bool? ?? false,
        );
      }).toList();
    } catch (_) {
      return const [
        CoreInfo(
          name: 'sing-box',
          version: null,
          installed: false,
          updateAvailable: false,
          downloading: false,
        ),
        CoreInfo(
          name: 'xray',
          version: null,
          installed: false,
          updateAvailable: false,
          downloading: false,
        ),
      ];
    }
  }

  Future<void> installCore(String name) async {
    await _methodChannel.invokeMethod('installCore', {'name': name});
  }

  Future<void> updateCore(String name) async {
    await _methodChannel.invokeMethod('updateCore', {'name': name});
  }

  Future<void> startCore({
    required String profileName,
    required String coreName,
  }) async {
    await _methodChannel.invokeMethod('startCore', {
      'profileName': profileName,
      'coreName': coreName,
    });
  }

  Future<void> stopCore() async {
    await _methodChannel.invokeMethod('stopCore');
  }
}
