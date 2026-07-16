import 'package:flutter/material.dart';
import '../../../shared/widgets/app_scaffold.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return AppScaffold(
      title: 'Settings',
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: const [
          Card(
            child: SwitchListTile(
              value: true,
              onChanged: null,
              title: Text('Auto reconnect'),
              subtitle: Text('Coming soon'),
            ),
          ),
          Card(
            child: ListTile(
              title: Text('Core download source'),
              subtitle: Text('GitHub Releases - Coming soon'),
            ),
          ),
          Card(
            child: ListTile(
              title: Text('Route mode'),
              subtitle: Text('Full VPN - Coming soon'),
            ),
          ),
        ],
      ),
    );
  }
}
