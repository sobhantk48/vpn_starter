import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/features/core_manager/domain/core_info.dart';
import 'package:vpn_starter/services/platform/core_platform_api.dart';

class CoreManagerRepository {
  const CoreManagerRepository();

  Future<List<CoreInfo>> getInstalledCores() async {
    final raw = await CorePlatformApi.getCores();
    return raw.map(CoreInfo.fromMap).toList();
  }

  Future<List<CoreInfo>> getCores() {
    return getInstalledCores();
  }

  Future<void> installCore(String name) {
    return CorePlatformApi.installCore(name);
  }

  Future<void> updateCore(String name) {
    return CorePlatformApi.updateCore(name);
  }
}

final coreManagerRepositoryProvider = Provider<CoreManagerRepository>((ref) {
  return const CoreManagerRepository();
});
