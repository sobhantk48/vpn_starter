import 'package:vpn_starter/services/core_platform_api.dart';

class LogsRepository {
  Stream<String> streamLogs() {
    return CorePlatformApi.logs();
  }
}
