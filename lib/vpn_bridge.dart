import 'package:flutter/services.dart';

class VpnBridge {
  static const MethodChannel _channel =
      MethodChannel('com.example.vpn_starter/vpn');

  static Future<bool> prepareVpn() async {
    final result = await _channel.invokeMethod<bool>('prepareVpn');
    return result ?? false;
  }

  static Future<bool> startVpn(String configJson) async {
    final result = await _channel.invokeMethod<bool>(
      'startVpn',
      {'config': configJson},
    );
    return result ?? false;
  }

  static Future<bool> stopVpn() async {
    final result = await _channel.invokeMethod<bool>('stopVpn');
    return result ?? false;
  }

  static Future<bool> vpnStatus() async {
    final result = await _channel.invokeMethod<bool>('vpnStatus');
    return result ?? false;
  }
}
