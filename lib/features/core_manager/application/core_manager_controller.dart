import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/core_manager_repository.dart';
import '../domain/core_info.dart';

final coreManagerControllerProvider =
    NotifierProvider<CoreManagerController, AsyncValue<List<CoreInfo>>>(
  CoreManagerController.new,
);

class CoreManagerController extends Notifier<AsyncValue<List<CoreInfo>>> {
  @override
  AsyncValue<List<CoreInfo>> build() {
    Future.microtask(load);
    return const AsyncValue.loading();
  }

  Future<void> load() async {
    state = const AsyncValue.loading();
    try {
      final repository = ref.read(coreManagerRepositoryProvider);
      final cores = await repository.getInstalledCores();
      state = AsyncValue.data(cores);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> install(String name) async {
    final repository = ref.read(coreManagerRepositoryProvider);
    await repository.installCore(name);
    await load();
  }

  Future<void> update(String name) async {
    final repository = ref.read(coreManagerRepositoryProvider);
    await repository.updateCore(name);
    await load();
  }
}
