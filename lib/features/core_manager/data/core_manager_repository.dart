import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/core/platform/core_platform_api.dart';
import 'package:vpn_starter/features/core_manager/domain/core_info.dart';

class CoreManagerRepository {
  CoreManagerRepository(this._api);

  final CorePlatformApi _api;

  Future<List<CoreInfo>> getCores() async {
    final raw = await _api.getCores();
    return raw.map(CoreInfo.fromMap).toList();
  }

  Future<void> installCore(String name) {
    return _api.installCore(name);
  }

  Future<void> updateCore(String name) {
    return _api.updateCore(name);
  }
}

final coreManagerRepositoryProvider = Provider<CoreManagerRepository>((ref) {
  final api = ref.watch(corePlatformApiProvider);
  return CoreManagerRepository(api);
});
