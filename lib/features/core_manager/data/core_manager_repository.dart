import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/features/core_manager/domain/core_info.dart';
import 'package:vpn_starter/services/platform/core_platform_api.dart';

class CoreManagerRepository {
  CoreManagerRepository({CorePlatformApi? api}) : _api = api ?? CorePlatformApi();

  final CorePlatformApi _api;

  Future<List<CoreInfo>> getInstalledCores() {
    return _api.getInstalledCores();
  }

  Future<List<CoreInfo>> getCores() {
    return getInstalledCores();
  }

  Future<void> installCore(String name) {
    return _api.installCore(name);
  }

  Future<void> updateCore(String name) {
    return _api.updateCore(name);
  }
}

final coreManagerRepositoryProvider = Provider<CoreManagerRepository>((ref) {
  return CoreManagerRepository();
});
