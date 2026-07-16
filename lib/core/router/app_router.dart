import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../features/core_manager/presentation/core_manager_page.dart';
import '../../features/home/presentation/home_page.dart';
import '../../features/logs/presentation/logs_page.dart';
import '../../features/profiles/presentation/profiles_page.dart';
import '../../features/settings/presentation/settings_page.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        builder: (_, __) => const HomePage(),
      ),
      GoRoute(
        path: '/profiles',
        builder: (_, __) => const ProfilesPage(),
      ),
      GoRoute(
        path: '/cores',
        builder: (_, __) => const CoreManagerPage(),
      ),
      GoRoute(
        path: '/logs',
        builder: (_, __) => const LogsPage(),
      ),
      GoRoute(
        path: '/settings',
        builder: (_, __) => const SettingsPage(),
      ),
    ],
  );
});
