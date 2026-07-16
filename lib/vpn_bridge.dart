import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';

class VpnBridge {
  static const MethodChannel _channel =
      MethodChannel('com.example.vpn_starter/vpn');

  static Future<bool> prepareVpn() async {
    try {
      debugPrint("DEBUG: Requesting VPN prepare...");
      final result = await _channel.invokeMethod<bool>('prepareVpn');
      debugPrint("DEBUG: VPN prepare result: $result");
      return result ?? false;
    } catch (e) {
      debugPrint("ERROR: Failed to prepare VPN: $e");
      return false;
    }
  }

  static Future<bool> startVpn(String configJson) async {
    try {
      debugPrint("DEBUG: Calling startVpn with config...");
      final result = await _channel.invokeMethod<bool>(
        'startVpn',
        {'config': configJson},
      );
      debugPrint("DEBUG: Start VPN response: $result");
      return result ?? false;
    } catch (e) {
      debugPrint("ERROR: Exception during startVpn: $e");
      return false;
    }
  }

  static Future<bool> stopVpn() async {
    try {
      debugPrint("DEBUG: Requesting stopVpn...");
      final result = await _channel.invokeMethod<bool>('stopVpn');
      debugPrint("DEBUG: Stop VPN response: $result");
      return result ?? false;
    } catch (e) {
      debugPrint("ERROR: Exception during stopVpn: $e");
      return false;
    }
  }

  static Future<bool> vpnStatus() async {
    try {
      final result = await _channel.invokeMethod<bool>('vpnStatus');
      return result ?? false;
    } catch (e) {
      debugPrint("ERROR: Could not check VPN status: $e");
      return false;
    }
  }
}
