import 'package:flutter/material.dart';
import '../../../shared/widgets/app_scaffold.dart';

class ProfilesPage extends StatelessWidget {
  const ProfilesPage({super.key});

  @override
  Widget build(BuildContext context) {
    return AppScaffold(
      title: 'Profiles',
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: const [
          Card(
            child: ListTile(
              leading: Icon(Icons.vpn_key),
              title: Text('demo-profile'),
              subtitle: Text('VLESS'),
              trailing: Icon(Icons.chevron_right),
            ),
          ),
        ],
      ),
    );
  }
}
