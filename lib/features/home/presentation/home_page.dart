import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vpn_starter/features/home/application/connection_controller.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(connectionControllerProvider);
    final controller = ref.read(connectionControllerProvider.notifier);

    const profileName = 'Default Profile';
    const coreName = 'sing-box';

    final isConnected = state.isConnected;
    final isBusy = state.isBusy;

    return Scaffold(
      appBar: AppBar(
        title: const Text('VPN Starter'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    Text(
                      isConnected ? 'Connected' : 'Disconnected',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 12),
                    Text(state.message),
                    const SizedBox(height: 12),
                    Text('Profile: ${state.activeProfileName ?? profileName}'),
                    Text('Core: ${state.activeCoreName ?? coreName}'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: isBusy
                  ? null
                  : () async {
                      if (isConnected) {
                        await controller.disconnect();
                      } else {
                        await controller.connect(
                          profileName: profileName,
                          coreName: coreName,
                        );
                      }
                    },
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 14),
                child: Text(
                  isBusy
                      ? 'Please wait...'
                      : isConnected
                          ? 'Disconnect'
                          : 'Connect',
                ),
              ),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: isBusy
                  ? null
                  : () async {
                      await controller.connect(
                        profileName: profileName,
                        coreName: coreName,
                      );
                    },
              child: const Text('Retry Connect'),
            ),
          ],
        ),
      ),
    );
  }
}
