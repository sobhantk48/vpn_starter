import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../services/platform/core_platform_api.dart';
import '../domain/core_info.dart';

final coreManagerRepositoryProvider = Provider<CoreManagerRepository>((ref) {
  return CoreManagerRepository(ref.read(corePlatformApiProvider));
});

class CoreManagerRepository {
  final CorePlatformApi _platformApi;

  CoreManagerRepository(this._platformApi);

  Future<List<CoreInfo>> getInstalledCores() {
    return _platformApi.getInstalledCores();
  }

  Future<void> installCore(String name) {
    return _platformApi.installCore(name);
  }

  Future<void> updateCore(String name) {
    return _platformApi.updateCore(name);
  }
}
