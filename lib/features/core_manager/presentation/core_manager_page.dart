import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../shared/widgets/app_scaffold.dart';
import '../application/core_manager_controller.dart';
import '../domain/core_info.dart';

class CoreManagerPage extends ConsumerWidget {
  const CoreManagerPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(coreManagerControllerProvider);
    final controller = ref.read(coreManagerControllerProvider.notifier);

    return AppScaffold(
      title: 'Core Manager',
      child: state.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text('Error: $error')),
        data: (cores) => ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: cores.length,
          itemBuilder: (context, index) {
            final core = cores[index];
            return _CoreCard(
              core: core,
              onInstall: () => controller.install(core.name),
              onUpdate: () => controller.update(core.name),
            );
          },
        ),
      ),
    );
  }
}

class _CoreCard extends StatelessWidget {
  final CoreInfo core;
  final VoidCallback onInstall;
  final VoidCallback onUpdate;

  const _CoreCard({
    required this.core,
    required this.onInstall,
    required this.onUpdate,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: const Icon(Icons.memory),
        title: Text(core.name),
        subtitle: Text(
          core.installed
              ? 'Installed • version: ${core.version ?? 'unknown'}'
              : 'Not installed',
        ),
        trailing: core.installed
            ? OutlinedButton(
                onPressed: onUpdate,
                child: const Text('Update'),
              )
            : FilledButton(
                onPressed: onInstall,
                child: const Text('Install'),
              ),
      ),
    );
  }
}
