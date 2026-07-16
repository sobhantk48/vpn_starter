import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/core/logs/logs_repository.dart';

final logsRepositoryProvider = Provider<LogsRepository>((ref) {
  return LogsRepository();
});

final logsControllerProvider =
    StateNotifierProvider<LogsController, List<String>>((ref) {
  final controller = LogsController(ref.read(logsRepositoryProvider));
  ref.onDispose(controller.dispose);
  return controller;
});

class LogsController extends StateNotifier<List<String>> {
  LogsController(this._repository) : super(const []) {
    _subscription = _repository.streamLogs().listen(_onLogLine);
  }

  final LogsRepository _repository;
  StreamSubscription<String>? _subscription;

  void _onLogLine(String line) {
    final next = <String>[...state, line];
    if (next.length > 300) {
      state = next.sublist(next.length - 300);
      return;
    }
    state = next;
  }

  void clear() {
    state = const [];
  }

  Future<void> dispose() async {
    await _subscription?.cancel();
  }
}
