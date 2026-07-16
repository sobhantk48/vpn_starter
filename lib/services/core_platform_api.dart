import 'dart:async';

import 'package:flutter/services.dart';

class CorePlatformApi {
  CorePlatformApi._();

  static const MethodChannel _coreChannel = MethodChannel('vpn_starter/core');
  static const EventChannel _logsChannel = EventChannel('vpn_starter/logs');

  static Stream<String>? _logStream;

  static Stream<String> logs() {
    _logStream ??= _logsChannel
        .receiveBroadcastStream()
        .map((event) => event?.toString() ?? '')
        .where((line) => line.isNotEmpty);
    return _logStream!;
  }

  static Future<List<Map<String, dynamic>>> getCores() async {
    final result = await _coreChannel.invokeMethod<List<dynamic>>('getCores');
    return (result ?? const [])
        .map(
          (item) => Map<String, dynamic>.from(
            (item as Map).cast<dynamic, dynamic>(),
          ),
        )
        .toList();
  }

  static Future<void> installCore(String name) {
    return _coreChannel.invokeMethod<void>(
      'installCore',
      <String, dynamic>{'name': name},
    );
  }

  static Future<void> updateCore(String name) {
    return _coreChannel.invokeMethod<void>(
      'updateCore',
      <String, dynamic>{'name': name},
    );
  }

  static Future<bool> requestVpnPermission() async {
    final result = await _coreChannel.invokeMethod<bool>('requestVpnPermission');
    return result ?? false;
  }

  static Future<bool> startCore({
    required String profileName,
    required String coreName,
  }) async {
    final result = await _coreChannel.invokeMethod<bool>(
      'startCore',
      <String, dynamic>{
        'profileName': profileName,
        'coreName': coreName,
      },
    );
    return result ?? false;
  }

  static Future<bool> stopCore() async {
    final result = await _coreChannel.invokeMethod<bool>('stopCore');
    return result ?? false;
  }
}
