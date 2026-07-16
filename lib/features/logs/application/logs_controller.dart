import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/core/logs/logs_repository.dart';

final logsControllerProvider =
    StateNotifierProvider<LogsController, List<String>>((ref) {
  final repository = ref.watch(logsRepositoryProvider);
  return LogsController(repository);
});

class LogsController extends StateNotifier<List<String>> {
  LogsController(this._repository) : super(const []) {
    _subscription = _repository.watchLogs().listen((line) {
      state = [...state, line];
    });
  }

  final LogsRepository _repository;
  StreamSubscription<String>? _subscription;

  void clear() {
    state = const [];
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}
