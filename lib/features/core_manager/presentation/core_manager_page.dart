import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/features/core_manager/application/core_manager_controller.dart';
import 'package:vpn_starter/features/core_manager/domain/core_info.dart';

class CoreManagerPage extends ConsumerWidget {
  const CoreManagerPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(coreManagerControllerProvider);
    final controller = ref.read(coreManagerControllerProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Core Manager'),
      ),
      body: state.when(
        data: (cores) {
          if (cores.isEmpty) {
            return const Center(
              child: Text('No cores found'),
            );
          }

          return RefreshIndicator(
            onRefresh: controller.refresh,
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: cores.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, index) {
                final core = cores[index];
                return _CoreTile(
                  core: core,
                  onInstall: () => controller.install(core.name),
                  onUpdate: () => controller.update(core.name),
                );
              },
            ),
          );
        },
        loading: () => const Center(
          child: CircularProgressIndicator(),
        ),
        error: (error, stackTrace) => Center(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Failed to load cores:\n$error',
              textAlign: TextAlign.center,
            ),
          ),
        ),
      ),
    );
  }
}

class _CoreTile extends StatelessWidget {
  const _CoreTile({
    required this.core,
    required this.onInstall,
    required this.onUpdate,
  });

  final CoreInfo core;
  final VoidCallback onInstall;
  final VoidCallback onUpdate;

  @override
  Widget build(BuildContext context) {
    final installedText = core.installed ? 'Installed' : 'Not installed';
    final versionText = core.version == null ? '' : ' • ${core.version}';

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    core.name,
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 6),
                  Text('$installedText$versionText'),
                ],
              ),
            ),
            const SizedBox(width: 12),
            if (!core.installed)
              FilledButton(
                onPressed: onInstall,
                child: const Text('Install'),
              )
            else
              OutlinedButton(
                onPressed: onUpdate,
                child: const Text('Update'),
              ),
          ],
        ),
      ),
    );
  }
}
