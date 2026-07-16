import 'package:flutter/services.dart';

class VpnChannel {
  static const MethodChannel _channel = MethodChannel('vpn_starter/vpn');

  static Future<bool> startVpn() async {
    final result = await _channel.invokeMethod<bool>('startVpn');
    return result ?? false;
  }

  static Future<bool> stopVpn() async {
    final result = await _channel.invokeMethod<bool>('stopVpn');
    return result ?? false;
  }
}
